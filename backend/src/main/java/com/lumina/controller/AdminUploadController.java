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

@Tag(name = "管理-文件上传")
@RestController
@RequestMapping("/api/admin/upload")
@RequiredArgsConstructor
public class AdminUploadController {

    private final QiniuService qiniuService;

    @Operation(summary = "上传图片到七牛云")
    @PostMapping
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        String url = qiniuService.upload(file);
        return Result.success(url);
    }
}
