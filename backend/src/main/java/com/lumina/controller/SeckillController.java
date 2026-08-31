package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.security.UserContext;
import com.lumina.service.SeckillService;
import com.lumina.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "秒杀模块")
@RestController
@RequestMapping("/api/seckill")
@RequiredArgsConstructor
public class SeckillController {

    private final SeckillService seckillService;

    @Operation(summary = "获取正在进行秒杀的商品列表")
    @GetMapping("/products")
    public Result<List<ProductVO>> products() {
        return Result.success(seckillService.getActiveProducts());
    }

    @Operation(summary = "执行秒杀")
    @PostMapping("/{productId}")
    public Result<Map<String, Object>> seckill(@PathVariable Long productId) {
        int remaining = seckillService.seckill(productId, UserContext.getUserId());
        return Result.success(Map.of("message", "抢购成功", "remaining", remaining));
    }
}
