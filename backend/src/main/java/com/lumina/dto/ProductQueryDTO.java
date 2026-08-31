package com.lumina.dto;

import lombok.Data;

@Data
public class ProductQueryDTO {
    private Integer page = 1;
    private Integer size = 12;
    private Long categoryId;
    private String keyword;
    private String sort;  // price_asc, price_desc, sales_desc, newest
}
