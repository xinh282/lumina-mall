package com.lumina.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.PageResult;
import com.lumina.common.Result;
import com.lumina.dto.ProductSaveDTO;
import com.lumina.entity.Product;
import com.lumina.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理-商品")
@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    @Operation(summary = "商品分页列表")
    @GetMapping
    public Result<PageResult<Product>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keyword) {
        IPage<Product> result = productService.adminPage(page, size, keyword);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<Product> detail(@PathVariable Long id) {
        return Result.success(productService.adminGetById(id));
    }

    @Operation(summary = "创建商品")
    @PostMapping
    public Result<Void> create(@Valid @RequestBody ProductSaveDTO dto) {
        productService.create(dto);
        return Result.success(null);
    }

    @Operation(summary = "更新商品")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ProductSaveDTO dto) {
        productService.update(id, dto);
        return Result.success(null);
    }

    @Operation(summary = "切换上下架状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        productService.updateStatus(id, status);
        return Result.success(null);
    }
}
