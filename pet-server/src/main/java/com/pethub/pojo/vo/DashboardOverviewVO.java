package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardOverviewVO {

    private Long userCount;
    private Long petCount;
    private Long postCount;
    private Long orderCount;
    private Long petOnlineCount;
    private Long pendingPostCount;
    private Long todayOrderCount;
}