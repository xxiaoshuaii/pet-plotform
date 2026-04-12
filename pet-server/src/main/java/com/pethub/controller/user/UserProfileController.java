package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端个人中心接口。
 * 这里提供当前登录用户的基础资料，给前端个人中心直接展示使用。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserService userService;

    /**
     * 获取当前登录用户资料。
     * 用户身份由 JWT 拦截器解析后放入上下文，这里直接读取当前用户 ID。
     */
    @GetMapping
    public Result<UserDetailVO> profile() {
        return Result.success(userService.getById(BaseContext.getCurrentId()));
    }
}
