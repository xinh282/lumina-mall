package com.lumina.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.PageResult;
import com.lumina.common.Result;
import com.lumina.dto.ProductQueryDTO;
import com.lumina.service.ProductService;
import com.lumina.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "商品模块", description = "商品列表、详情、搜索、热门推荐")
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "商品分页列表（支持分类筛选、关键词搜索、排序）")
    @GetMapping
    public Result<PageResult<ProductVO>> list(ProductQueryDTO query) {
        IPage<ProductVO> page = productService.pageWithConditions(query);
        return Result.success(PageResult.of(page));
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<ProductVO> detail(@PathVariable Long id) {
        return Result.success(productService.getDetail(id));
    }

    @Operation(summary = "热门商品")
    @GetMapping("/hot")
    public Result<List<ProductVO>> hot(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(productService.getHotProducts(limit));
    }

    @Operation(summary = "新品商品")
    @GetMapping("/new")
    public Result<List<ProductVO>> newProducts(@RequestParam(defaultValue = "8") int limit) {
        return Result.success(productService.getNewProducts(limit));
    }
}
