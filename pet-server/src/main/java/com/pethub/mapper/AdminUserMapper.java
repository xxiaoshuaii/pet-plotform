package com.pethub.mapper;

import com.pethub.pojo.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AdminUserMapper {

    AdminUser selectByUsername(@Param("username") String username);
}
