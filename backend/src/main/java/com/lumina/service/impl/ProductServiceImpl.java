package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lumina.common.BusinessException;
import com.lumina.dto.ProductQueryDTO;
import com.lumina.dto.ProductSaveDTO;
import com.lumina.entity.Product;
import com.lumina.mapper.CartMapper;
import com.lumina.mapper.ProductMapper;
import com.lumina.service.ProductService;
import com.lumina.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final CartMapper cartMapper;

    private static final String DETAIL_KEY = "product:detail:";
    private static final String HOT_KEY = "product:hot:";
    private static final String NEW_KEY = "product:new:";

    @Override
    public IPage<ProductVO> pageWithConditions(ProductQueryDTO query) {
        Page<ProductVO> page = new Page<>(query.getPage(), query.getSize());
        return productMapper.selectPageWithConditions(page, query);
    }

    @Override
    public ProductVO getDetail(Long id) {
        String key = DETAIL_KEY + id;
        ProductVO cached = getCached(key, ProductVO.class);
        if (cached != null) return cached;

        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException("商品不存在或已下架");
        }
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        if (product.getCategoryId() != null) {
            vo.setCategoryName("");
        }
        redisTemplate.opsForValue().set(key, vo, Duration.ofHours(1));
        return vo;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductVO> getHotProducts(int limit) {
        String key = HOT_KEY + limit;
        List<ProductVO> cached = getCachedList(key);
        if (cached != null) return cached;

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getIsHot, 1)
               .orderByDesc(Product::getSales)
               .last("LIMIT " + limit);
        List<Product> products = productMapper.selectList(wrapper);
        List<ProductVO> vos = products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).toList();
        redisTemplate.opsForValue().set(key, vos, Duration.ofMinutes(30));
        return vos;
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<ProductVO> getNewProducts(int limit) {
        String key = NEW_KEY + limit;
        List<ProductVO> cached = getCachedList(key);
        if (cached != null) return cached;

        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Product::getStatus, 1)
               .eq(Product::getIsNew, 1)
               .orderByDesc(Product::getCreateTime)
               .last("LIMIT " + limit);
        List<Product> products = productMapper.selectList(wrapper);
        List<ProductVO> vos = products.stream().map(p -> {
            ProductVO vo = new ProductVO();
            BeanUtils.copyProperties(p, vo);
            return vo;
        }).toList();
        redisTemplate.opsForValue().set(key, vos, Duration.ofMinutes(30));
        return vos;
    }

    @Override
    public IPage<Product> adminPage(int pageNum, int pageSize, String keyword) {
        Page<Product> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Product> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Product::getName, keyword);
        }
        wrapper.orderByDesc(Product::getCreateTime);
        return productMapper.selectPage(page, wrapper);
    }

    @Override
    public Product adminGetById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        return product;
    }

    @Override
    public void create(ProductSaveDTO dto) {
        Product product = new Product();
        BeanUtils.copyProperties(dto, product);
        product.setStatus(1);
        if (product.getIsHot() == null) product.setIsHot(0);
        if (product.getIsNew() == null) product.setIsNew(0);
        if (product.getSortOrder() == null) product.setSortOrder(0);
        productMapper.insert(product);
        clearProductCaches(null);
    }

    @Override
    public void update(Long id, ProductSaveDTO dto) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        BeanUtils.copyProperties(dto, product);
        productMapper.updateById(product);
        clearProductCaches(id);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Product product = productMapper.selectById(id);
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        product.setStatus(status);
        productMapper.updateById(product);
        clearProductCaches(id);

        if (status == 0) {
            int removed = cartMapper.deleteByProductId(id);
            if (removed > 0) {
                log.info("商品下架，清除购物车: productId={}, count={}", id, removed);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T getCached(String key, Class<T> type) {
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) return null;
        if (type.isInstance(obj)) return (T) obj;
        if (obj instanceof Map) {
            return objectMapper.convertValue(obj, type);
        }
        redisTemplate.delete(key);
        return null;
    }

    @SuppressWarnings("unchecked")
    private List<ProductVO> getCachedList(String key) {
        Object obj = redisTemplate.opsForValue().get(key);
        if (obj == null) return null;
        if (obj instanceof List<?> list) {
            if (list.isEmpty()) { redisTemplate.delete(key); return null; }
            if (list.get(0) instanceof ProductVO) return (List<ProductVO>) list;
            if (list.get(0) instanceof Map) {
                return list.stream()
                        .map(item -> objectMapper.convertValue(item, ProductVO.class))
                        .toList();
            }
        }
        redisTemplate.delete(key);
        return null;
    }

    private void clearProductCaches(Long id) {
        if (id != null) {
            redisTemplate.delete(DETAIL_KEY + id);
        }
        redisTemplate.delete(HOT_KEY + "8");
        redisTemplate.delete(NEW_KEY + "8");
    }
}
