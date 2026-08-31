package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.common.BusinessException;
import com.lumina.entity.Product;
import com.lumina.mapper.ProductMapper;
import com.lumina.service.SeckillService;
import com.lumina.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeckillServiceImpl implements SeckillService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final DefaultRedisScript<Long> seckillScript;

    private static final String STOCK_KEY_PREFIX = "seckill:stock:";
    private static final String USER_KEY_PREFIX = "seckill:user:";
    private static final String ORDER_QUEUE_KEY = "seckill:orders";

    @Override
    public void initSeckillStock() {
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        w.eq(Product::getStatus, 1).gt(Product::getSeckillStock, 0)
         .isNotNull(Product::getSeckillPrice)
         .le(Product::getSeckillStart, LocalDateTime.now())
         .ge(Product::getSeckillEnd, LocalDateTime.now());
        List<Product> products = productMapper.selectList(w);
        for (Product p : products) {
            String key = STOCK_KEY_PREFIX + p.getId();
            redisTemplate.opsForValue().set(key, p.getSeckillStock());
            log.info("秒杀预热: productId={}, stock={}", p.getId(), p.getSeckillStock());
        }
    }

    @Override
    public List<ProductVO> getActiveProducts() {
        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        w.eq(Product::getStatus, 1).gt(Product::getSeckillStock, 0)
         .isNotNull(Product::getSeckillPrice)
         .le(Product::getSeckillStart, LocalDateTime.now())
         .ge(Product::getSeckillEnd, LocalDateTime.now());
        List<Product> products = productMapper.selectList(w);
        List<ProductVO> vos = new ArrayList<>();
        for (Product p : products) {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(p, vo);
            // 从 Redis 读取实时库存
            String key = STOCK_KEY_PREFIX + p.getId();
            Object redisStock = redisTemplate.opsForValue().get(key);
            if (redisStock != null) {
                vo.setSeckillStock(Integer.parseInt(redisStock.toString()));
            }
            vos.add(vo);
        }
        return vos;
    }

    @Override
    public int seckill(Long productId, Long userId) {
        // 校验秒杀活动时间
        Product product = productMapper.selectById(productId);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }
        if (product.getSeckillPrice() == null || product.getSeckillStock() <= 0) {
            throw new BusinessException("该商品暂无秒杀活动");
        }
        LocalDateTime now = LocalDateTime.now();
        if (product.getSeckillStart() != null && now.isBefore(product.getSeckillStart())) {
            throw new BusinessException("秒杀活动尚未开始");
        }
        if (product.getSeckillEnd() != null && now.isAfter(product.getSeckillEnd())) {
            throw new BusinessException("秒杀活动已结束");
        }

        String stockKey = STOCK_KEY_PREFIX + productId;
        String userKey = USER_KEY_PREFIX + productId + ":" + userId;

        Long result = redisTemplate.execute(seckillScript, List.of(stockKey, userKey));

        if (result == null) throw new BusinessException("秒杀活动异常");
        if (result == -1) throw new BusinessException("商品已售罄");
        if (result == -2) throw new BusinessException("您已抢购过该商品");

        String orderMsg = userId + ":" + productId;
        redisTemplate.opsForList().leftPush(ORDER_QUEUE_KEY, orderMsg);
        log.info("秒杀成功: userId={}, productId={}, remaining={}", userId, productId, result);

        return result.intValue();
    }

    @Scheduled(fixedDelay = 30000)
    public void refreshSeckillStock() {
        initSeckillStock();
    }
}
