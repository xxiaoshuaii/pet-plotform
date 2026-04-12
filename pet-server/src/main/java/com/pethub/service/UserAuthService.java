package com.pethub.service;

import com.pethub.pojo.dto.LoginDTO;
import com.pethub.pojo.vo.UserLoginVO;

public interface UserAuthService {

    UserLoginVO login(LoginDTO loginDTO);
}
