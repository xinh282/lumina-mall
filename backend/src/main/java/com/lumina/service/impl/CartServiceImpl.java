package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.common.BusinessException;
import com.lumina.dto.CartAddDTO;
import com.lumina.entity.Cart;
import com.lumina.entity.Product;
import com.lumina.entity.ProductSku;
import com.lumina.mapper.CartMapper;
import com.lumina.mapper.ProductMapper;
import com.lumina.mapper.ProductSkuMapper;
import com.lumina.service.CartService;
import com.lumina.vo.CartVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartMapper cartMapper;
    private final ProductMapper productMapper;
    private final ProductSkuMapper skuMapper;

    @Override
    @Transactional
    public void add(Long userId, CartAddDTO dto) {
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }
        // 校验SKU库存
        int stock = product.getStock();
        if (dto.getSkuId() != null) {
            ProductSku sku = skuMapper.selectById(dto.getSkuId());
            if (sku == null || !sku.getProductId().equals(dto.getProductId())) {
                throw new BusinessException("规格不存在");
            }
            if (sku.getStatus() == 0) throw new BusinessException("该规格已停售");
            stock = sku.getStock();
        }
        // 购物车已有数量 + 本次添加数量 ≤ 库存
        int existingQty = 0;
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId).eq(Cart::getProductId, dto.getProductId())
               .eq(dto.getSkuId() != null, Cart::getSkuId, dto.getSkuId());
        Cart existing = cartMapper.selectOne(wrapper);
        if (existing != null) existingQty = existing.getQuantity();
        if (existingQty + dto.getQuantity() > stock) {
            throw new BusinessException("库存不足，当前库存: " + stock);
        }
        if (existing != null) {
            existing.setQuantity(existingQty + dto.getQuantity());
            cartMapper.updateById(existing);
        } else {
            Cart cart = new Cart();
            cart.setUserId(userId);
            cart.setProductId(dto.getProductId());
            cart.setSkuId(dto.getSkuId());
            cart.setQuantity(dto.getQuantity());
            cart.setChecked(1);
            cartMapper.insert(cart);
        }
    }

    @Override
    public List<CartVO> list(Long userId) {
        return cartMapper.selectCartListByUserId(userId);
    }

    @Override
    public void updateQuantity(Long id, Integer quantity, Long userId) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车记录不存在");
        }
        if (quantity <= 0) {
            cartMapper.deleteById(id);
        } else {
            cart.setQuantity(quantity);
            cartMapper.updateById(cart);
        }
    }

    @Override
    public void remove(Long id, Long userId) {
        Cart cart = cartMapper.selectById(id);
        if (cart == null || !cart.getUserId().equals(userId)) {
            throw new BusinessException("购物车记录不存在");
        }
        cartMapper.deleteById(id);
    }

    @Override
    public void clear(Long userId) {
        LambdaQueryWrapper<Cart> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Cart::getUserId, userId);
        cartMapper.delete(wrapper);
    }
}
