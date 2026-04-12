package com.pethub.mapper;

import com.pethub.pojo.dto.PostCommentSaveDTO;
import com.pethub.pojo.vo.PostCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper {

    List<PostCommentVO> selectListByPostId(@Param("postId") Long postId);

    int insert(@Param("postId") Long postId, @Param("userId") Long userId, @Param("comment") PostCommentSaveDTO postCommentSaveDTO);

    int countByPostId(@Param("postId") Long postId);
}
