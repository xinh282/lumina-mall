package com.lumina.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumina.entity.Cart;
import com.lumina.vo.CartVO;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CartMapper extends BaseMapper<Cart> {

    List<CartVO> selectCartListByUserId(@Param("userId") Long userId);

    @Delete("DELETE FROM l_cart WHERE product_id = #{productId}")
    int deleteByProductId(@Param("productId") Long productId);
}
