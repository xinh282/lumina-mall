package com.lumina.service;

import com.lumina.entity.*;
import com.lumina.mapper.*;
import com.lumina.util.SnowflakeIdGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillOrderProcessor {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;
    private final SnowflakeIdGenerator idGenerator;
    private final NotificationService notificationService;

    private static final String ORDER_QUEUE_KEY = "seckill:orders";

    @Scheduled(fixedDelay = 200)
    @Transactional(rollbackFor = Exception.class)
    public void processSeckillOrders() {
        Object msg = redisTemplate.opsForList().rightPop(ORDER_QUEUE_KEY);
        if (msg == null) return;

        String[] parts = msg.toString().split(":");
        Long userId = Long.valueOf(parts[0]);
        Long productId = Long.valueOf(parts[1]);

        try {
            // 原子扣减：WHERE seckill_stock > 0，一行 SQL 搞定
            int affected = productMapper.deductSeckillStock(productId);
            if (affected == 0) {
                log.info("秒杀库存已耗尽，跳过: userId={}, productId={}", userId, productId);
                return;
            }

            // 扣减成功后读取商品信息创建订单
            Product product = productMapper.selectById(productId);
            if (product == null) return;

            BigDecimal price = product.getSeckillPrice() != null ? product.getSeckillPrice() : product.getPrice();

            Order order = new Order();
            order.setOrderNo(generateOrderNo());
            order.setUserId(userId);
            order.setTotalAmount(price);
            order.setStatus("PAID");
            order.setPayTime(LocalDateTime.now());
            orderMapper.insert(order);

            OrderItem item = new OrderItem();
            item.setOrderId(order.getId());
            item.setProductId(product.getId());
            item.setProductName(product.getName() + "【秒杀】");
            item.setProductPrice(price);
            item.setQuantity(1);
            item.setTotalPrice(price);
            orderItemMapper.insert(item);

            log.info("秒杀订单创建成功: orderNo={}, userId={}, productId={}", order.getOrderNo(), userId, productId);

            notificationService.create(userId, "秒杀成功",
                    "恭喜您抢到「" + product.getName() + "」秒杀价 ¥" + price, "ORDER", order.getId());
        } catch (Exception e) {
            log.error("秒杀异步下单异常: userId={}, productId={}", userId, productId, e);
            // 重新入队，等下轮重试
            redisTemplate.opsForList().leftPush(ORDER_QUEUE_KEY, msg);
        }
    }

    @Scheduled(fixedDelay = 15000)
    public void cleanStaleQueue() {
        // 每 15 秒清理一次：检查队列里的消息对应商品是否还有库存，没有就清掉
        Long size = redisTemplate.opsForList().size(ORDER_QUEUE_KEY);
        if (size == null || size == 0) return;

        // 只检查队尾最近一条，判断对应商品库存
        Object msg = redisTemplate.opsForList().index(ORDER_QUEUE_KEY, -1);
        if (msg == null) return;

        String[] parts = msg.toString().split(":");
        Long productId = Long.valueOf(parts[1]);
        Product product = productMapper.selectById(productId);
        if (product == null || product.getSeckillStock() <= 0) {
            // 库存没了，清空队列中该商品的所有消息
            long cleaned = 0;
            while (true) {
                Object m = redisTemplate.opsForList().rightPop(ORDER_QUEUE_KEY);
                if (m == null) break;
                if (!m.toString().contains(":" + productId)) {
                    // 其他商品的，放回去
                    redisTemplate.opsForList().rightPush(ORDER_QUEUE_KEY, m);
                    break;
                }
                cleaned++;
            }
            if (cleaned > 0) {
                log.info("秒杀队列清理: productId={}, 清除 {} 条过期消息", productId, cleaned);
            }
        }
    }

    private String generateOrderNo() {
        return "SK" + idGenerator.nextOrderNo();
    }
}
