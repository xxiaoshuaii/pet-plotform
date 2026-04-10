package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.dto.PostAuditDTO;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PostDetailVO;
import com.pethub.pojo.vo.PostVO;
import com.pethub.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 帖子管理控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostService postService;

    /**
     * 分页查询帖子列表。
     */
    @GetMapping
    public Result<PageResultVO<PostVO>> page(PostQuery query) {
        return Result.success(postService.page(query));
    }

    /**
     * 根据帖子 ID 查询详情。
     */
    @GetMapping("/{id}")
    public Result<PostDetailVO> getById(@PathVariable Long id) {
        return Result.success(postService.getById(id));
    }

    /**
     * 审核帖子。
     */
    @PatchMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody PostAuditDTO postAuditDTO) {
        postService.updateStatus(id, postAuditDTO);
        return Result.success();
    }

    /**
     * 删除帖子。
     */
    @DeleteMapping("/{id}")
    public Result<Boolean> removeById(@PathVariable Long id) {
        return Result.success(postService.removeById(id));
    }
}
