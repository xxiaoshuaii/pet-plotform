package com.pethub.mapper;

import com.pethub.pojo.entity.PostLike;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface PostLikeMapper {

    PostLike selectByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    int insert(@Param("postId") Long postId, @Param("userId") Long userId);

    int deleteByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    int countByPostId(@Param("postId") Long postId);
}
