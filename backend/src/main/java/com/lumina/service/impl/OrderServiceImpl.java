package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumina.common.BusinessException;
import com.lumina.dto.OrderCreateDTO;
import com.lumina.entity.*;
import com.lumina.mapper.*;
import com.lumina.entity.ProductSku;
import com.lumina.service.CouponService;
import com.lumina.service.NotificationService;
import com.lumina.service.OrderService;
import com.lumina.vo.OrderItemVO;
import com.lumina.util.SnowflakeIdGenerator;
import com.lumina.vo.OrderVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final SnowflakeIdGenerator idGenerator;
    private final NotificationService notificationService;
    private final CouponService couponService;
    private final ProductSkuMapper skuMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderVO createOrder(Long userId, OrderCreateDTO dto) {
        List<Cart> cartItems = cartMapper.selectBatchIds(dto.getCartItemIds());
        if (cartItems.isEmpty()) throw new BusinessException("购物车项不存在");

        for (Cart cart : cartItems) {
            if (!cart.getUserId().equals(userId)) throw new BusinessException("无效的购物车项");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        for (Cart cart : cartItems) {
            Product product = productMapper.selectById(cart.getProductId());
            if (product == null || product.getStatus() == 0) {
                throw new BusinessException("商品「" + (product != null ? product.getName() : "未知") + "」已下架");
            }
            if (product.getStock() < cart.getQuantity()) {
                throw new BusinessException("商品「" + product.getName() + "」库存不足");
            }
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity()));
            totalAmount = totalAmount.add(itemTotal);

            OrderItem item = new OrderItem();
            item.setProductId(product.getId());
            item.setProductName(product.getName());
            item.setProductPrice(product.getPrice());
            item.setQuantity(cart.getQuantity());
            item.setTotalPrice(itemTotal);
            orderItems.add(item);
        }

        // 优惠券处理
        BigDecimal couponDiscount = BigDecimal.ZERO;
        if (dto.getUserCouponId() != null) {
            couponDiscount = couponService.calculateDiscount(dto.getUserCouponId(), totalAmount);
            totalAmount = totalAmount.subtract(couponDiscount);
        }

        // ① 先原子扣库存（防并发超卖），优先扣 SKU 库存
        for (Cart cart : cartItems) {
            int affected;
            if (cart.getSkuId() != null) {
                affected = skuMapper.deductStock(cart.getSkuId(), cart.getQuantity());
            } else {
                affected = productMapper.deductStock(cart.getProductId(), cart.getQuantity());
            }
            if (affected == 0) {
                throw new BusinessException("库存不足，下单失败");
            }
            productMapper.incrementSales(cart.getProductId(), cart.getQuantity());
        }

        // ② 再创建订单
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setUserId(userId);
        order.setTotalAmount(totalAmount);
        order.setCouponDiscount(couponDiscount);
        order.setUserCouponId(dto.getUserCouponId());
        order.setStatus("PENDING");
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        orderMapper.insert(order);

        for (OrderItem item : orderItems) {
            item.setOrderId(order.getId());
            orderItemMapper.insert(item);
        }

        // ③ 清理购物车
        for (Cart cart : cartItems) {
            cartMapper.deleteById(cart.getId());
        }

        redisTemplate.delete("product:detail:" + cartItems.get(0).getProductId());
        redisTemplate.delete("product:hot:8");
        redisTemplate.delete("product:new:8");

        notificationService.create(userId, "订单创建成功",
                "订单" + order.getOrderNo() + "已创建，金额¥" + order.getTotalAmount(), "ORDER", order.getId());

        if (dto.getUserCouponId() != null) {
            couponService.useCoupon(dto.getUserCouponId());
        }

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        return vo;
    }

    @Override
    public IPage<OrderVO> listOrders(Long userId, String status, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Order::getUserId, userId);
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
        return orderPage.convert(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            return vo;
        });
    }

    @Override
    public OrderVO getDetail(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderItems(items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).toList());
        return vo;
    }

    @Override
    @Transactional
    public void cancelOrder(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待支付订单可取消");
        }
        order.setStatus("CANCELLED");
        orderMapper.updateById(order);

        // 恢复优惠券
        if (order.getUserCouponId() != null) {
            couponService.restoreCoupon(order.getUserCouponId());
        }

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);
        for (OrderItem item : items) {
            productMapper.restoreStock(item.getProductId(), item.getQuantity());
        }
    }

    @Override
    @Transactional
    public void confirmReceipt(Long orderId, Long userId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null || !order.getUserId().equals(userId)) {
            throw new BusinessException("订单不存在");
        }
        if (!"SHIPPED".equals(order.getStatus())) {
            throw new BusinessException("仅已发货订单可确认收货");
        }
        order.setStatus("RECEIVED");
        orderMapper.updateById(order);

        notificationService.create(userId, "已确认收货",
                "订单" + order.getOrderNo() + "已确认收货", "ORDER", orderId);
    }

    private String generateOrderNo() {
        return idGenerator.nextOrderNo();
    }

    @Override
    public IPage<OrderVO> adminList(Long userId, String status, String orderNo, int page, int size) {
        Page<Order> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Order> wrapper = new LambdaQueryWrapper<>();
        if (userId != null) {
            wrapper.eq(Order::getUserId, userId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(Order::getStatus, status);
        }
        if (orderNo != null && !orderNo.isEmpty()) {
            wrapper.eq(Order::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(Order::getCreateTime);
        IPage<Order> orderPage = orderMapper.selectPage(pageParam, wrapper);
        return orderPage.convert(order -> {
            OrderVO vo = new OrderVO();
            BeanUtils.copyProperties(order, vo);
            return vo;
        });
    }

    @Override
    public OrderVO adminGetDetail(Long orderId) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");

        LambdaQueryWrapper<OrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(OrderItem::getOrderId, orderId);
        List<OrderItem> items = orderItemMapper.selectList(wrapper);

        OrderVO vo = new OrderVO();
        BeanUtils.copyProperties(order, vo);
        vo.setOrderItems(items.stream().map(item -> {
            OrderItemVO itemVO = new OrderItemVO();
            BeanUtils.copyProperties(item, itemVO);
            return itemVO;
        }).toList());
        return vo;
    }

    @Override
    @Transactional
    public void adminUpdateStatus(Long orderId, String status, String trackingNo, String logisticsCompany) {
        Order order = orderMapper.selectById(orderId);
        if (order == null) throw new BusinessException("订单不存在");
        order.setStatus(status);
        if ("PAID".equals(status)) {
            order.setPayTime(LocalDateTime.now());
        }
        if ("SHIPPED".equals(status) && trackingNo != null) {
            order.setTrackingNo(trackingNo);
            order.setLogisticsCompany(logisticsCompany != null ? logisticsCompany : "");
        }
        orderMapper.updateById(order);

        String statusText = switch (status) {
            case "PAID" -> "已支付";
            case "SHIPPED" -> "已发货";
            case "RECEIVED" -> "已完成";
            default -> status;
        };
        notificationService.create(order.getUserId(), "订单状态更新",
                "订单" + order.getOrderNo() + "状态变更为: " + statusText, "ORDER", orderId);
    }
}
