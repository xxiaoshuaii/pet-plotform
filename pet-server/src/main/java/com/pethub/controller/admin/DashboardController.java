package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.vo.CategoryPieItemVO;
import com.pethub.pojo.vo.DashboardOverviewVO;
import com.pethub.pojo.vo.NoticeVO;
import com.pethub.pojo.vo.OrderTrendItemVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PostVO;
import com.pethub.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/overview")
    public Result<DashboardOverviewVO> getOverview() {
        return Result.success(dashboardService.getOverview());
    }

    @GetMapping("/order-trend")
    public Result<List<OrderTrendItemVO>> getOrderTrend() {
        return Result.success(dashboardService.getOrderTrend());
    }

    @GetMapping("/category-pie")
    public Result<List<CategoryPieItemVO>> getCategoryPie() {
        return Result.success(dashboardService.getCategoryPie());
    }

    @GetMapping("/recent-orders")
    public Result<List<OrderVO>> getRecentOrders() {
        return Result.success(dashboardService.getRecentOrders());
    }

    @GetMapping("/recent-posts")
    public Result<List<PostVO>> getRecentPosts() {
        return Result.success(dashboardService.getRecentPosts());
    }

    @GetMapping("/notices")
    public Result<List<NoticeVO>> getLatestNotices() {
        return Result.success(dashboardService.getLatestNotices());
    }
}