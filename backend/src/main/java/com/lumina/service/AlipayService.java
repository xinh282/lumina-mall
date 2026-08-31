package com.lumina.service;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeQueryModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.config.AlipayConfig;
import com.lumina.entity.Order;
import com.lumina.entity.Payment;
import com.lumina.mapper.OrderMapper;
import com.lumina.mapper.PaymentMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AlipayService {

    private final AlipayClient alipayClient;
    private final AlipayConfig alipayConfig;
    private final OrderMapper orderMapper;
    private final PaymentMapper paymentMapper;
    private final NotificationService notificationService;

    /**
     * 手动确认支付（沙箱/线下场景，管理员或用户手动确认）
     */
    @Transactional
    public void manualConfirm(Long orderId, Long userId) {
        log.info("手动确认支付: orderId={}, userId={}", orderId, userId);
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new RuntimeException("订单不存在: " + orderId);
        if (!order.getUserId().equals(userId)) throw new RuntimeException("订单不属于当前用户");
        if (!"PENDING".equals(order.getStatus())) throw new RuntimeException("订单状态不是待支付: " + order.getStatus());

        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getOrderId, orderId).eq(Payment::getStatus, "PENDING");
        Payment payment = paymentMapper.selectOne(w);
        if (payment != null) {
            payment.setTradeNo("MANUAL_" + System.currentTimeMillis());
            payment.setStatus("SUCCESS");
            payment.setPayTime(LocalDateTime.now());
            paymentMapper.updateById(payment);
            log.info("支付流水更新: paymentId={}", payment.getId());
        }

        order.setStatus("PAID");
        order.setPayTime(LocalDateTime.now());
        orderMapper.updateById(order);
        log.info("订单状态更新为PAID: orderNo={}", order.getOrderNo());

        notificationService.create(userId, "支付成功",
                "订单" + order.getOrderNo() + "已支付 ¥" + order.getTotalAmount(), "ORDER", order.getId());
    }

    /**
     * 主动查询支付状态并更新订单
     */
    public String queryAndUpdateOrder(Long orderId, Long userId) throws AlipayApiException {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) return "订单不存在";
        if (!"PENDING".equals(order.getStatus())) return order.getStatus();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        AlipayTradeQueryModel model = new AlipayTradeQueryModel();
        model.setOutTradeNo(order.getOrderNo());
        request.setBizModel(model);
        AlipayTradeQueryResponse resp = alipayClient.execute(request);

        if (resp.isSuccess() && "TRADE_SUCCESS".equals(resp.getTradeStatus())) {
            // 更新支付流水
            LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
            w.eq(Payment::getOrderNo, order.getOrderNo()).eq(Payment::getStatus, "PENDING");
            Payment payment = paymentMapper.selectOne(w);
            if (payment != null) {
                payment.setTradeNo(resp.getTradeNo());
                payment.setStatus("SUCCESS");
                payment.setPayTime(LocalDateTime.now());
                paymentMapper.updateById(payment);
            }
            // 更新订单
            order.setStatus("PAID");
            order.setPayTime(LocalDateTime.now());
            orderMapper.updateById(order);
            notificationService.create(userId, "支付成功",
                    "订单" + order.getOrderNo() + "已支付 ¥" + order.getTotalAmount(), "ORDER", order.getId());
            return "PAID";
        }
        return "PENDING";
    }

    /**
     * 生成支付宝收银台支付页面（整段HTML）
     */
    public String createPayPage(Long orderId, Long userId) throws AlipayApiException {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new RuntimeException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new RuntimeException("订单状态不可支付");
        }

        // 创建支付流水
        LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
        w.eq(Payment::getOrderId, orderId).eq(Payment::getStatus, "PENDING");
        Payment payment = paymentMapper.selectOne(w);
        if (payment == null) {
            payment = new Payment();
            payment.setOrderId(orderId);
            payment.setOrderNo(order.getOrderNo());
            payment.setAmount(order.getTotalAmount());
            payment.setPayType("ALIPAY");
            payment.setStatus("PENDING");
            paymentMapper.insert(payment);
        }

        AlipayTradePagePayRequest request = new AlipayTradePagePayRequest();
        request.setNotifyUrl(alipayConfig.getNotifyUrl());
        request.setReturnUrl(alipayConfig.getReturnUrl());

        AlipayTradePagePayModel model = new AlipayTradePagePayModel();
        model.setOutTradeNo(order.getOrderNo());
        model.setTotalAmount(order.getTotalAmount().toString());
        model.setSubject("LUMINA 商城订单 - " + order.getOrderNo());
        model.setProductCode("FAST_INSTANT_TRADE_PAY");
        model.setTimeoutExpress("30m");
        request.setBizModel(model);

        return alipayClient.pageExecute(request).getBody();
    }

    /**
     * 处理支付宝异步通知
     */
    @Transactional
    public String handleNotify(Map<String, String> params) {
        try {
            boolean verified = AlipaySignature.rsaCheckV1(params,
                    alipayConfig.getAlipayPublicKey(), "UTF-8", "RSA2");
            if (!verified) return "fail";

            String tradeStatus = params.get("trade_status");
            if (!"TRADE_SUCCESS".equals(tradeStatus)) return "success";

            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("alipay_trade_no");
            BigDecimal amount = new BigDecimal(params.get("total_amount"));

            // 幂等：检查是否已处理
            LambdaQueryWrapper<Payment> w = new LambdaQueryWrapper<>();
            w.eq(Payment::getOrderNo, orderNo).eq(Payment::getStatus, "PENDING");
            Payment payment = paymentMapper.selectOne(w);
            if (payment == null) return "success"; // 已处理

            payment.setTradeNo(tradeNo);
            payment.setStatus("SUCCESS");
            payment.setPayTime(LocalDateTime.now());
            paymentMapper.updateById(payment);

            // 更新订单状态
            LambdaQueryWrapper<Order> ow = new LambdaQueryWrapper<>();
            ow.eq(Order::getOrderNo, orderNo);
            Order order = orderMapper.selectOne(ow);
            if (order != null) {
                order.setStatus("PAID");
                order.setPayTime(LocalDateTime.now());
                orderMapper.updateById(order);
                notificationService.create(order.getUserId(), "支付成功",
                        "订单" + orderNo + "已支付 ¥" + amount, "ORDER", order.getId());
            }

            log.info("支付成功: orderNo={}, tradeNo={}, amount={}", orderNo, tradeNo, amount);
            return "success";
        } catch (Exception e) {
            log.error("支付回调处理失败", e);
            return "fail";
        }
    }
}
