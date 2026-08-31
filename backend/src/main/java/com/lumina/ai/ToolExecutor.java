package com.lumina.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.entity.*;
import com.lumina.mapper.*;
import com.lumina.security.UserContext;
import com.lumina.service.CouponService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ToolExecutor {

    private final ProductMapper productMapper;
    private final OrderMapper orderMapper;
    private final RefundMapper refundMapper;
    private final CouponService couponService;
    private final CategoryMapper categoryMapper;

    @SuppressWarnings("unchecked")
    public Map<String, Object> execute(String toolName, Map<String, Object> args) {
        Long userId = UserContext.getUserId();
        return switch (toolName) {
            case "search_products" -> searchProducts(args);
            case "get_product_detail" -> getProductDetail(args);
            case "check_order_status" -> checkOrders(userId, args);
            case "check_refund_status" -> checkRefunds(userId);
            case "get_my_coupons" -> getCoupons(userId);
            case "get_shipping_info" -> getShippingInfo();
            case "get_return_policy" -> getReturnPolicy();
            case "get_size_guide" -> getSizeGuide(args);
            case "get_store_info" -> getStoreInfo();
            default -> Map.of("error", "未知工具: " + toolName);
        };
    }

    private Map<String, Object> searchProducts(Map<String, Object> args) {
        String keyword = (String) args.getOrDefault("keyword", "");
        String categoryName = (String) args.get("categoryName");
        String sortBy = (String) args.getOrDefault("sortBy", "sales");
        int limit = args.get("limit") instanceof Integer i ? i : 5;

        LambdaQueryWrapper<Product> w = new LambdaQueryWrapper<>();
        w.eq(Product::getStatus, 1);

        // 按分类名称查找 → 转成分类 ID
        if (categoryName != null && !categoryName.isEmpty()) {
            LambdaQueryWrapper<Category> cw = new LambdaQueryWrapper<>();
            cw.like(Category::getName, categoryName);
            List<Category> cats = categoryMapper.selectList(cw);
            if (!cats.isEmpty()) {
                List<Long> catIds = cats.stream().map(Category::getId).toList();
                w.in(Product::getCategoryId, catIds);
            }
        }

        // 按关键词搜索（多字段 OR）
        if (keyword != null && !keyword.isEmpty()) {
            String kw = keyword.trim();
            w.and(qw -> qw
                .like(Product::getName, kw)
                .or().like(Product::getDescription, kw)
                .or().like(Product::getBadgeText, kw)
            );
        } else {
            // 无关键词无分类 → 直接返回热销商品
        }

        if (args.get("categoryId") instanceof Integer cid && cid > 0) {
            w.eq(Product::getCategoryId, cid.longValue());
        }
        if (args.get("minPrice") instanceof Number min) {
            w.ge(Product::getPrice, new BigDecimal(min.toString()));
        }
        if (args.get("maxPrice") instanceof Number max) {
            w.le(Product::getPrice, new BigDecimal(max.toString()));
        }

        // 排序
        switch (sortBy) {
            case "price_asc" -> w.orderByAsc(Product::getPrice);
            case "price_desc" -> w.orderByDesc(Product::getPrice);
            case "newest" -> w.orderByDesc(Product::getCreateTime);
            default -> w.orderByDesc(Product::getSales); // 默认销量排序
        }
        w.last("LIMIT " + limit);

        List<Product> products = productMapper.selectList(w);

        // 如果关键词搜索无结果，且有分类名，提示分类下所有商品
        if (products.isEmpty() && keyword != null && !keyword.isEmpty()) {
            // 宽泛搜索：只用 like
            LambdaQueryWrapper<Product> w2 = new LambdaQueryWrapper<>();
            w2.eq(Product::getStatus, 1).like(Product::getName, keyword);
            w2.orderByDesc(Product::getSales).last("LIMIT " + limit);
            products = productMapper.selectList(w2);
        }

        // 仍然无结果 → 返回热销替代
        if (products.isEmpty()) {
            LambdaQueryWrapper<Product> w3 = new LambdaQueryWrapper<>();
            w3.eq(Product::getStatus, 1).orderByDesc(Product::getSales).last("LIMIT " + limit);
            products = productMapper.selectList(w3);
            if (products.isEmpty()) {
                return Map.of("products", List.of(), "total", 0,
                    "message", "商城暂无商品数据，请联系管理员上架商品");
            }
            List<Map<String, Object>> list = toProductList(products);
            return Map.of("products", list, "total", list.size(),
                "message", "未找到匹配「" + keyword + "」的商品，以下是最受欢迎的商品供参考");
        }

        List<Map<String, Object>> list = toProductList(products);
        return Map.of("products", list, "total", list.size(),
            "message", list.isEmpty() ? "未找到匹配商品" : "找到" + list.size() + "款商品");
    }

    private List<Map<String, Object>> toProductList(List<Product> products) {
        return products.stream().map(p -> {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("id", p.getId());
            m.put("name", p.getName());
            m.put("price", p.getPrice());
            m.put("image", p.getImage());
            m.put("stock", p.getStock());
            m.put("sales", p.getSales());
            return m;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> getProductDetail(Map<String, Object> args) {
        Long id = args.get("productId") instanceof Integer i ? i.longValue() : (Long) args.get("productId");
        Product p = productMapper.selectById(id);
        if (p == null) return Map.of("error", "商品不存在");

        return Map.of(
            "id", p.getId(), "name", p.getName(), "price", p.getPrice(),
            "originalPrice", p.getOriginalPrice() != null ? p.getOriginalPrice() : "",
            "stock", p.getStock(), "sales", p.getSales(),
            "description", p.getDescription() != null ? p.getDescription() : "暂无描述",
            "image", p.getImage() != null ? p.getImage() : ""
        );
    }

    private Map<String, Object> checkOrders(Long userId, Map<String, Object> args) {
        int limit = args.get("limit") instanceof Integer i ? i : 3;
        LambdaQueryWrapper<Order> w = new LambdaQueryWrapper<>();
        w.eq(Order::getUserId, userId).orderByDesc(Order::getCreateTime).last("LIMIT " + limit);
        List<Order> orders = orderMapper.selectList(w);

        if (orders.isEmpty()) return Map.of("message", "您还没有订单记录");

        List<Map<String, Object>> list = orders.stream().map(o -> Map.<String, Object>of(
            "orderNo", o.getOrderNo(), "status", o.getStatus(),
            "totalAmount", o.getTotalAmount(), "createTime", o.getCreateTime() != null ? o.getCreateTime().toString() : ""
        )).collect(Collectors.toList());

        return Map.of("orders", list, "total", list.size());
    }

    private Map<String, Object> checkRefunds(Long userId) {
        LambdaQueryWrapper<Refund> w = new LambdaQueryWrapper<>();
        w.eq(Refund::getUserId, userId).orderByDesc(Refund::getCreateTime).last("LIMIT 5");
        List<Refund> refunds = refundMapper.selectList(w);

        if (refunds.isEmpty()) return Map.of("message", "您没有退款记录");

        Map<String, String> statusMap = Map.of(
            "PENDING", "待审核", "APPROVED", "已通过，等待退款", "REJECTED", "未通过", "REFUNDED", "已退款"
        );
        List<Map<String, Object>> list = refunds.stream().map(r -> Map.<String, Object>of(
            "orderId", r.getOrderId(), "amount", r.getAmount(),
            "status", statusMap.getOrDefault(r.getStatus(), r.getStatus()),
            "reason", r.getReason() != null ? r.getReason() : "",
            "adminNote", r.getAdminNote() != null ? r.getAdminNote() : "",
            "createTime", r.getCreateTime() != null ? r.getCreateTime().toString() : ""
        )).collect(Collectors.toList());

        return Map.of("refunds", list, "total", list.size());
    }

    private Map<String, Object> getCoupons(Long userId) {
        try {
            List<Map<String, Object>> coupons = couponService.userCoupons(userId);
            if (coupons.isEmpty()) return Map.of("message", "您当前没有可用的优惠券，可以去领券中心看看");
            return Map.of("coupons", coupons, "total", coupons.size());
        } catch (Exception e) {
            return Map.of("message", "获取优惠券信息失败，请登录后重试");
        }
    }

    private Map<String, Object> getShippingInfo() {
        return Map.of(
            "message", "LUMINA 提供全国顺丰包邮服务",
            "details", List.of(
                "全国统一顺丰配送，下单后 1-3 个工作日发货",
                "满 99 元免运费，不满收取 8 元运费",
                "支持送货上门，发货后可在订单详情查看物流单号",
                "港澳台及海外地区请联系客服咨询"
            )
        );
    }

    private Map<String, Object> getReturnPolicy() {
        return Map.of(
            "message", "LUMINA 支持 7 天无理由退换",
            "details", List.of(
                "签收后 7 天内可申请无理由退换，商品需保持原状、吊牌完整",
                "质量问题 15 天内免费退换，我们承担来回运费",
                "退款将在收到退货后 1-3 个工作日退回原支付账户",
                "内衣、化妆品等特殊商品拆封后不支持退换"
            )
        );
    }

    private Map<String, Object> getSizeGuide(Map<String, Object> args) {
        String category = (String) args.getOrDefault("category", "通用");
        return Map.of(
            "message", "以下是" + category + "尺码参考",
            "details", List.of(
                "S 码：身高 155-160cm / 体重 45-50kg",
                "M 码：身高 160-165cm / 体重 50-55kg",
                "L 码：身高 165-170cm / 体重 55-60kg",
                "XL 码：身高 170-175cm / 体重 60-65kg",
                "具体版型可能有差异，建议参考商品详情页的具体尺码表"
            )
        );
    }

    private Map<String, Object> getStoreInfo() {
        return Map.of(
            "message", "关于 LUMINA",
            "details", List.of(
                "LUMINA 是一个精选设计师品牌集合平台，致力于为日常生活注入美学品质",
                "目前以线上商城为主，上海、北京、成都设有线下体验店",
                "体验店地址：上海市静安区南京西路 1688 号 / 北京市朝阳区三里屯太古里",
                "营业时间：10:00 - 22:00，欢迎到店试穿体验"
            )
        );
    }
}
