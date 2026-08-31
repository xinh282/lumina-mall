package com.lumina.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.Result;
import com.lumina.entity.Coupon;
import com.lumina.service.CouponService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "管理-优惠券")
@RestController
@RequestMapping("/api/admin/coupons")
@RequiredArgsConstructor
public class AdminCouponController {

    private final CouponService couponService;

    @Operation(summary = "优惠券列表")
    @GetMapping
    public Result<IPage<Coupon>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(couponService.adminList(page, size));
    }

    @Operation(summary = "创建优惠券")
    @PostMapping
    public Result<Coupon> create(@RequestBody Coupon coupon) {
        return Result.success(couponService.create(coupon));
    }
}
