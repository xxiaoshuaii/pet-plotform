package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.dto.PostSaveDTO;
import com.pethub.pojo.query.PostQuery;
import com.pethub.pojo.vo.PageResultVO;
import com.pethub.pojo.vo.PostVO;
import com.pethub.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端帖子接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/posts")
public class UserPostController {

    private final PostService postService;

    /**
     * 查询当前登录用户自己的帖子列表。
     */
    @GetMapping("/my")
    public Result<PageResultVO<PostVO>> myPosts(PostQuery query) {
        return Result.success(postService.pageByUserId(BaseContext.getCurrentId(), query));
    }

    /**
     * 发布帖子。
     */
    @PostMapping
    public Result<Long> save(@RequestBody PostSaveDTO postSaveDTO) {
        return Result.success(postService.save(BaseContext.getCurrentId(), postSaveDTO));
    }

    /**
     * 修改当前用户自己的帖子。
     */
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody PostSaveDTO postSaveDTO) {
        postService.updateByUserId(BaseContext.getCurrentId(), id, postSaveDTO);
        return Result.success();
    }

    /**
     * 删除当前用户自己的帖子。
     */
    @DeleteMapping("/{id}")
    public Result<Void> remove(@PathVariable Long id) {
        postService.removeByUserId(BaseContext.getCurrentId(), id);
        return Result.success();
    }
}
