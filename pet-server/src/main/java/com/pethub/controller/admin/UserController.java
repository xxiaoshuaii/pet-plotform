package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.UserStatusDTO;
import com.pethub.pojo.query.UserQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.UserDetailVO;
import com.pethub.pojo.vo.UserVO;
import com.pethub.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户管理控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    /**
     * 分页查询用户列表。
     */
    @GetMapping
    public Result<PageResultVO<UserVO>> page(UserQuery query) {
        return Result.success(userService.page(query));
    }

    /**
     * 根据用户 ID 查询用户详情。
     */
    @GetMapping("/{id}")
    public Result<UserDetailVO> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    /**
     * 启用或禁用用户。
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody UserStatusDTO userStatusDTO) {
        userService.updateStatus(id, userStatusDTO);
        return Result.success();
    }
}
