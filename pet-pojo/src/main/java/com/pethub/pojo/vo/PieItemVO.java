package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PieItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private String name;
    private Integer value;
}
