package com.lumina.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.Result;
import com.lumina.entity.Refund;
import com.lumina.security.UserContext;
import com.lumina.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "退款")
@RestController
@RequestMapping("/api/refunds")
@RequiredArgsConstructor
public class RefundController {

    private final RefundService refundService;

    @Operation(summary = "申请退款")
    @PostMapping
    public Result<Refund> apply(@RequestBody Map<String, Object> body) {
        Long userId = UserContext.getUserId();
        Long orderId = Long.valueOf(body.get("orderId").toString());
        String reason = body.get("reason").toString();
        return Result.success(refundService.apply(userId, orderId, reason));
    }

    @Operation(summary = "我的退款列表")
    @GetMapping
    public Result<IPage<Refund>> myList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(refundService.userList(UserContext.getUserId(), page, size));
    }
}
