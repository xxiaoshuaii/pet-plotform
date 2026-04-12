package com.pethub.controller.user;

import com.pethub.common.context.BaseContext;
import com.pethub.common.result.Result;
import com.pethub.pojo.dto.PostCommentSaveDTO;
import com.pethub.pojo.vo.PostCommentVO;
import com.pethub.service.PostInteractService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 用户端帖子互动接口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/user/posts")
public class UserPostInteractController {

    private final PostInteractService postInteractService;

    /**
     * 点赞或取消点赞。
     */
    @PostMapping("/{id}/like")
    public Result<Boolean> toggleLike(@PathVariable Long id) {
        return Result.success(postInteractService.toggleLike(BaseContext.getCurrentId(), id));
    }

    /**
     * 查询帖子评论列表。
     */
    @GetMapping("/{id}/comments")
    public Result<List<PostCommentVO>> comments(@PathVariable Long id) {
        return Result.success(postInteractService.listComments(id));
    }

    /**
     * 发表评论。
     */
    @PostMapping("/{id}/comments")
    public Result<Void> saveComment(@PathVariable Long id, @RequestBody PostCommentSaveDTO postCommentSaveDTO) {
        postInteractService.saveComment(BaseContext.getCurrentId(), id, postCommentSaveDTO);
        return Result.success();
    }
}
