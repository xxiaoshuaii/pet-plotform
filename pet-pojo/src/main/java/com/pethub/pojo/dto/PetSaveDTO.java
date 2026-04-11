package com.pethub.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetSaveDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Long categoryId;
    private String breed;
    private Integer age;
    private String gender;
    private BigDecimal price;
    private Integer stock;
    private String coverUrl;
    private String description;
    private String healthStatus;
    private String vaccineInfo;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
