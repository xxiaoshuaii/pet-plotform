package com.pethub.mapper;

import com.pethub.pojo.dto.PostCommentSaveDTO;
import com.pethub.pojo.entity.PostComment;
import com.pethub.pojo.vo.PostCommentVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostCommentMapper {

    List<PostCommentVO> selectListByPostId(@Param("postId") Long postId);

    List<PostCommentVO> selectListByIds(@Param("ids") List<Long> ids);

    int insert(PostComment postComment);

    int countByPostId(@Param("postId") Long postId);

}
