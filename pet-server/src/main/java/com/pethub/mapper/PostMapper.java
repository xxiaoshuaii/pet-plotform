package com.pethub.mapper;

import com.pethub.pojo.dto.PostSaveDTO;
import com.pethub.pojo.entity.Post;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.vo.PostDetailVO;
import com.pethub.pojo.vo.PostVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {

    List<PostVO> selectPage(PostQuery query);

    List<PostVO> selectPageByUserId(@Param("userId") Long userId);

    PostDetailVO selectDetailById(@Param("id") Long id, @Param("currentUserId") Long currentUserId);

    Post selectEntityById(@Param("id") Long id);

    Post selectEntityByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

    int insert(@Param("userId") Long userId, @Param("post") PostSaveDTO postSaveDTO);

    int updateByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId, @Param("post") PostSaveDTO postSaveDTO);

    int updateLikeCount(@Param("id") Long id, @Param("likeCount") Integer likeCount);

    int updateCommentCount(@Param("id") Long id, @Param("commentCount") Integer commentCount);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("rejectReason") String rejectReason);

    int deleteById(@Param("id") Long id);

    int deleteByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);
}
