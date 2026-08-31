package com.lumina.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.common.Result;
import com.lumina.entity.Favorite;
import com.lumina.mapper.FavoriteMapper;
import com.lumina.security.UserContext;
import com.lumina.vo.ProductVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "商品收藏")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteMapper favoriteMapper;

    @Operation(summary = "我的收藏列表")
    @GetMapping
    public Result<List<ProductVO>> list() {
        return Result.success(favoriteMapper.listUserFavorites(UserContext.getUserId()));
    }

    @Operation(summary = "是否已收藏")
    @GetMapping("/check/{productId}")
    public Result<Boolean> check(@PathVariable Long productId) {
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, UserContext.getUserId()).eq(Favorite::getProductId, productId);
        return Result.success(favoriteMapper.selectCount(w) > 0);
    }

    @Operation(summary = "收藏/取消收藏")
    @PostMapping("/toggle/{productId}")
    public Result<Boolean> toggle(@PathVariable Long productId) {
        Long userId = UserContext.getUserId();
        LambdaQueryWrapper<Favorite> w = new LambdaQueryWrapper<>();
        w.eq(Favorite::getUserId, userId).eq(Favorite::getProductId, productId);
        Favorite fav = favoriteMapper.selectOne(w);
        if (fav != null) {
            favoriteMapper.deleteById(fav.getId());
            return Result.success(false);
        }
        Favorite f = new Favorite();
        f.setUserId(userId);
        f.setProductId(productId);
        favoriteMapper.insert(f);
        return Result.success(true);
    }
}
