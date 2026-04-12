package com.pethub.mapper;

import com.pethub.pojo.dto.UserProfileUpdateDTO;
import com.pethub.pojo.dto.UserStatusDTO;
import com.pethub.pojo.entity.User;
import com.pethub.pojo.query.UserQuery;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.pojo.vo.UserVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface UserMapper {

    List<UserVO> selectPage(UserQuery query);

    User selectEntityByUsernameOrPhone(@Param("keyword") String keyword);

    UserDetailVO selectById(@Param("id") Long id);

    int updateProfileById(@Param("id") Long id, @Param("profile") UserProfileUpdateDTO userProfileUpdateDTO);

    int updateStatusById(@Param("id") Long id, @Param("status") Integer status);
}
