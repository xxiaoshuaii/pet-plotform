package com.pethub.utils;

import com.pethub.mapper.CategoryMapper;
import com.pethub.pojo.entity.Category;
import com.pethub.pojo.query.PetQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PetVO;
import com.pethub.service.PetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class PetUtill {

    private static final int TOOL_PAGE_SIZE = 5;

    private final PetService petService;
    private final CategoryMapper categoryMapper;

    @Tool(description = "根据宠物ID查询宠物详情")
    public String getPetInfo(Long petId) {
        log.info("AI tool invoked: getPetInfo, petId={}", petId);
        PetVO pet = petService.getById(petId);
        if (pet == null) {
            return "未找到对应宠物信息。";
        }
        return formatPetDetail(pet);
    }

    @Tool(description = "查询平台当前是否有某类宠物可浏览或交易辅助，例如猫、猫咪、狗、金毛、英短等")
    public String searchAvailablePets(String keyword) {
        log.info("AI tool invoked: searchAvailablePets, keyword={}", keyword);
        String normalizedKeyword = normalizeKeyword(keyword);
        if (normalizedKeyword.isEmpty()) {
            return "未提供有效的宠物关键词。";
        }

        Long categoryId = findCategoryId(normalizedKeyword);
        PageResultVO<PetVO> result;
        if (categoryId != null) {
            result = petService.page(new PetQuery(null, categoryId, 1, 1, TOOL_PAGE_SIZE));
        } else {
            result = petService.page(new PetQuery(normalizedKeyword, null, 1, 1, TOOL_PAGE_SIZE));
        }

        List<PetVO> records = result.getRecords();
        if (records == null || records.isEmpty()) {
            return "当前未查询到与“" + normalizedKeyword + "”相关的在架宠物信息。";
        }

        StringBuilder builder = new StringBuilder();
        builder.append("已查询到与“").append(normalizedKeyword).append("”相关的在架宠物，共 ")
                .append(result.getTotal() == null ? records.size() : result.getTotal())
                .append(" 条，以下展示前 ").append(records.size()).append(" 条：");
        for (PetVO pet : records) {
            builder.append("\n- ID: ").append(pet.getId())
                    .append("，名称: ").append(defaultText(pet.getName()))
                    .append("，分类: ").append(defaultText(pet.getCategoryName()))
                    .append("，品种: ").append(defaultText(pet.getBreed()))
                    .append("，价格: ").append(pet.getPrice() == null ? "待定" : pet.getPrice())
                    .append("，库存: ").append(pet.getStock() == null ? "未知" : pet.getStock());
        }
        return builder.toString();
    }

    private Long findCategoryId(String keyword) {
        Category category = categoryMapper.selectByName(keyword);
        if (category == null) {
            return null;
        }
        if (category.getStatus() != null && category.getStatus() == 0) {
            return null;
        }
        return category.getId();
    }

    private String normalizeKeyword(String keyword) {
        String value = keyword == null ? "" : keyword.trim();
        if (value.isEmpty()) {
            return "";
        }
        if (value.contains("猫")) {
            return "猫";
        }
        if (value.contains("狗") || value.contains("犬")) {
            return "狗";
        }
        return value;
    }

    private String formatPetDetail(PetVO pet) {
        return "宠物ID: " + pet.getId()
                + "，名称: " + defaultText(pet.getName())
                + "，分类: " + defaultText(pet.getCategoryName())
                + "，品种: " + defaultText(pet.getBreed())
                + "，年龄: " + (pet.getAge() == null ? "未知" : pet.getAge())
                + "，性别: " + defaultText(pet.getGender())
                + "，价格: " + (pet.getPrice() == null ? "待定" : pet.getPrice())
                + "，健康情况: " + defaultText(pet.getHealthStatus())
                + "，免疫情况: " + defaultText(pet.getVaccineInfo());
    }

    private String defaultText(String value) {
        return value == null || value.isBlank() ? "未知" : value;
    }
}
