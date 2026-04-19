package com.pethub.service.impl;

import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.cache.DashboardCacheNames;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.CategoryMapper;
import com.pethub.mapper.PetMapper;
import com.pethub.pojo.dto.PetSaveDTO;
import com.pethub.pojo.dto.PetStatusDTO;
import com.pethub.pojo.entity.Category;
import com.pethub.pojo.entity.Pet;
import com.pethub.pojo.query.PetQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PetVO;
import com.pethub.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class PetServiceImpl implements PetService {

    private static final DefaultRedisScript<Long> UNLOCK_SCRIPT;

    static {
        UNLOCK_SCRIPT = new DefaultRedisScript<>();
        UNLOCK_SCRIPT.setLocation(new ClassPathResource("lua/unlock.lua"));
        UNLOCK_SCRIPT.setResultType(Long.class);
    }

    private final PetMapper petMapper;
    private final CategoryMapper categoryMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public PageResultVO<PetVO> page(PetQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<PetVO> records = petMapper.selectPage(query);
        PageInfo<PetVO> pageInfo = new PageInfo<>(records);
        return new PageResultVO<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    public PetVO getById(Long id) {
        String key = "pet:" + id;
        String json = redisTemplate.opsForValue().get(key);
        if (StringUtils.hasText(json)) {
            return JSONUtil.toBean(json, PetVO.class);
        }
        if (json != null) {
            throw new BusinessException("Pet does not exist");
        }

        String lockKey = "lock:pet:" + id;
        String threadId = UUID.randomUUID().toString();
        boolean locked = tryLock(lockKey, threadId);
        try {
            if (!locked) {
                sleepBriefly();
                return getById(id);
            }

            json = redisTemplate.opsForValue().get(key);
            if (StringUtils.hasText(json)) {
                return JSONUtil.toBean(json, PetVO.class);
            }
            if (json != null) {
                throw new BusinessException("Pet does not exist");
            }

            PetVO petVO = petMapper.selectById(id);
            if (petVO == null) {
                redisTemplate.opsForValue().set(key, "", 5, TimeUnit.MINUTES);
                throw new BusinessException("Pet does not exist");
            }

            long ttl = 60L + RandomUtil.randomInt(10);
            redisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(petVO), ttl, TimeUnit.MINUTES);
            return petVO;
        } finally {
            if (locked) {
                unlock(lockKey, threadId);
            }
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    })
    public void save(PetSaveDTO petSaveDTO) {
        validatePetSaveDTO(petSaveDTO);
        normalizeStatusByStock(petSaveDTO);

        int rows = petMapper.insert(petSaveDTO);
        if (rows < 1) {
            throw new BusinessException("Failed to create pet");
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    })
    public void update(Long id, PetSaveDTO petSaveDTO) {
        validatePetSaveDTO(petSaveDTO);
        normalizeStatusByStock(petSaveDTO);

        Pet pet = petMapper.selectEntityById(id);
        if (pet == null) {
            throw new BusinessException("Pet does not exist");
        }

        int rows = petMapper.updateById(id, petSaveDTO);
        if (rows < 1) {
            throw new BusinessException("Failed to update pet");
        }
        evictPetCache(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    })
    public boolean removeById(Long id) {
        Pet pet = petMapper.selectEntityById(id);
        if (pet == null) {
            throw new BusinessException("Pet does not exist");
        }
        boolean removed = petMapper.deleteById(id) > 0;
        if (removed) {
            evictPetCache(id);
        }
        return removed;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    })
    public void updateStatus(Long id, PetStatusDTO petStatusDTO) {
        if (petStatusDTO.getStatus() == null) {
            throw new BusinessException("Pet status cannot be empty");
        }

        Pet pet = petMapper.selectEntityById(id);
        if (pet == null) {
            throw new BusinessException("Pet does not exist");
        }

        if (petStatusDTO.getStatus() == 1) {
            ensureCategoryEnabled(pet.getCategoryId());
            if (pet.getStock() == null || pet.getStock() <= 0) {
                throw new BusinessException("Pets with stock less than or equal to 0 cannot be listed");
            }
        }

        int rows = petMapper.updateStatusById(id, petStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("Failed to update pet status");
        }
        evictPetCache(id);
    }

    private boolean tryLock(String key, String threadId) {
        Boolean locked = redisTemplate.opsForValue().setIfAbsent(key, threadId, 10, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(locked);
    }

    private void unlock(String key, String threadId) {
        redisTemplate.execute(UNLOCK_SCRIPT, Collections.singletonList(key), threadId);
    }

    private void sleepBriefly() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException("Failed to get pet detail, please try again later");
        }
    }

    private void validatePetSaveDTO(PetSaveDTO petSaveDTO) {
        if (petSaveDTO.getName() == null || petSaveDTO.getName().isBlank()) {
            throw new BusinessException("Pet name cannot be empty");
        }
        if (petSaveDTO.getCategoryId() == null) {
            throw new BusinessException("Pet category cannot be empty");
        }
        if (petSaveDTO.getBreed() == null || petSaveDTO.getBreed().isBlank()) {
            throw new BusinessException("Pet breed cannot be empty");
        }
        if (petSaveDTO.getAge() == null) {
            throw new BusinessException("Pet age cannot be empty");
        }
        if (petSaveDTO.getPrice() == null) {
            throw new BusinessException("Pet price cannot be empty");
        }
        if (petSaveDTO.getStock() == null) {
            throw new BusinessException("Pet stock cannot be empty");
        }
        if (petSaveDTO.getCoverUrl() == null || petSaveDTO.getCoverUrl().isBlank()) {
            throw new BusinessException("Pet cover cannot be empty");
        }
        if (petSaveDTO.getStatus() == null) {
            throw new BusinessException("Pet status cannot be empty");
        }

        ensureCategoryEnabled(petSaveDTO.getCategoryId());
    }

    private void normalizeStatusByStock(PetSaveDTO petSaveDTO) {
        if (petSaveDTO.getStock() != null && petSaveDTO.getStock() <= 0) {
            petSaveDTO.setStatus(0);
        }
    }

    private void ensureCategoryEnabled(Long categoryId) {
        Category category = categoryMapper.selectEntityById(categoryId);
        if (category == null) {
            throw new BusinessException("Pet category does not exist");
        }
        if (category.getStatus() != null && category.getStatus() == 0) {
            throw new BusinessException("Current category is disabled");
        }
    }

    private void evictPetCache(Long id) {
        if (id != null) {
            redisTemplate.delete("pet:" + id);
        }
    }
}
