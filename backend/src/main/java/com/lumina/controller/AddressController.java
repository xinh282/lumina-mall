package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.dto.AddressDTO;
import com.lumina.entity.Address;
import com.lumina.security.UserContext;
import com.lumina.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "收货地址")
@RestController
@RequestMapping("/api/address")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;

    @Operation(summary = "获取地址列表")
    @GetMapping
    public Result<List<Address>> list() {
        return Result.success(addressService.listByUser(UserContext.getUserId()));
    }

    @Operation(summary = "获取默认地址")
    @GetMapping("/default")
    public Result<Address> getDefault() {
        return Result.success(addressService.getDefault(UserContext.getUserId()));
    }

    @Operation(summary = "保存地址")
    @PostMapping
    public Result<Void> save(@Valid @RequestBody AddressDTO dto) {
        addressService.save(UserContext.getUserId(), dto);
        return Result.success(null);
    }

    @Operation(summary = "删除地址")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        addressService.delete(id, UserContext.getUserId());
        return Result.success(null);
    }
}
