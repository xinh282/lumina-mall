package com.lumina.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lumina.entity.Order;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT COUNT(*) FROM l_order WHERE status != 'CANCELLED' AND create_time >= #{start} AND create_time < #{end}")
    int countOrdersBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Select("SELECT COALESCE(SUM(total_amount), 0) FROM l_order WHERE status != 'CANCELLED' AND create_time >= #{start} AND create_time < #{end}")
    BigDecimal sumRevenueBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Update("UPDATE l_order SET create_time = COALESCE(pay_time, NOW()), update_time = NOW() WHERE create_time IS NULL")
    int repairNullCreateTime();

    @Select("SELECT DATE(create_time) as date, COUNT(*) as count, COALESCE(SUM(total_amount), 0) as amount " +
            "FROM l_order WHERE status != 'CANCELLED' AND create_time >= #{start} AND create_time < #{end} " +
            "GROUP BY DATE(create_time) ORDER BY date")
    List<Map<String, Object>> dailyTrend(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
