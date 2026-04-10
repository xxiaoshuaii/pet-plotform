package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.vo.NoticeVO;
import com.pethub.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 通知中心控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    /**
     * 查询全部通知。
     */
    @GetMapping
    public Result<List<NoticeVO>> list() {
        return Result.success(noticeService.list());
    }

    /**
     * 查询未读通知数量。
     */
    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(noticeService.getUnreadCount());
    }

    /**
     * 标记单条通知为已读。
     */
    @PatchMapping("/{id}/read")
    public Result<Void> readById(@PathVariable Long id) {
        noticeService.readById(id);
        return Result.success();
    }

    /**
     * 全部标记为已读。
     */
    @PatchMapping("/read-all")
    public Result<Void> readAll() {
        noticeService.readAll();
        return Result.success();
    }
}
