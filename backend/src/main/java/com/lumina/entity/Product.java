package com.lumina.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("l_product")
public class Product {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String badge;
    private String badgeText;
    private String image;
    private String images;
    private Integer sales;
    private Integer status;
    private Integer isHot;
    private Integer isNew;
    private Integer seckillStock;
    private BigDecimal seckillPrice;
    private LocalDateTime seckillStart;
    private LocalDateTime seckillEnd;
    private Integer sortOrder;
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
