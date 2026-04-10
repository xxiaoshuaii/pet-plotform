package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.OrderStatusDTO;
import com.pethub.pojo.query.OrderQuery;
import com.pethub.pojo.vo.OrderDetailVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 订单管理控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    /**
     * 分页查询订单列表。
     */
    @GetMapping
    public Result<PageResultVO<OrderVO>> page(OrderQuery query) {
        return Result.success(orderService.page(query));
    }

    /**
     * 根据订单 ID 查询详情。
     */
    @GetMapping("/{id}")
    public Result<OrderDetailVO> getById(@PathVariable Long id) {
        return Result.success(orderService.getById(id));
    }

    /**
     * 修改订单状态。
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody OrderStatusDTO orderStatusDTO) {
        orderService.updateStatus(id, orderStatusDTO);
        return Result.success();
    }

    /**
     * 删除订单。
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> removeById(@PathVariable Long id) {
        return Result.success(orderService.removeById(id));
    }
}
