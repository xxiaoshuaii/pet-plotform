package com.pethub.pojo.query;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PetQuery implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private Long categoryId;
    private Integer status;
    private Integer pageNum;
    private Integer pageSize;
}
