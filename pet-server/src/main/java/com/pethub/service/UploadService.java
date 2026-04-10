package com.pethub.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    /**
     * 上传文件并返回可访问地址。
     */
    String upload(MultipartFile file);
}
