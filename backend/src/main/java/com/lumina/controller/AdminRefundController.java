package com.lumina.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.Result;
import com.lumina.entity.Refund;
import com.lumina.service.RefundService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理-退款")
@RestController
@RequestMapping("/api/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final RefundService refundService;

    @Operation(summary = "退款申请列表")
    @GetMapping
    public Result<IPage<Refund>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(refundService.adminList(status, page, size));
    }

    @Operation(summary = "通过退款申请")
    @PutMapping("/{id}/approve")
    public Result<Void> approve(@PathVariable Long id, @RequestBody Map<String, String> body) {
        refundService.approve(id, body.getOrDefault("adminNote", ""));
        return Result.success(null);
    }

    @Operation(summary = "拒绝退款申请")
    @PutMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody Map<String, String> body) {
        refundService.reject(id, body.getOrDefault("adminNote", ""));
        return Result.success(null);
    }

    @Operation(summary = "确认退款到账")
    @PutMapping("/{id}/refunded")
    public Result<Void> confirmRefunded(@PathVariable Long id) {
        refundService.confirmRefunded(id);
        return Result.success(null);
    }
}
