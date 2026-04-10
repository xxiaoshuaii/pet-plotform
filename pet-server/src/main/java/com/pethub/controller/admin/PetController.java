package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.PetSaveDTO;
import com.pethub.pojo.dto.PetStatusDTO;
import com.pethub.pojo.query.PetQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PetVO;
import com.pethub.service.PetService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 宠物管理控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/pet")
public class PetController {

    private final PetService petService;

    /**
     * 分页查询宠物列表。
     */
    @GetMapping
    public Result<PageResultVO<PetVO>> page(PetQuery query) {
        return Result.success(petService.page(query));
    }

    /**
     * 新增宠物。
     */
    @PostMapping
    public Result<Void> save(@RequestBody PetSaveDTO petSaveDTO) {
        petService.save(petSaveDTO);
        return Result.success();
    }

    /**
     * 编辑宠物。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PetSaveDTO petSaveDTO) {
        petService.update(id, petSaveDTO);
        return Result.success();
    }

    /**
     * 删除宠物。
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> removeById(@PathVariable Long id) {
        return Result.success(petService.removeById(id));
    }

    /**
     * 更新宠物上架状态。
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody PetStatusDTO petStatusDTO) {
        petService.updateStatus(id, petStatusDTO);
        return Result.success();
    }
}
