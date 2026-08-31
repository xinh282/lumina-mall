package com.lumina;

import com.lumina.entity.Product;
import com.lumina.mapper.ProductMapper;
import com.lumina.service.SeckillService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class SeckillConcurrencyTest {

    @Autowired
    private SeckillService seckillService;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    private static final String STOCK_KEY = "seckill:stock:";
    private static final String USER_KEY = "seckill:user:";

    private Long testProductId;

    @BeforeEach
    void setUp() {
        // 查找或创建测试秒杀商品
        Product p = productMapper.selectById(9999L);
        if (p == null) {
            p = new Product();
            p.setId(9999L);
            p.setName("并发测试商品");
            p.setCategoryId(1L);
            p.setPrice(new BigDecimal("100"));
            p.setStock(1000);
            p.setStatus(1);
            p.setSeckillPrice(new BigDecimal("1"));
            p.setSeckillStock(1000);
            p.setSeckillStart(LocalDateTime.now().minusHours(1));
            p.setSeckillEnd(LocalDateTime.now().plusHours(1));
            productMapper.insert(p);
        } else {
            p.setSeckillStock(1000);
            p.setSeckillStart(LocalDateTime.now().minusHours(1));
            p.setSeckillEnd(LocalDateTime.now().plusHours(1));
            productMapper.updateById(p);
        }
        testProductId = 9999L;

        // 清理 Redis 旧数据
        redisTemplate.delete(STOCK_KEY + testProductId);
        redisTemplate.delete(redisTemplate.keys(USER_KEY + testProductId + ":*"));
    }

    @Test
    @DisplayName("100库存 200并发 — 验证不超卖")
    void test100Stock200Threads() throws Exception {
        runConcurrencyTest(100, 200);
    }

    @Test
    @DisplayName("100库存 500并发 — 极限压测")
    void test100Stock500Threads() throws Exception {
        runConcurrencyTest(100, 500);
    }

    @Test
    @DisplayName("10库存 1000并发 — 极端抢购")
    void test10Stock1000Threads() throws Exception {
        runConcurrencyTest(10, 1000);
    }

    @Test
    @DisplayName("100库存 5000并发 — 极限涌入")
    void test100Stock5000Threads() throws Exception {
        runConcurrencyTest(100, 5000);
    }

    private void runConcurrencyTest(int stock, int threads) throws Exception {
        // 1. 初始化 Redis 秒杀库存
        redisTemplate.opsForValue().set(STOCK_KEY + testProductId, stock);
        // 更新商品表 seckillStock
        Product p = productMapper.selectById(testProductId);
        p.setSeckillStock(stock);
        productMapper.updateById(p);

        // 2. 线程池 + 倒计时门闩（所有线程同时出发）
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(threads, 500));
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger soldOutCount = new AtomicInteger(0);
        AtomicInteger duplicateCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        AtomicLong maxLatency = new AtomicLong(0);
        AtomicLong totalLatency = new AtomicLong(0);

        // 3. 提交所有任务
        for (int i = 0; i < threads; i++) {
            final long userId = 100000L + i;
            executor.submit(() -> {
                try {
                    startLatch.await(); // 等待发令枪
                    long t1 = System.nanoTime();
                    seckillService.seckill(testProductId, userId);
                    long t2 = System.nanoTime();
                    successCount.incrementAndGet();
                    long latency = (t2 - t1) / 1_000_000;
                    totalLatency.addAndGet(latency);
                    maxLatency.updateAndGet(v -> Math.max(v, latency));
                } catch (Exception e) {
                    String msg = e.getMessage();
                    if (msg != null && msg.contains("售罄")) {
                        soldOutCount.incrementAndGet();
                    } else if (msg != null && msg.contains("已抢购")) {
                        duplicateCount.incrementAndGet();
                    } else {
                        errorCount.incrementAndGet();
                    }
                } finally {
                    doneLatch.countDown();
                }
            });
        }

        // 4. 发令！所有线程同时开始
        long startTime = System.currentTimeMillis();
        startLatch.countDown();
        doneLatch.await();
        long endTime = System.currentTimeMillis();
        executor.shutdown();

        // 5. 验证 Redis 库存归零
        Object remaining = redisTemplate.opsForValue().get(STOCK_KEY + testProductId);
        int redisStock = remaining != null ? Integer.parseInt(remaining.toString()) : -1;

        // 6. 输出报告
        System.out.println("\n========================================");
        System.out.println("  秒杀并发测试报告");
        System.out.println("========================================");
        System.out.println("配置: " + stock + " 库存 / " + threads + " 并发线程");
        System.out.println("----------------------------------------");
        System.out.printf("成功抢购:   %d\n", successCount.get());
        System.out.printf("售罄拒绝:   %d\n", soldOutCount.get());
        System.out.printf("重复抢购:   %d\n", duplicateCount.get());
        System.out.printf("其他错误:   %d\n", errorCount.get());
        System.out.printf("Redis剩余:  %d\n", redisStock);
        System.out.println("----------------------------------------");
        System.out.printf("总耗时:     %d ms\n", endTime - startTime);
        System.out.printf("平均延迟:   %.1f ms\n",
            successCount.get() > 0 ? (double) totalLatency.get() / successCount.get() : 0);
        System.out.printf("最大延迟:   %d ms\n", maxLatency.get());
        System.out.printf("TPS:        %.0f\n",
            (double) threads / ((endTime - startTime) / 1000.0));
        System.out.println("========================================\n");

        // 7. 断言
        assertEquals(stock, successCount.get(), "成功数应等于库存数，不超卖");
        assertEquals(0, redisStock, "Redis库存应归零");
        assertEquals(0, errorCount.get(), "不应有其他错误");
    }
}
