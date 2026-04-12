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
public class AddressVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private Long id;
    private String contactName;
    private String contactPhone;
    private String province;
    private String city;
    private String district;
    private String detailAddress;
    private String tag;
    private Integer isDefault;
    private LocalDateTime createTime;
}
