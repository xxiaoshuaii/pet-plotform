package com.pethub.pojo.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notice implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String type;
    private String title;
    private String content;
    private Integer isRead;
    private Long orderId;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer isDeleted;
}
