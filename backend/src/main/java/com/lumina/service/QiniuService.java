package com.lumina.service;

import org.springframework.web.multipart.MultipartFile;

public interface QiniuService {
    String upload(MultipartFile file);
}
