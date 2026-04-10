package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDetailVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String coverUrl;
    private String title;
    private String content;
    private String username;
    private Integer likeCount;
    private Integer commentCount;
    private Integer status;
    private String rejectReason;
    private LocalDateTime createTime;
}
