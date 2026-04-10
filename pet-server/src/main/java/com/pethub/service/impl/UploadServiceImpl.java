package com.pethub.service.impl;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.pethub.common.exception.BusinessException;
import com.pethub.properties.AliOssProperties;
import com.pethub.service.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * 文件上传服务实现。
 * 当前使用阿里云 OSS 存储图片。
 */
@Service
@RequiredArgsConstructor
public class UploadServiceImpl implements UploadService {

    private final AliOssProperties aliOssProperties;

    @Override
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("上传文件不能为空");
        }

        validateConfig();

        String originalFilename = file.getOriginalFilename();
        String suffix = getFileSuffix(originalFilename);
        String folder = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String objectName = "uploads/" + folder + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        OSS ossClient = null;
        try (InputStream inputStream = file.getInputStream()) {
            ossClient = new OSSClientBuilder().build(
                    aliOssProperties.getEndpoint(),
                    aliOssProperties.getAccessKeyId(),
                    aliOssProperties.getAccessKeySecret()
            );

            ossClient.putObject(aliOssProperties.getBucketName(), objectName, inputStream);
            return buildFileUrl(objectName);
        } catch (IOException e) {
            throw new BusinessException("读取上传文件失败");
        } catch (Exception e) {
            throw new BusinessException("文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }

    private void validateConfig() {
        if (isBlank(aliOssProperties.getEndpoint())
                || isBlank(aliOssProperties.getBucketName())
                || isBlank(aliOssProperties.getRegion())
                || isBlank(aliOssProperties.getAccessKeyId())
                || isBlank(aliOssProperties.getAccessKeySecret())) {
            throw new BusinessException("阿里云OSS配置不完整");
        }
    }

    private String buildFileUrl(String objectName) {
        return "https://" + aliOssProperties.getBucketName() + ".oss-" + aliOssProperties.getRegion() + ".aliyuncs.com/" + objectName;
    }

    private String getFileSuffix(String originalFilename) {
        if (originalFilename == null || !originalFilename.contains(".")) {
            return "";
        }
        return originalFilename.substring(originalFilename.lastIndexOf("."));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
