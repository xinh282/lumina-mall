package com.lumina.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumina.entity.Favorite;
import com.lumina.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {

    @Select("SELECT p.*, c.name AS categoryName FROM l_favorite f " +
            "JOIN l_product p ON f.product_id = p.id " +
            "LEFT JOIN l_category c ON p.category_id = c.id " +
            "WHERE f.user_id = #{userId} AND p.status = 1 ORDER BY f.create_time DESC")
    List<ProductVO> listUserFavorites(@Param("userId") Long userId);
}
