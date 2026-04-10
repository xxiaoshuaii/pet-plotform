package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrendPointVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String date;
    private Integer value;
}
