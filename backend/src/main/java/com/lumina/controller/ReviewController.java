package com.lumina.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.Result;
import com.lumina.entity.Review;
import com.lumina.security.UserContext;
import com.lumina.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "商品评价")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "发表评价")
    @PostMapping
    public Result<Review> create(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        Long productId = Long.valueOf(body.get("productId").toString());
        Integer rating = Integer.valueOf(body.get("rating").toString());
        String content = body.get("content").toString();
        Long orderId = body.containsKey("orderId") && body.get("orderId") != null
                ? Long.valueOf(body.get("orderId").toString()) : null;
        return Result.success(reviewService.create(userId, productId, orderId, rating, content));
    }

    @Operation(summary = "商品评价列表")
    @GetMapping("/product/{productId}")
    public Result<IPage<Review>> list(@PathVariable Long productId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(reviewService.listByProduct(productId, page, size));
    }

    @Operation(summary = "商品评分统计")
    @GetMapping("/product/{productId}/stats")
    public Result<Map<String, Object>> stats(@PathVariable Long productId) {
        return Result.success(reviewService.getRatingStats(productId));
    }

    @Operation(summary = "管理员评价列表")
    @GetMapping("/admin/list")
    public Result<IPage<Review>> adminList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long productId) {
        return Result.success(reviewService.adminList(productId, page, size));
    }

    @Operation(summary = "管理员删除评价")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        reviewService.deleteByAdmin(id);
        return Result.success(null);
    }
}
