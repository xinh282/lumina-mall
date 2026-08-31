package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumina.common.BusinessException;
import com.lumina.entity.Review;
import com.lumina.mapper.ReviewMapper;
import com.lumina.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewMapper reviewMapper;

    @Override
    @Transactional
    public Review create(Long userId, Long productId, Long orderId, Integer rating, String content) {
        if (rating == null || rating < 1 || rating > 5) {
            throw new BusinessException("评分必须在1-5之间");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评价内容不能为空");
        }
        // 同一订单同一商品不重复评价
        if (orderId != null) {
            LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
            w.eq(Review::getUserId, userId)
             .eq(Review::getProductId, productId)
             .eq(Review::getOrderId, orderId);
            if (reviewMapper.selectCount(w) > 0) {
                throw new BusinessException("该订单已评价过此商品");
            }
        }
        Review review = new Review();
        review.setUserId(userId);
        review.setProductId(productId);
        review.setOrderId(orderId);
        review.setRating(rating);
        review.setContent(content.trim());
        reviewMapper.insert(review);
        return review;
    }

    @Override
    public IPage<Review> listByProduct(Long productId, int page, int size) {
        Page<Review> p = new Page<>(page, size);
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        w.eq(Review::getProductId, productId).orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(p, w);
    }

    @Override
    public Map<String, Object> getRatingStats(Long productId) {
        return reviewMapper.getRatingStats(productId);
    }

    @Override
    public IPage<Review> adminList(Long productId, int page, int size) {
        Page<Review> p = new Page<>(page, size);
        LambdaQueryWrapper<Review> w = new LambdaQueryWrapper<>();
        if (productId != null) w.eq(Review::getProductId, productId);
        w.orderByDesc(Review::getCreateTime);
        return reviewMapper.selectPage(p, w);
    }

    @Override
    public void deleteByAdmin(Long reviewId) {
        reviewMapper.deleteById(reviewId);
    }
}
