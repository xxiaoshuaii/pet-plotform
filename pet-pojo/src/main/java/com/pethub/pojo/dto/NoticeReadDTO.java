package com.pethub.pojo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NoticeReadDTO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private List<Long> ids;
}
