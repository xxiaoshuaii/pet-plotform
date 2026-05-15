package com.pethub.service.impl;

import cn.hutool.json.JSONUtil;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.pethub.common.cache.DashboardCacheNames;
import com.pethub.common.context.BaseContext;
import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.PostMapper;
import com.pethub.pojo.dto.PostAuditDTO;
import com.pethub.pojo.dto.PostSaveDTO;
import com.pethub.pojo.entity.Post;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PostDetailVO;
import com.pethub.pojo.vo.PostVO;
import com.pethub.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private static final Duration POST_DETAIL_CACHE_TTL = Duration.ofMinutes(30);
    private static final Duration POST_DETAIL_VERSION_TTL = Duration.ofDays(30);

    private final PostMapper postMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public PageResultVO<PostVO> page(PostQuery query) {
        query.setCurrentUserId(BaseContext.getCurrentId());
        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<PostVO> records = postMapper.selectPage(query);
        PageInfo<PostVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    public PostDetailVO getById(Long id) {
        Long currentUserId = BaseContext.getCurrentId();
        long version = getPostDetailCacheVersion(id);
        String key = buildPostDetailCacheKey(id, currentUserId, version);
        String json = stringRedisTemplate.opsForValue().get(key);
        if (json != null) {
            return JSONUtil.toBean(json, PostDetailVO.class);
        }

        PostDetailVO postDetailVO = postMapper.selectDetailById(id, currentUserId);
        if (postDetailVO == null) {
            throw new BusinessException("Post does not exist");
        }

        stringRedisTemplate.opsForValue().set(key, JSONUtil.toJsonStr(postDetailVO), POST_DETAIL_CACHE_TTL);
        return postDetailVO;
    }

    @Override
    public PageResultVO<PostVO> pageByUserId(Long userId, PostQuery query) {
        validateUserId(userId);

        int pageNum = query.getPageNum() == null || query.getPageNum() < 1 ? 1 : query.getPageNum();
        int pageSize = query.getPageSize() == null || query.getPageSize() < 1 ? 10 : query.getPageSize();

        PageHelper.startPage(pageNum, pageSize);
        List<PostVO> records = postMapper.selectPageByUserId(userId);
        PageInfo<PostVO> pageInfo = new PageInfo<>(records);

        return new PageResultVO<>(pageInfo.getList(), pageInfo.getTotal(), pageInfo.getPageNum(), pageInfo.getPageSize());
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_POSTS, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public Long save(Long userId, PostSaveDTO postSaveDTO) {
        validateUserId(userId);
        validatePost(postSaveDTO);

        int rows = postMapper.insert(userId, postSaveDTO);
        if (rows < 1) {
            throw new BusinessException("Failed to create post");
        }
        return postSaveDTO.getId();
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_POSTS, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void updateByUserId(Long userId, Long id, PostSaveDTO postSaveDTO) {
        validateUserId(userId);
        validatePost(postSaveDTO);

        Post current = postMapper.selectEntityByIdAndUserId(id, userId);
        if (current == null) {
            throw new BusinessException("Post does not exist or cannot be modified");
        }

        int rows = postMapper.updateByIdAndUserId(id, userId, postSaveDTO);
        if (rows < 1) {
            throw new BusinessException("Failed to update post");
        }
        evictPostDetailCache(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_POSTS, allEntries = true)
    })
    public void updateStatus(Long id, PostAuditDTO postAuditDTO) {
        if (postAuditDTO.getStatus() == null) {
            throw new BusinessException("Audit status cannot be empty");
        }
        if (postAuditDTO.getStatus() != 1 && postAuditDTO.getStatus() != 2) {
            throw new BusinessException("Illegal audit status");
        }

        Post post = postMapper.selectEntityById(id);
        if (post == null) {
            throw new BusinessException("Post does not exist");
        }

        String rejectReason = postAuditDTO.getStatus() == 2 ? postAuditDTO.getRejectReason() : null;
        int rows = postMapper.updateStatusById(id, postAuditDTO.getStatus(), rejectReason);
        if (rows < 1) {
            throw new BusinessException("Failed to update post audit status");
        }
        evictPostDetailCache(id);
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_POSTS, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public boolean removeById(Long id) {
        Post post = postMapper.selectEntityById(id);
        if (post == null) {
            throw new BusinessException("Post does not exist");
        }
        evictPostDetailCache(id);
        return postMapper.deleteById(id) > 0;
    }

    @Override
    @Caching(evict = {
            @CacheEvict(cacheNames = DashboardCacheNames.OVERVIEW, allEntries = true),
            @CacheEvict(cacheNames = DashboardCacheNames.RECENT_POSTS, allEntries = true)
    })
    @Transactional(rollbackFor = Exception.class)
    public void removeByUserId(Long userId, Long id) {
        validateUserId(userId);

        Post current = postMapper.selectEntityByIdAndUserId(id, userId);
        if (current == null) {
            throw new BusinessException("Post does not exist or cannot be deleted");
        }

        int rows = postMapper.deleteByIdAndUserId(id, userId);
        if (rows < 1) {
            throw new BusinessException("Failed to delete post");
        }
        evictPostDetailCache(id);
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("User is not logged in");
        }
    }

    private void validatePost(PostSaveDTO postSaveDTO) {
        if (postSaveDTO == null) {
            throw new BusinessException("Post payload cannot be empty");
        }
        if (postSaveDTO.getTitle() == null || postSaveDTO.getTitle().isBlank()) {
            throw new BusinessException("Post title cannot be empty");
        }
        if (postSaveDTO.getContent() == null || postSaveDTO.getContent().isBlank()) {
            throw new BusinessException("Post content cannot be empty");
        }
    }

    private String buildPostDetailCacheKey(Long postId, Long currentUserId, long version) {
        return "post:detail:" + postId + ":" + version + ":" + (currentUserId == null ? "guest" : currentUserId);
    }

    private void evictPostDetailCache(Long postId) {
        String versionKey = buildPostDetailVersionKey(postId);
        stringRedisTemplate.opsForValue().increment(versionKey);
        stringRedisTemplate.expire(versionKey, POST_DETAIL_VERSION_TTL);
    }

    private long getPostDetailCacheVersion(Long postId) {
        String version = stringRedisTemplate.opsForValue().get(buildPostDetailVersionKey(postId));
        if (version == null || version.isBlank()) {
            return 0L;
        }
        try {
            return Long.parseLong(version);
        } catch (NumberFormatException ex) {
            return 0L;
        }
    }

    private String buildPostDetailVersionKey(Long postId) {
        return "post:detail:version:" + postId;
    }
}
