package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Integer userCount;
    private Integer petCount;
    private Integer postCount;
    private Integer orderCount;
}
