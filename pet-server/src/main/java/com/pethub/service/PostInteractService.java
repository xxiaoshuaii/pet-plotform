package com.pethub.service;

import com.pethub.pojo.dto.PostCommentSaveDTO;
import com.pethub.pojo.vo.PostCommentVO;

import java.util.List;

public interface PostInteractService {

    boolean toggleLike(Long userId, Long postId);

    List<PostCommentVO> listComments(Long postId);

    void saveComment(Long userId, Long postId, PostCommentSaveDTO postCommentSaveDTO);
}
