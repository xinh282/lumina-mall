package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.entity.Coupon;
import com.lumina.security.UserContext;
import com.lumina.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Tag(name = "优惠券")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "可领取的优惠券列表")
    @GetMapping("/available")
    public Result<List<Coupon>> available() {
        return Result.success(couponService.availableCoupons());
    }

    @Operation(summary = "领取优惠券")
    @PostMapping("/{couponId}/claim")
    public Result<Void> claim(@PathVariable Long couponId) {
        couponService.claim(UserContext.getUserId(), couponId);
        return Result.success(null);
    }

    @Operation(summary = "我的优惠券")
    @GetMapping
    public Result<List<Map<String, Object>>> myCoupons() {
        return Result.success(couponService.userCoupons(UserContext.getUserId()));
    }

    @Operation(summary = "计算优惠金额")
    @GetMapping("/{id}/discount")
    public Result<Map<String, Object>> calcDiscount(
            @PathVariable Long id,
            @RequestParam BigDecimal amount) {
        BigDecimal discount = couponService.calculateDiscount(id, amount);
        return Result.success(Map.of("discount", discount, "finalAmount", amount.subtract(discount)));
    }
}
