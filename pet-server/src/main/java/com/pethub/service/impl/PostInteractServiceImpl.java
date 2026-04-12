package com.pethub.service.impl;

import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.PostCommentMapper;
import com.pethub.mapper.PostLikeMapper;
import com.pethub.mapper.PostMapper;
import com.pethub.pojo.dto.PostCommentSaveDTO;
import com.pethub.pojo.entity.Post;
import com.pethub.pojo.entity.PostLike;
import com.pethub.pojo.vo.PostCommentVO;
import com.pethub.service.PostInteractService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostInteractServiceImpl implements PostInteractService {

    private final PostMapper postMapper;
    private final PostLikeMapper postLikeMapper;
    private final PostCommentMapper postCommentMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean toggleLike(Long userId, Long postId) {
        validateUserId(userId);
        validatePublicPost(postId);

        PostLike current = postLikeMapper.selectByPostIdAndUserId(postId, userId);
        if (current == null) {
            postLikeMapper.insert(postId, userId);
            postMapper.updateLikeCount(postId, postLikeMapper.countByPostId(postId));
            return true;
        }

        postLikeMapper.deleteByPostIdAndUserId(postId, userId);
        postMapper.updateLikeCount(postId, postLikeMapper.countByPostId(postId));
        return false;
    }

    @Override
    public List<PostCommentVO> listComments(Long postId) {
        validatePublicPost(postId);
        return postCommentMapper.selectListByPostId(postId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveComment(Long userId, Long postId, PostCommentSaveDTO postCommentSaveDTO) {
        validateUserId(userId);
        validatePublicPost(postId);
        if (postCommentSaveDTO == null || postCommentSaveDTO.getContent() == null || postCommentSaveDTO.getContent().isBlank()) {
            throw new BusinessException("评论内容不能为空");
        }

        int rows = postCommentMapper.insert(postId, userId, postCommentSaveDTO);
        if (rows < 1) {
            throw new BusinessException("发表评论失败");
        }

        postMapper.updateCommentCount(postId, postCommentMapper.countByPostId(postId));
    }

    private void validateUserId(Long userId) {
        if (userId == null) {
            throw new BusinessException("未登录或登录已失效");
        }
    }

    private void validatePublicPost(Long postId) {
        Post post = postMapper.selectEntityById(postId);
        if (post == null || post.getStatus() == null || post.getStatus() != 1) {
            throw new BusinessException("帖子不存在或暂不可互动");
        }
    }
}
