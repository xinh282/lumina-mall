package com.lumina.service;

import com.lumina.vo.ProductVO;
import java.util.List;

public interface SeckillService {
    void initSeckillStock();
    List<ProductVO> getActiveProducts();
    int seckill(Long productId, Long userId);
}
