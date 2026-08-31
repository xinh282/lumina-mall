package com.lumina.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CartVO {
    private Long id;
    private Long userId;
    private Long productId;
    private String productName;
    private BigDecimal productPrice;
    private Long skuId;
    private String specs;
    private String productImage;
    private Integer quantity;
    private Boolean checked;
    private LocalDateTime createTime;
}
