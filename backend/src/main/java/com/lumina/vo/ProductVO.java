package com.lumina.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductVO {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private String categoryName;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private Integer stock;
    private String badge;
    private String badgeText;
    private String image;
    private String images;
    private Integer sales;
    private Boolean isHot;
    private Boolean isNew;
    private Integer seckillStock;
    private BigDecimal seckillPrice;
    private LocalDateTime seckillStart;
    private LocalDateTime seckillEnd;
    private Integer sortOrder;
    private LocalDateTime createTime;
}
