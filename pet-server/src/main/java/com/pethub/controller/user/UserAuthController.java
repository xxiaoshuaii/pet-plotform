package com.pethub.controller.user;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.LoginDTO;
import com.pethub.pojo.vo.UserLoginVO;
import com.pethub.service.UserAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/auth/login")
public class UserAuthController {

    private final UserAuthService userAuthService;

    @PostMapping
    public Result<UserLoginVO> login(@RequestBody LoginDTO loginDTO) {
        return Result.success(userAuthService.login(loginDTO));
    }
}
