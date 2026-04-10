package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.CategorySaveDTO;
import com.pethub.pojo.dto.CategoryStatusDTO;
import com.pethub.pojo.query.CategoryQuery;
import com.pethub.pojo.vo.CategoryVO;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.service.CategoryService;
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
 * 分类管理控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/categories")
public class CategoryController {

    private final CategoryService categoryService;

    /**
     * 分页查询分类列表。
     */
    @GetMapping
    public Result<PageResultVO<CategoryVO>> page(CategoryQuery query) {
        return Result.success(categoryService.page(query));
    }

    /**
     * 新增分类。
     */
    @PostMapping
    public Result<Void> save(@RequestBody CategorySaveDTO categorySaveDTO) {
        categoryService.save(categorySaveDTO);
        return Result.success();
    }

    /**
     * 编辑分类。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody CategorySaveDTO categorySaveDTO) {
        categoryService.update(id, categorySaveDTO);
        return Result.success();
    }

    /**
     * 删除分类。
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> removeById(@PathVariable Long id) {
        return Result.success(categoryService.removeById(id));
    }

    /**
     * 更新分类状态。
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody CategoryStatusDTO categoryStatusDTO) {
        categoryService.updateStatus(id, categoryStatusDTO);
        return Result.success();
    }
}
