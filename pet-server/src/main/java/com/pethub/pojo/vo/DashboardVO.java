package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO {

    private Long userCount;
    private Long petCount;
    private Long postCount;
    private Long orderCount;
    private Long petOnlineCount;
    private Long pendingPostCount;
    private Long todayOrderCount;
    private List<OrderTrendItemVO> orderTrend;
    private List<CategoryPieItemVO> categoryPie;
    private List<OrderVO> recentOrders;
    private List<PostVO> recentPosts;
    private List<NoticeVO> notices;
}
