package com.lumina.vo;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderVO {
    private Long id;
    private String orderNo;
    private Long userId;
    private BigDecimal totalAmount;
    private String status;
    private String receiverName;
    private String receiverPhone;
    private String receiverAddress;
    private String trackingNo;
    private String logisticsCompany;
    private LocalDateTime payTime;
    private BigDecimal couponDiscount;
    private LocalDateTime createTime;
    private List<OrderItemVO> orderItems;
}
