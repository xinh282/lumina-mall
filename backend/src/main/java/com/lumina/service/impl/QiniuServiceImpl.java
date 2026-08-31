package com.lumina.service.impl;

import com.lumina.common.BusinessException;
import com.lumina.config.QiniuConfig;
import com.lumina.service.QiniuService;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.util.Auth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QiniuServiceImpl implements QiniuService {

    private final QiniuConfig qiniuConfig;

    @Override
    public String upload(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("文件不能为空");
        }

        Auth auth = Auth.create(qiniuConfig.getAccessKey(), qiniuConfig.getSecretKey());
        String uploadToken = auth.uploadToken(qiniuConfig.getBucket());

        String originalName = file.getOriginalFilename();
        String suffix = "";
        if (StringUtils.hasText(originalName) && originalName.contains(".")) {
            suffix = originalName.substring(originalName.lastIndexOf("."));
        }
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String key = "products/" + datePath + "/" + UUID.randomUUID().toString().replace("-", "") + suffix;

        Configuration cfg = new Configuration();
        UploadManager uploadManager = new UploadManager(cfg);
        try {
            uploadManager.put(file.getBytes(), key, uploadToken);
        } catch (IOException e) {
            throw new BusinessException("图片上传失败");
        }

        // 正确拼接，不加多余斜杠，不重复
        return qiniuConfig.getDomain() + "/" + key;
    }
}
