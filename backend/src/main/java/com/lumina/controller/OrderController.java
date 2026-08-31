package com.lumina.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.common.PageResult;
import com.lumina.common.Result;
import com.lumina.dto.OrderCreateDTO;
import com.lumina.security.UserContext;
import com.lumina.service.OrderService;
import com.lumina.vo.OrderVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "订单模块", description = "订单创建、查询、取消")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(summary = "创建订单")
    @PostMapping
    public Result<OrderVO> create(@Valid @RequestBody OrderCreateDTO dto) {
        return Result.success(orderService.createOrder(UserContext.getUserId(), dto));
    }

    @Operation(summary = "订单列表")
    @GetMapping
    public Result<PageResult<OrderVO>> list(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        IPage<OrderVO> result = orderService.listOrders(UserContext.getUserId(), status, page, size);
        return Result.success(PageResult.of(result));
    }

    @Operation(summary = "订单详情")
    @GetMapping("/{id}")
    public Result<OrderVO> detail(@PathVariable Long id) {
        return Result.success(orderService.getDetail(id, UserContext.getUserId()));
    }

    @Operation(summary = "取消订单")
    @PutMapping("/{id}/cancel")
    public Result<?> cancel(@PathVariable Long id) {
        orderService.cancelOrder(id, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "确认收货")
    @PutMapping("/{id}/receive")
    public Result<?> confirmReceipt(@PathVariable Long id) {
        orderService.confirmReceipt(id, UserContext.getUserId());
        return Result.success();
    }
}
