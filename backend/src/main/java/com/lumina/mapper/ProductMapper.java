package com.lumina.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lumina.dto.ProductQueryDTO;
import com.lumina.entity.Product;
import com.lumina.vo.ProductVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface ProductMapper extends BaseMapper<Product> {

    IPage<ProductVO> selectPageWithConditions(Page<ProductVO> page, @Param("query") ProductQueryDTO query);

    @Update("UPDATE l_product SET stock = stock - #{quantity} WHERE id = #{productId} AND stock >= #{quantity}")
    int deductStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Update("UPDATE l_product SET stock = stock + #{quantity} WHERE id = #{productId}")
    int restoreStock(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Update("UPDATE l_product SET sales = sales + #{quantity} WHERE id = #{productId}")
    int incrementSales(@Param("productId") Long productId, @Param("quantity") int quantity);

    @Update("UPDATE l_product SET seckill_stock = seckill_stock - 1, stock = stock - 1, sales = sales + 1 WHERE id = #{productId} AND seckill_stock > 0")
    int deductSeckillStock(@Param("productId") Long productId);
}
