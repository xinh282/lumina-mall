package com.lumina.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.entity.Coupon;
import com.lumina.entity.UserCoupon;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface CouponService {
    Coupon create(Coupon coupon);
    IPage<Coupon> adminList(int page, int size);
    List<Coupon> availableCoupons();
    void claim(Long userId, Long couponId);
    List<Map<String, Object>> userCoupons(Long userId);
    BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount);
    void useCoupon(Long userCouponId);
    void restoreCoupon(Long userCouponId);
}
