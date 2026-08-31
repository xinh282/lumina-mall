package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.dto.CartAddDTO;
import com.lumina.security.UserContext;
import com.lumina.service.CartService;
import com.lumina.vo.CartVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "购物车模块", description = "购物车增删改查")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @Operation(summary = "购物车列表")
    @GetMapping
    public Result<List<CartVO>> list() {
        return Result.success(cartService.list(UserContext.getUserId()));
    }

    @Operation(summary = "添加商品到购物车")
    @PostMapping
    public Result<?> add(@Valid @RequestBody CartAddDTO dto) {
        cartService.add(UserContext.getUserId(), dto);
        return Result.success();
    }

    @Operation(summary = "更新购物车商品数量")
    @PutMapping("/{id}")
    public Result<?> update(@PathVariable Long id, @RequestParam Integer quantity) {
        cartService.updateQuantity(id, quantity, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "删除购物车商品")
    @DeleteMapping("/{id}")
    public Result<?> remove(@PathVariable Long id) {
        cartService.remove(id, UserContext.getUserId());
        return Result.success();
    }

    @Operation(summary = "清空购物车")
    @DeleteMapping
    public Result<?> clear() {
        cartService.clear(UserContext.getUserId());
        return Result.success();
    }
}
