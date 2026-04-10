package com.pethub.pojo.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String orderNo;
    private String username;
    private String petName;
    private Integer status;
    private String createTime;
    private Integer pageNum;
    private Integer pageSize;
}
