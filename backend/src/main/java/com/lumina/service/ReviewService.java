package com.lumina.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.entity.Review;
import java.util.Map;

public interface ReviewService {
    Review create(Long userId, Long productId, Long orderId, Integer rating, String content);
    IPage<Review> listByProduct(Long productId, int page, int size);
    IPage<Review> adminList(Long productId, int page, int size);
    Map<String, Object> getRatingStats(Long productId);
    void deleteByAdmin(Long reviewId);
}
