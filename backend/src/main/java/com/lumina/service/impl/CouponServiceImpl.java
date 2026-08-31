package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumina.common.BusinessException;
import com.lumina.entity.Coupon;
import com.lumina.entity.UserCoupon;
import com.lumina.mapper.CouponMapper;
import com.lumina.mapper.UserCouponMapper;
import com.lumina.service.CouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponMapper couponMapper;
    private final UserCouponMapper userCouponMapper;

    @Override
    @Transactional
    public Coupon create(Coupon coupon) {
        coupon.setUsedCount(0);
        coupon.setStatus(1);
        couponMapper.insert(coupon);
        return coupon;
    }

    @Override
    public IPage<Coupon> adminList(int page, int size) {
        Page<Coupon> p = new Page<>(page, size);
        LambdaQueryWrapper<Coupon> w = new LambdaQueryWrapper<>();
        w.orderByDesc(Coupon::getCreateTime);
        return couponMapper.selectPage(p, w);
    }

    @Override
    public List<Coupon> availableCoupons() {
        LambdaQueryWrapper<Coupon> w = new LambdaQueryWrapper<>();
        w.eq(Coupon::getStatus, 1).apply("used_count < total_count");
        w.orderByDesc(Coupon::getCreateTime);
        return couponMapper.selectList(w);
    }

    @Override
    @Transactional
    public void claim(Long userId, Long couponId) {
        Coupon coupon = couponMapper.selectById(couponId);
        if (coupon == null || coupon.getStatus() == 0) {
            throw new BusinessException("优惠券不存在或已失效");
        }
        if (coupon.getUsedCount() >= coupon.getTotalCount()) {
            throw new BusinessException("优惠券已被领完");
        }

        LambdaQueryWrapper<UserCoupon> w = new LambdaQueryWrapper<>();
        w.eq(UserCoupon::getUserId, userId).eq(UserCoupon::getCouponId, couponId);
        if (userCouponMapper.selectCount(w) > 0) {
            throw new BusinessException("已领取过该优惠券");
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponMapper.updateById(coupon);

        UserCoupon uc = new UserCoupon();
        uc.setUserId(userId);
        uc.setCouponId(couponId);
        uc.setStatus("UNUSED");
        userCouponMapper.insert(uc);
    }

    @Override
    public List<Map<String, Object>> userCoupons(Long userId) {
        LambdaQueryWrapper<UserCoupon> w = new LambdaQueryWrapper<>();
        w.eq(UserCoupon::getUserId, userId).eq(UserCoupon::getStatus, "UNUSED");
        List<UserCoupon> ucs = userCouponMapper.selectList(w);
        LocalDateTime now = LocalDateTime.now();
        List<Map<String, Object>> result = new ArrayList<>();
        for (UserCoupon uc : ucs) {
            Coupon c = couponMapper.selectById(uc.getCouponId());
            if (c == null || c.getStatus() == 0) continue;
            // 检查是否过期
            if (uc.getCreateTime() != null) {
                LocalDateTime expireAt = uc.getCreateTime().plusDays(c.getExpireDays());
                if (now.isAfter(expireAt)) {
                    uc.setStatus("EXPIRED");
                    userCouponMapper.updateById(uc);
                    continue;
                }
            }
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", uc.getId());
            m.put("name", c.getName());
            m.put("type", c.getType());
            m.put("threshold", c.getThreshold());
            m.put("discountValue", c.getDiscountValue());
            m.put("expireDays", c.getExpireDays());
            result.add(m);
        }
        return result;
    }

    @Override
    public BigDecimal calculateDiscount(Long userCouponId, BigDecimal orderAmount) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc == null || !"UNUSED".equals(uc.getStatus())) {
            throw new BusinessException("优惠券不可用");
        }
        Coupon c = couponMapper.selectById(uc.getCouponId());
        if (c == null || c.getStatus() == 0) {
            throw new BusinessException("优惠券已失效");
        }
        // 检查是否过期
        if (uc.getCreateTime() != null) {
            LocalDateTime expireAt = uc.getCreateTime().plusDays(c.getExpireDays());
            if (LocalDateTime.now().isAfter(expireAt)) {
                uc.setStatus("EXPIRED");
                userCouponMapper.updateById(uc);
                throw new BusinessException("优惠券已过期");
            }
        }
        if (orderAmount.compareTo(c.getThreshold()) < 0) {
            throw new BusinessException("未达到使用门槛 ¥" + c.getThreshold());
        }
        if ("FIXED".equals(c.getType())) {
            return c.getDiscountValue();
        } else {
            // discountValue 如 8 表示 8折 = 优惠 20%
            BigDecimal payRate = c.getDiscountValue().divide(BigDecimal.valueOf(10), 2, RoundingMode.HALF_UP);
            BigDecimal discount = orderAmount.multiply(BigDecimal.ONE.subtract(payRate));
            return discount.setScale(2, RoundingMode.HALF_UP);
        }
    }

    @Override
    @Transactional
    public void useCoupon(Long userCouponId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc != null) {
            uc.setStatus("USED");
            uc.setUsedTime(LocalDateTime.now());
            userCouponMapper.updateById(uc);
        }
    }

    @Override
    @Transactional
    public void restoreCoupon(Long userCouponId) {
        UserCoupon uc = userCouponMapper.selectById(userCouponId);
        if (uc != null && "USED".equals(uc.getStatus())) {
            uc.setStatus("UNUSED");
            uc.setUsedTime(null);
            userCouponMapper.updateById(uc);
        }
    }

    @Scheduled(cron = "0 0 3 * * ?")
    @Transactional
    public void expireCoupons() {
        LambdaQueryWrapper<UserCoupon> w = new LambdaQueryWrapper<>();
        w.eq(UserCoupon::getStatus, "UNUSED");
        List<UserCoupon> list = userCouponMapper.selectList(w);
        LocalDateTime now = LocalDateTime.now();
        int count = 0;
        for (UserCoupon uc : list) {
            Coupon c = couponMapper.selectById(uc.getCouponId());
            if (c != null && uc.getCreateTime() != null) {
                if (now.isAfter(uc.getCreateTime().plusDays(c.getExpireDays()))) {
                    uc.setStatus("EXPIRED");
                    userCouponMapper.updateById(uc);
                    count++;
                }
            }
        }
        if (count > 0) log.info("过期优惠券处理完成: {} 张", count);
    }
}
