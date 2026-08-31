package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.dto.LoginDTO;
import com.lumina.dto.RegisterDTO;
import com.lumina.security.UserContext;
import com.lumina.service.UserService;
import com.lumina.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户模块", description = "注册、登录、个人信息管理")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "用户注册")
    @PostMapping("/register")
    public Result<?> register(@Valid @RequestBody RegisterDTO dto) {
        userService.register(dto);
        return Result.success();
    }

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<?> login(@Valid @RequestBody LoginDTO dto) {
        return Result.success(userService.login(dto));
    }

    @Operation(summary = "获取个人信息")
    @GetMapping("/profile")
    public Result<UserVO> profile() {
        return Result.success(userService.getProfile(UserContext.getUserId()));
    }

    @Operation(summary = "更新个人信息")
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody UserVO vo) {
        userService.updateProfile(UserContext.getUserId(), vo);
        return Result.success();
    }
}
