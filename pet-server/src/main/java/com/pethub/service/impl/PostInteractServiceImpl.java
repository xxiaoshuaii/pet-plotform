package com.pethub.service.impl;

import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.PostCommentMapper;
import com.pethub.mapper.PostLikeMapper;
import com.pethub.mapper.PostMapper;
import com.pethub.pojo.dto.PostCommentSaveDTO;
import com.pethub.pojo.entity.Post;
import com.pethub.pojo.entity.PostComment;
import com.pethub.pojo.entity.PostLike;
import com.pethub.pojo.vo.PostCommentVO;
import com.pethub.service.PostInteractService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostInteractServiceImpl implements PostInteractService {

    private static final Duration POST_DETAIL_VERSION_TTL = Duration.ofDays(30);

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long userId, Long postId) {
        validateUserId(userId);
        validatePublicPost(postId);

        String likesKey = "post:likes:" + postId;
        PostLike currentLike = postLikeMapper.selectByPostIdAndUserId(postId, userId);

        if (currentLike != null) {
            postLikeMapper.deleteByPostIdAndUserId(postId, userId);
            postMapper.updateLikeCount(postId, postLikeMapper.countByPostId(postId));
            stringRedisTemplate.opsForSet().remove(likesKey, userId.toString());
            evictPostDetailCache(postId);
            return false;
        }

        postLikeMapper.insert(postId, userId);
        postMapper.updateLikeCount(postId, postLikeMapper.countByPostId(postId));
        stringRedisTemplate.opsForSet().add(likesKey, userId.toString());
        evictPostDetailCache(postId);
        return true;
    }
    @Override
    public List<PostCommentVO> listComments(Long postId) {
        validatePublicPost(postId);

        String commentsKey = buildCommentsKey(postId);
        Set<String> commentIds = stringRedisTemplate.opsForZSet().range(commentsKey, 0, -1);
        if (commentIds == null || commentIds.isEmpty()) {
            List<PostCommentVO> comments = postCommentMapper.selectListByPostId(postId);
            cacheCommentIndex(postId, comments);
            return comments;
        }

        List<Long> orderedIds = commentIds.stream()
                .map(Long::valueOf)
                .toList();
        List<PostCommentVO> comments = postCommentMapper.selectListByIds(orderedIds);
        if (comments.isEmpty()) {
            stringRedisTemplate.delete(commentsKey);
            return List.of();
        }

        Map<Long, PostCommentVO> commentMap = comments.stream()
                .collect(Collectors.toMap(PostCommentVO::getId, comment -> comment, (left, right) -> left, LinkedHashMap::new));

        List<PostCommentVO> orderedComments = new ArrayList<>(orderedIds.size());
        for (Long id : orderedIds) {
            PostCommentVO comment = commentMap.get(id);
            if (comment != null) {
                orderedComments.add(comment);
            }
        }
        return orderedComments;
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveComment(Long userId, Long postId, PostCommentSaveDTO postCommentSaveDTO) {
        validateUserId(userId);
        validatePublicPost(postId);

        if (postCommentSaveDTO == null || postCommentSaveDTO.getContent() == null || postCommentSaveDTO.getContent().isBlank()) {
            throw new BusinessException("Comment content cannot be empty");
        }

        PostComment postComment = new PostComment();
        postComment.setPostId(postId);
        postComment.setUserId(userId);
        postComment.setContent(postCommentSaveDTO.getContent().trim());

        int rows = postCommentMapper.insert(postComment);
        if (rows < 1 || postComment.getId() == null) {
            throw new BusinessException("Failed to publish comment");
        }

        postMapper.updateCommentCount(postId, postCommentMapper.countByPostId(postId));
        stringRedisTemplate.opsForZSet().add(buildCommentsKey(postId), String.valueOf(postComment.getId()), System.currentTimeMillis());
        evictPostDetailCache(postId);
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("User is not logged in or session has expired");
        }
    }

    private void validatePublicPost(Long postId) {
        Post post = postMapper.selectEntityById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("Post does not exist or is not available for interaction");
        }
    }

    private void evictPostDetailCache(Long postId) {
        String versionKey = "post:detail:version:" + postId;
        stringRedisTemplate.opsForValue().increment(versionKey);
        stringRedisTemplate.expire(versionKey, POST_DETAIL_VERSION_TTL);
    }

    private String buildCommentsKey(Long postId) {
        return "post:comments:" + postId;
    }

    private void cacheCommentIndex(Long postId, List<PostCommentVO> comments) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        String commentsKey = buildCommentsKey(postId);
        for (PostCommentVO comment : comments) {
            double score = comment.getCreateTime() == null
                    ? System.currentTimeMillis()
                    : comment.getCreateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
            stringRedisTemplate.opsForZSet().add(commentsKey, String.valueOf(comment.getId()), score);
        }
    }
}
