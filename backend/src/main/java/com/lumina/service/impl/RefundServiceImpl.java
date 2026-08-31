package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumina.common.BusinessException;
import com.lumina.entity.*;
import com.lumina.mapper.*;
import com.lumina.service.CouponService;
import com.lumina.service.NotificationService;
import com.lumina.service.RefundService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RefundServiceImpl implements RefundService {

    private final RefundMapper refundMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final ProductMapper productMapper;
    private final NotificationService notificationService;
    private final CouponService couponService;

    @Override
    @Transactional
    public Refund apply(Long userId, Long orderId, String reason) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"RECEIVED".equals(order.getStatus())) {
            throw new BusinessException("仅已收货订单可申请退款");
        }

        LambdaQueryWrapper<Refund> existCheck = new LambdaQueryWrapper<>();
        existCheck.eq(Refund::getOrderId, orderId)
                  .ne(Refund::getStatus, "REJECTED");
        if (refundMapper.selectCount(existCheck) > 0) {
            throw new BusinessException("该订单已有退款申请在处理中");
        }

        Refund refund = new Refund();
        refund.setOrderId(orderId);
        refund.setUserId(userId);
        refund.setReason(reason);
        refund.setAmount(order.getTotalAmount());
        refund.setStatus("PENDING");
        refundMapper.insert(refund);

        notificationService.create(userId, "退款申请已提交",
                "订单" + order.getOrderNo() + "的退款申请已提交，请等待审核", "REFUND", refund.getId());
        return refund;
    }

    @Override
    public IPage<Refund> userList(Long userId, int page, int size) {
        Page<Refund> p = new Page<>(page, size);
        LambdaQueryWrapper<Refund> w = new LambdaQueryWrapper<>();
        w.eq(Refund::getUserId, userId).orderByDesc(Refund::getCreateTime);
        return refundMapper.selectPage(p, w);
    }

    @Override
    public IPage<Refund> adminList(String status, int page, int size) {
        Page<Refund> p = new Page<>(page, size);
        LambdaQueryWrapper<Refund> w = new LambdaQueryWrapper<>();
        if (status != null && !status.isEmpty()) w.eq(Refund::getStatus, status);
        w.orderByDesc(Refund::getCreateTime);
        return refundMapper.selectPage(p, w);
    }

    @Override
    @Transactional
    public void approve(Long id, String adminNote) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null || !"PENDING".equals(refund.getStatus())) {
            throw new BusinessException("退款申请状态异常");
        }
        refund.setStatus("APPROVED");
        refund.setAdminNote(adminNote);
        refundMapper.updateById(refund);

        Order order = orderMapper.selectById(refund.getOrderId());
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);

        LambdaQueryWrapper<OrderItem> itemW = new LambdaQueryWrapper<>();
        itemW.eq(OrderItem::getOrderId, refund.getOrderId());
        List<OrderItem> items = orderItemMapper.selectList(itemW);
        for (OrderItem item : items) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        }

        // 退优惠券
        if (order.getUserCouponId() != null) {
            couponService.restoreCoupon(order.getUserCouponId());
        }

        notificationService.create(refund.getUserId(), "退款申请已通过",
                "您的退款申请已通过审核，金额¥" + refund.getAmount() + "将退回", "REFUND", refund.getId());
    }

    @Override
    public void reject(Long id, String adminNote) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null || !"PENDING".equals(refund.getStatus())) {
            throw new BusinessException("退款申请状态异常");
        }
        refund.setStatus("REJECTED");
        refund.setAdminNote(adminNote);
        refundMapper.updateById(refund);

        notificationService.create(refund.getUserId(), "退款申请未通过",
                "您的退款申请未通过审核: " + adminNote, "REFUND", refund.getId());
    }

    @Override
    @Transactional
    public void confirmRefunded(Long id) {
        Refund refund = refundMapper.selectById(id);
        if (refund == null || !"APPROVED".equals(refund.getStatus())) {
            throw new BusinessException("退款单状态异常");
        }
        refund.setStatus("REFUNDED");
        refundMapper.updateById(refund);

        notificationService.create(refund.getUserId(), "退款已到账",
                "您的退款¥" + refund.getAmount() + "已退回，请查收", "REFUND", refund.getId());
    }
}
