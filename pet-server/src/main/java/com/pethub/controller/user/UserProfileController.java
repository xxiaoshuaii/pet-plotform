package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.dto.UserProfileUpdateDTO;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端个人资料接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/profile")
public class UserProfileController {

    private final UserService userService;

    /**
     * 获取当前登录用户资料。
     */
    @GetMapping
    public Result<UserDetailVO> profile() {
        return Result.success(userService.getById(BaseContext.getCurrentId()));
    }

    /**
     * 更新当前登录用户资料。
     */
    @PutMapping
    public Result<Void> updateProfile(@RequestBody UserProfileUpdateDTO userProfileUpdateDTO) {
        userService.updateProfile(BaseContext.getCurrentId(), userProfileUpdateDTO);
        return Result.success();
    }
}
