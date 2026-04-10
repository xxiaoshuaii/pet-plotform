package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.LoginDTO;
import com.pethub.pojo.vo.LoginVO;
import com.pethub.service.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 登录控制器。
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/login")
public class LoginController {

    private final LoginService loginService;

    @PostMapping
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        // 接收前端登录参数，调用业务层完成登录。
        log.info("用户登录：{}", loginDTO);
        return Result.success(loginService.login(loginDTO));
    }
}
