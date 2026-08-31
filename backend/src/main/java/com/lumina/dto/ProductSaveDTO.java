package com.lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductSaveDTO {
    @NotBlank(message = "商品名称不能为空")
    private String name;

    private String description;

    @NotNull(message = "分类不能为空")
    private Long categoryId;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private BigDecimal originalPrice;

    @NotNull(message = "库存不能为空")
    private Integer stock;

    private String badge;
    private String badgeText;
    private String image;
    private String images;
    private Integer isHot;
    private Integer isNew;
    private Integer sortOrder;
    private BigDecimal seckillPrice;
    private Integer seckillStock;
    private LocalDateTime seckillStart;
    private LocalDateTime seckillEnd;
}
