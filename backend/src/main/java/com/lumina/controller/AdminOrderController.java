package com.lumina.controller;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.PageResult;
import com.lumina.common.Result;
import com.lumina.service.OrderService;
import com.lumina.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "管理-订单")
@RestController
@RequestMapping("/api/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final OrderService orderService;

    @Operation(summary = "全部订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String orderNo,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<OrderVO> result = orderService.adminList(userId, status, orderNo, page, size);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.adminGetDetail(id));
    }

    @Operation(summary = "更新订单状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        orderService.adminUpdateStatus(id, body.get("status"),
                body.get("trackingNo"), body.get("logisticsCompany"));
        return Result.success(null);
    }
}
