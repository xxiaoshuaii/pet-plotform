package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.dto.OrderCreateDTO;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.vo.OrderCreateVO;
import com.pethub.pojo.vo.OrderDetailVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端订单接口。
 * 这里封装“我的订单”查询，避免前端自己传用户名造成越权风险。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/orders")
public class UserOrderController {

    private final OrderService orderService;

    /**
     * 查询当前登录用户的订单列表。
     * 用户名从登录上下文中获取，前端只需要传分页和可选的状态条件。
     */
    @GetMapping("/my")
    public Result<PageResultVO<OrderVO>> myOrders(OrderQuery query) {
        query.setUsername(BaseContext.getCurrentUsername());
        return Result.success(orderService.page(query));
    }

    /**
     * 创建订单。
     * 当前登录用户从上下文读取，前端只需要提交宠物和联系人信息。
     */
    @PostMapping
    public Result<OrderCreateVO> create(@RequestBody OrderCreateDTO orderCreateDTO) {
        return Result.success(orderService.create(BaseContext.getCurrentId(), orderCreateDTO));
    }

    @GetMapping("/{id}")
    public Result<OrderDetailVO> getById(@PathVariable Long id) {
        return Result.success(orderService.getByIdForUser(BaseContext.getCurrentId(), id));
    }

    @PatchMapping("/{id}/pay")
    public Result<Void> pay(@PathVariable Long id) {
        orderService.pay(BaseContext.getCurrentId(), id);
        return Result.success();
    }

    @PatchMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        orderService.cancelForUser(BaseContext.getCurrentId(), id);
        return Result.success();
    }
}
