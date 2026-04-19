package com.pethub.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.cache.DashboardCacheNames;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.CategoryMapper;
import com.pethub.mapper.PetMapper;
import com.pethub.pojo.dto.CategorySaveDTO;
import com.pethub.pojo.dto.CategoryStatusDTO;
import com.pethub.pojo.entity.Category;
import com.pethub.pojo.query.CategoryQuery;
import com.pethub.pojo.vo.CategoryVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;
    private final PetMapper petMapper;
    private final StringRedisTemplate redisTemplate;

    @Override
    public PageResultVO<CategoryVO> page(CategoryQuery query) {
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 5 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<CategoryVO> records = categoryMapper.selectPage(query);
        PageInfo<CategoryVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(
                pageInfo.getList(),
                pageInfo.getTotal(),
                pageInfo.getPageNum(),
                pageInfo.getPageSize()
        );
    }

    @Override
    @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    public void save(CategorySaveDTO categorySaveDTO) {
        validateCategory(categorySaveDTO);

        Category category = categoryMapper.selectByName(categorySaveDTO.getName());
        if (category != null && category.getIsDeleted() != null && category.getIsDeleted() == 0) {
            throw new BusinessException("Category name already exists");
        }

        int rows = categoryMapper.insert(categorySaveDTO);
        if (rows < 1) {
            throw new BusinessException("Failed to create category");
        }
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    })
    public void update(Long id, CategorySaveDTO categorySaveDTO) {
        validateCategory(categorySaveDTO);

        Category current = categoryMapper.selectEntityById(id);
        if (current == null) {
            throw new BusinessException("Category does not exist");
        }

        Category sameNameCategory = categoryMapper.selectByName(categorySaveDTO.getName());
        if (sameNameCategory != null
                && !sameNameCategory.getId().equals(id)
                && sameNameCategory.getIsDeleted() != null
                && sameNameCategory.getIsDeleted() == 0) {
            throw new BusinessException("Category name already exists");
        }

        int rows = categoryMapper.updateById(id, categorySaveDTO);
        if (rows < 1) {
            throw new BusinessException("Failed to update category");
        }

        if (categorySaveDTO.getStatus() == 0) {
            petMapper.batchOffShelfByCategoryId(id);
            evictPetCacheByCategoryId(id);
        }
    }

    @Override
    @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    public boolean removeById(Long id) {
        Category current = categoryMapper.selectEntityById(id);
        if (current == null) {
            throw new BusinessException("Category does not exist");
        }
        return categoryMapper.deleteById(id) > 0;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.CATEGORY_PIE, allEntries = true)
    })
    public void updateStatus(Long id, CategoryStatusDTO categoryStatusDTO) {
        if (categoryStatusDTO.getStatus() == null) {
            throw new BusinessException("Category status cannot be empty");
        }

        Category current = categoryMapper.selectEntityById(id);
        if (current == null) {
            throw new BusinessException("Category does not exist");
        }

        int rows = categoryMapper.updateStatusById(id, categoryStatusDTO.getStatus());
        if (rows < 1) {
            throw new BusinessException("Failed to update category status");
        }

        if (categoryStatusDTO.getStatus() == 0) {
            petMapper.batchOffShelfByCategoryId(id);
            evictPetCacheByCategoryId(id);
        }
    }

    private void validateCategory(CategorySaveDTO categorySaveDTO) {
        if (categorySaveDTO.getName() == null || categorySaveDTO.getName().isBlank()) {
            throw new BusinessException("Category name cannot be empty");
        }
        if (categorySaveDTO.getStatus() == null) {
            throw new BusinessException("Category status cannot be empty");
        }
    }

    private void evictPetCacheByCategoryId(Long categoryId) {
        List<Long> petIds = petMapper.selectIdsByCategoryId(categoryId);
        if (petIds == null || petIds.isEmpty()) {
            return;
        }
        redisTemplate.delete(petIds.stream().map(id -> "pet:" + id).toList());
    }
}
