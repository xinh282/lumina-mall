package com.lumina.config;

import com.lumina.service.SeckillService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SeckillInitializer implements ApplicationRunner {

    private final SeckillService seckillService;

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化秒杀商品库存到Redis...");
        seckillService.initSeckillStock();
        log.info("秒杀商品库存初始化完成");
    }
}
