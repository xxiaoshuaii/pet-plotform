package com.pethub.service;

import com.pethub.pojo.vo.CategoryPieItemVO;
import com.pethub.pojo.vo.DashboardOverviewVO;
import com.pethub.pojo.vo.NoticeVO;
import com.pethub.pojo.vo.OrderTrendItemVO;
import com.pethub.pojo.vo.OrderVO;
import com.pethub.pojo.vo.PostVO;

import java.util.List;

public interface DashboardService {

    DashboardOverviewVO getOverview();

    List<OrderTrendItemVO> getOrderTrend();

    List<CategoryPieItemVO> getCategoryPie();

    List<OrderVO> getRecentOrders();

    List<PostVO> getRecentPosts();

    List<NoticeVO> getLatestNotices();
}