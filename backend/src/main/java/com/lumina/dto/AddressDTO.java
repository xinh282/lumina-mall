package com.lumina.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddressDTO {
    @NotBlank(message = "收货人不能为空")
    private String receiverName;
    @NotBlank(message = "手机号不能为空")
    private String receiverPhone;
    @NotBlank(message = "地址不能为空")
    private String receiverAddress;
    private Integer saveAsDefault;
}
