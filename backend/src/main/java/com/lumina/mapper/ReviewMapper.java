package com.lumina.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumina.entity.Review;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface ReviewMapper extends BaseMapper<Review> {

    @Select("SELECT COALESCE(AVG(rating), 0) as avgRating, COUNT(*) as total FROM l_review WHERE product_id = #{productId}")
    Map<String, Object> getRatingStats(@Param("productId") Long productId);
}
