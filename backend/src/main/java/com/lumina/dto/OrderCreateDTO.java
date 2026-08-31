package com.lumina.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class OrderCreateDTO {
    @NotBlank(message = "收货人不能为空")
    private String receiverName;
    @NotBlank(message = "收货电话不能为空")
    private String receiverPhone;
    @NotBlank(message = "收货地址不能为空")
    private String receiverAddress;
    @NotEmpty(message = "购物车项不能为空")
    private List<Long> cartItemIds;
    private Long userCouponId;
}
