package com.lumina.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.common.Result;
import com.lumina.entity.ProductSku;
import com.lumina.mapper.ProductSkuMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商品规格")
@RestController
@RequestMapping("/api/skus")
@RequiredArgsConstructor
public class SkuController {

    private final ProductSkuMapper skuMapper;

    @Operation(summary = "获取商品SKU列表")
    @GetMapping("/product/{productId}")
    public Result<List<ProductSku>> listByProduct(@PathVariable Long productId) {
        LambdaQueryWrapper<ProductSku> w = new LambdaQueryWrapper<>();
        w.eq(ProductSku::getProductId, productId);
        return Result.success(skuMapper.selectList(w));
    }

    @Operation(summary = "新增SKU")
    @PostMapping
    public Result<ProductSku> create(@RequestBody Map<String, Object> body) {
        ProductSku sku = new ProductSku();
        sku.setProductId(Long.valueOf(body.get("productId").toString()));
        sku.setSpecs(body.get("specs").toString());
        if (body.get("price") != null) sku.setPrice(new java.math.BigDecimal(body.get("price").toString()));
        sku.setStock(body.get("stock") != null ? Integer.valueOf(body.get("stock").toString()) : 0);
        sku.setSkuCode(body.get("skuCode") != null ? body.get("skuCode").toString() : "");
        sku.setStatus(1);
        skuMapper.insert(sku);
        return Result.success(sku);
    }

    @Operation(summary = "删除SKU")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        skuMapper.deleteById(id);
        return Result.success(null);
    }
}
