package com.pethub.mapper;

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

    PostDetailVO selectDetailById(@Param("id") Long id);

    Post selectEntityById(@Param("id") Long id);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status, @Param("rejectReason") String rejectReason);

    int deleteById(@Param("id") Long id);
}
