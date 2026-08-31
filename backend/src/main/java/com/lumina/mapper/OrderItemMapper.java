package com.lumina.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumina.entity.OrderItem;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

@Mapper
public interface OrderItemMapper extends BaseMapper<OrderItem> {

    @Select("SELECT oi.product_id as productId, p.name as productName, " +
            "SUM(oi.quantity) as totalQuantity, SUM(oi.total_price) as totalRevenue " +
            "FROM l_order_item oi " +
            "JOIN l_product p ON oi.product_id = p.id " +
            "JOIN l_order o ON oi.order_id = o.id " +
            "WHERE o.status != 'CANCELLED' " +
            "GROUP BY oi.product_id, p.name " +
            "ORDER BY totalQuantity DESC LIMIT #{limit}")
    List<Map<String, Object>> topProducts(@Param("limit") int limit);
}
