package com.pethub.service;

import com.pethub.pojo.dto.UserProfileUpdateDTO;
import com.pethub.pojo.dto.UserStatusDTO;
import com.pethub.pojo.query.UserQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.pojo.vo.UserVO;

public interface UserService {

    PageResultVO<UserVO> page(UserQuery query);

    UserDetailVO getById(Long id);

    void updateProfile(Long id, UserProfileUpdateDTO userProfileUpdateDTO);

    void updateStatus(Long id, UserStatusDTO userStatusDTO);
}
