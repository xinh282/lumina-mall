package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.service.QiniuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "通用上传")
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class PublicUploadController {

    private final QiniuService qiniuService;

    @Operation(summary = "上传图片（需登录）")
    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.success(qiniuService.upload(file));
    }
}
