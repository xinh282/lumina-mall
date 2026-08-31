package com.lumina.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.dto.ProductSaveDTO;
import com.lumina.dto.ProductQueryDTO;
import com.lumina.entity.Product;
import com.lumina.vo.ProductVO;
import java.util.List;

public interface ProductService {
    IPage<ProductVO> pageWithConditions(ProductQueryDTO query);
    ProductVO getDetail(Long id);
    List<ProductVO> getHotProducts(int limit);
    List<ProductVO> getNewProducts(int limit);
    IPage<Product> adminPage(int page, int size, String keyword);
    Product adminGetById(Long id);
    void create(ProductSaveDTO dto);
    void update(Long id, ProductSaveDTO dto);
    void updateStatus(Long id, Integer status);
}
