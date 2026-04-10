package com.pethub.service.impl;

import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.AdminUserMapper;
import com.pethub.pojo.dto.LoginDTO;
import com.pethub.pojo.entity.AdminUser;
import com.pethub.pojo.vo.AdminUserInfoVO;
import com.pethub.pojo.vo.LoginVO;
import com.pethub.service.LoginService;
import com.pethub.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * 登录业务实现类。
 */
@Service
@RequiredArgsConstructor
public class LoginServiceImpl implements LoginService {

    private final AdminUserMapper adminUserMapper;
    private final JwtUtil jwtUtil;

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        // 1. 先根据用户名查询管理员信息。
        AdminUser adminUser = adminUserMapper.selectByUsername(loginDTO.getUsername());
        if (adminUser == null || !adminUser.getPassword().equals(loginDTO.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        // 2. 用户名和密码正确后，生成 JWT token。
        LoginVO loginVO = new LoginVO();
        loginVO.setToken(jwtUtil.createToken(adminUser.getId(), adminUser.getUsername()));

        // 3. 组装前端首页右上角等位置会用到的用户信息。
        AdminUserInfoVO userInfo = new AdminUserInfoVO();
        userInfo.setId(adminUser.getId());
        userInfo.setUsername(adminUser.getUsername());
        userInfo.setNickname(adminUser.getNickname());
        userInfo.setRole("管理员");
        userInfo.setAvatar(adminUser.getAvatar());

        loginVO.setUserInfo(userInfo);
        return loginVO;
    }
}
