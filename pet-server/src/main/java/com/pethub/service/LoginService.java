package com.pethub.service;


import com.pethub.pojo.dto.LoginDTO;
import com.pethub.pojo.vo.LoginVO;

public interface LoginService {
    /**
     * 用户登录
     * @param loginDTO
     * @return
     */
    LoginVO login(LoginDTO loginDTO);
}
