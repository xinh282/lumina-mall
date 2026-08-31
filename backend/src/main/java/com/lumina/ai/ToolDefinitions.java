package com.lumina.ai;

import java.util.List;
import java.util.Map;

/**
 * AI 导购可调用的工具定义（传给 DeepSeek 的 tools 参数）
 */
public class ToolDefinitions {

    public static List<Map<String, Object>> getAll() {
        return List.of(
            // 1. 商品搜索
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "search_products",
                    "description", "搜索商品。支持关键词、分类名称、价格区间、排序方式。当用户找商品、推荐、查热销、按分类浏览时调用。若无关键词则返回热销商品。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "keyword", Map.of("type", "string", "description", "搜索关键词，如'连衣裙'、'春季薄外套'等。可为空表示不限定关键词"),
                            "categoryName", Map.of("type", "string", "description", "分类名称，如'女装'、'男装'、'配饰'。AI应把用户说的品类转成分类名"),
                            "categoryId", Map.of("type", "integer", "description", "分类ID（可选，优先用categoryName）"),
                            "maxPrice", Map.of("type", "number", "description", "最高价格（可选）"),
                            "minPrice", Map.of("type", "number", "description", "最低价格（可选）"),
                            "sortBy", Map.of("type", "string", "description", "排序: sales(销量)/price_asc(价格升)/price_desc(价格降)/newest(最新)，默认sales"),
                            "limit", Map.of("type", "integer", "description", "返回数量，默认5")
                        )
                    )
                )
            ),
            // 2. 商品详情
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "get_product_detail",
                    "description", "查询某个商品的详细信息，包括材质、库存、评价等。当用户询问特定商品细节时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "productId", Map.of("type", "integer", "description", "商品ID")
                        ),
                        "required", List.of("productId")
                    )
                )
            ),
            // 3. 订单查询
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "check_order_status",
                    "description", "查询用户最近的订单状态。当用户问'我的订单到哪了'、'什么时候发货'等时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "limit", Map.of("type", "integer", "description", "返回最近几条订单，默认3")
                        )
                    )
                )
            ),
            // 4. 退款进度
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "check_refund_status",
                    "description", "查询用户的退款申请进度。当用户问退款相关问题时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of()
                    )
                )
            ),
            // 5. 优惠券
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "get_my_coupons",
                    "description", "查询用户可用的优惠券。当用户问'有优惠吗'、'能便宜点吗'、'有券吗'时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of()
                    )
                )
            ),
            // 6. 物流配送
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "get_shipping_info",
                    "description", "查询配送说明。当用户问'多久能到'、'包邮吗'、'能发顺丰吗'时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of()
                    )
                )
            ),
            // 7. 退换政策
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "get_return_policy",
                    "description", "查询退换货政策。当用户问'不合适能退吗'、'怎么退货'时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of()
                    )
                )
            ),
            // 8. 尺码指南
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "get_size_guide",
                    "description", "查询尺码指南。当用户问'穿什么码'、'尺码偏大吗'、'我170选什么号'时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of(
                            "category", Map.of("type", "string", "description", "品类：女装/男装/鞋履")
                        )
                    )
                )
            ),
            // 9. 门店/品牌
            Map.of(
                "type", "function",
                "function", Map.of(
                    "name", "get_store_info",
                    "description", "查询品牌信息和线下门店。当用户问'你们是什么品牌'、'有实体店吗'、'在哪可以试穿'时调用。",
                    "parameters", Map.of(
                        "type", "object",
                        "properties", Map.of()
                    )
                )
            )
        );
    }
}
