package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.security.UserContext;
import com.lumina.service.AlipayService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Tag(name = "支付模块")
@RestController
@RequestMapping("/api/pay")
@RequiredArgsConstructor
public class PaymentController {

    private final AlipayService alipayService;

    @Operation(summary = "创建支付（返回支付宝收银台HTML页面）")
    @PostMapping("/create")
    public Result<Map<String, String>> create(@RequestBody Map<String, Long> body) {
        try {
            Long orderId = body.get("orderId");
            String html = alipayService.createPayPage(orderId, UserContext.getUserId());
            Map<String, String> result = new HashMap<>();
            result.put("payForm", html);
            return Result.success(result);
        } catch (Exception e) {
            return Result.error(500, e.getMessage());
        }
    }

    @Operation(summary = "主动查询支付状态")
    @PostMapping("/query")
    public Result<String> query(@RequestBody Map<String, Long> body) {
        try {
            // 先尝试主动查询支付宝
            String status = alipayService.queryAndUpdateOrder(body.get("orderId"), UserContext.getUserId());
            if ("PAID".equals(status)) return Result.success("PAID");
        } catch (Exception ignored) {}
        return Result.success("PENDING");
    }

    @Operation(summary = "确认支付（沙箱/线下支付后手动确认）")
    @PostMapping("/confirm")
    public Result<String> confirm(@RequestBody Map<String, Long> body) {
        Long orderId = body.get("orderId");
        Long userId = UserContext.getUserId();
        log.info("支付确认请求: orderId={}, userId={}", orderId, userId);
        try {
            String status = alipayService.queryAndUpdateOrder(orderId, userId);
            if ("PAID".equals(status)) return Result.success("PAID");
        } catch (Exception e) {
            log.warn("支付宝查询失败, 走手动确认: {}", e.getMessage());
        }
        alipayService.manualConfirm(orderId, userId);
        return Result.success("PAID");
    }

    @Operation(summary = "支付宝异步通知（无需认证）")
    @PostMapping("/notify")
    public String notify(HttpServletRequest request) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));
        return alipayService.handleNotify(params);
    }
}
