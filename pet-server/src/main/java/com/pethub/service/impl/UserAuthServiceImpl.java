package com.pethub.service.impl;

import com.pethub.common.exception.BusinessException;
import com.pethub.mapper.UserMapper;
import com.pethub.pojo.dto.LoginDTO;
import com.pethub.pojo.entity.User;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.pojo.vo.UserLoginVO;
import com.pethub.service.UserAuthService;
import com.pethub.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAuthServiceImpl implements UserAuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    @Override
    public UserLoginVO login(LoginDTO loginDTO) {
        if (loginDTO == null || loginDTO.getUsername() == null || loginDTO.getUsername().isBlank()) {
            throw new BusinessException("用户名或手机号不能为空");
        }
        if (loginDTO.getPassword() == null || loginDTO.getPassword().isBlank()) {
            throw new BusinessException("密码不能为空");
        }

        User user = userMapper.selectEntityByUsernameOrPhone(loginDTO.getUsername());
        if (user == null || !loginDTO.getPassword().equals(user.getPassword())) {
            throw new BusinessException("用户名/手机号或密码错误");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException("当前账号已被禁用");
        }

        UserDetailVO userInfo = new UserDetailVO(
                user.getId(),
                user.getAvatar(),
                user.getUsername(),
                user.getPhone(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getIntro(),
                user.getCreateTime()
        );

        return new UserLoginVO(jwtUtil.createUserToken(user.getId(), user.getUsername()), userInfo);
    }
}
