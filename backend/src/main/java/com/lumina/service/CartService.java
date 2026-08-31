package com.lumina.service;

import com.lumina.dto.CartAddDTO;
import com.lumina.vo.CartVO;
import java.util.List;

public interface CartService {
    void add(Long userId, CartAddDTO dto);
    List<CartVO> list(Long userId);
    void updateQuantity(Long id, Integer quantity, Long userId);
    void remove(Long id, Long userId);
    void clear(Long userId);
}
