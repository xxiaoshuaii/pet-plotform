package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.pojo.vo.NoticeVO;
import com.pethub.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping
    public Result<List<NoticeVO>> list(@RequestParam(required = false) Integer limit) {
        return Result.success(noticeService.list(limit));
    }

    @GetMapping("/unread-count")
    public Result<Long> unreadCount() {
        return Result.success(noticeService.getUnreadCount());
    }

    @PatchMapping("/{id}/read")
    public Result<Void> readById(@PathVariable Long id) {
        noticeService.readById(id);
        return Result.success();
    }

    @PatchMapping("/read-all")
    public Result<Void> readAll() {
        noticeService.readAll();
        return Result.success();
    }
}
