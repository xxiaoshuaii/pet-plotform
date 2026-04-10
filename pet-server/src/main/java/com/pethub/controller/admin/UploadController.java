package com.pethub.controller.admin;

import com.pethub.common.result.Result;
import com.pethub.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传控制器。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/upload")
public class UploadController {

    private final UploadService uploadService;

    /**
     * 上传图片或其他文件，返回访问地址。
     */
    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(uploadService.upload(file));
    }
}
