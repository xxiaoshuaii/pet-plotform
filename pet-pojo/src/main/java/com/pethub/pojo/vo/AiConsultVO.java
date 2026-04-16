package com.pethub.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiConsultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    //模型回答的直接结果
    private String answer;
    //模型建议的提示
    private List<String> tips;
    //模型建议的追问
    private List<String> followUps;
    //模型建议的宠物
    private List<PetVO> recommendedPets;
    private String provider;
}
