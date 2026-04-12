package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
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
}
