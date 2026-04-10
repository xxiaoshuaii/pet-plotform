package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userCount;
    private Integer petCount;
    private Integer postCount;
    private Integer orderCount;
    private List<TrendPointVO> orderTrend;
    private List<PieItemVO> categoryPie;
    private List<RecentOrderVO> recentOrders;
    private List<RecentPostVO> recentPosts;
    private List<NoticeVO> recentNotices;
}
