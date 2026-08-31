package com.lumina.ai;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIService {

    private final ToolExecutor toolExecutor;
    private final ObjectMapper objectMapper;

    @Value("${deepseek.api-key:}")
    private String apiKey;

    @Value("${deepseek.base-url:https://api.deepseek.com}")
    private String baseUrl;

    private static final String MODEL = "deepseek-chat";
    private static final String SYSTEM_PROMPT = """
        你是「LUMINA 精选好物商城」的 AI 智能导购，名字叫"小L"，是一个专业的售前咨询+导购助手。

        【核心职责】
        1. 帮用户找商品、推荐商品、对比商品（必须调用 search_products 获取真实数据）
        2. 回答价格、库存、优惠券等购物相关问题
        3. 查询订单状态、退款进度、物流配送、退换政策、尺码指南等
        4. 主动引导用户做出购买决策，提升购物体验

        【铁律——绝对禁止】
        1. 禁止编造任何商品！所有商品信息必须来自工具返回的真实数据，不能说"有一款xxx"除非工具确实返回了
        2. 禁止编造价格、库存、销量等数字！这些只能来自工具返回值
        3. 禁止回答无关问题！你只负责商城导购。遇到政治、医疗、法律、编程、娱乐八卦等问题，统一回复："不好意思，我是商城导购助手，只负责帮您挑好物哦～有什么购物问题尽管问我！"
        4. 禁止扮演其他角色！不要写代码、写文章、翻译、做题

        【回答规范】
        1. 先理解再行动：用户说"给女友的礼物"，先搜合适商品，不要直接说"我建议买xxx"然后编造
        2. 无结果要给替代：工具返回空时，主动推荐热销商品或建议放宽条件，不能说"没有"就结束
        3. 展示要有结构：用列表、对比、分类组织信息，方便用户决策
        4. 结尾给行动选项：如"需要我帮你对比这2款吗？""想看看哪款的详情？"
        5. 语气温馨专业：适度使用 emoji 😊，但不要每句都用

        【场景化SOP】
        - 找货/推荐：search_products → 展示2-5款 → 说明推荐理由 → 问是否需要详情
        - 比价/省钱：get_my_coupons + search_products → 算优惠 → 推荐最优方案
        - 查订单：check_order_status → 告知状态 → 如有问题引导联系客服
        - 查退款：check_refund_status → 告知进度 → 说明预计到账时间
        - 问物流：get_shipping_info → 告知配送方式+时效
        - 问退换：get_return_policy → 告知政策+流程
        - 问尺码：get_size_guide → 给出建议 → 提醒"具体版型可能有差异"
        - 问品牌/门店：get_store_info → 告知品牌故事+门店地址

        【模糊需求处理】
        用户需求模糊时（如"送礼物""换季了"），先确认或推测场景，直接搜可能的关键词。
        例如"送女朋友礼物"→ 搜 keyword:"礼物"或"精致"、"丝巾"、"香薰"、"项链"
        "换季了"→ 搜当前季节相关的品类，如夏季搜"短袖""薄款""防晒"
        """;

    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    @PostConstruct
    public void init() {
        String masked = apiKey != null && apiKey.length() > 8
            ? apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4)
            : "NOT SET";
        log.info("AI 导购初始化: model={}, baseUrl={}, apiKey={}", MODEL, baseUrl, masked);
    }

    /**
     * 处理一轮对话，可能包含多次 tool call 往返
     */
    @SuppressWarnings("unchecked")
    public ChatDTO.Response chat(String userMessage, List<Map<String, String>> history, Long userId) {
        // 设置用户上下文，供 ToolExecutor 使用
        com.lumina.security.UserContext.setUserId(userId);

        List<Map<String, Object>> messages = new ArrayList<>();
        messages.add(Map.of("role", "system", "content", SYSTEM_PROMPT));

        // 历史对话（最近 10 轮）
        if (history != null) {
            for (Map<String, String> h : history) {
                if (h.containsKey("role") && h.containsKey("content")) {
                    messages.add(Map.of("role", (Object) h.get("role"), "content", (Object) h.get("content")));
                }
            }
        }

        // 当前用户消息
        messages.add(Map.of("role", "user", "content", userMessage));

        try {
            // 第一轮：发送消息 + 工具定义
            Map<String, Object> resp1 = callDeepSeek(messages, true);
            Map<String, Object> choice = getFirstChoice(resp1);
            Map<String, Object> assistantMsg = (Map<String, Object>) choice.get("message");

            // tool_calls 在 message 里面，不在 choice 层级
            if (assistantMsg.containsKey("tool_calls")) {
                List<Map<String, Object>> toolCalls = (List<Map<String, Object>>) assistantMsg.get("tool_calls");

                // 添加 AI 的 tool_calls 消息（必须有 content，即使为 null）
                Map<String, Object> aiMsg = new LinkedHashMap<>();
                aiMsg.put("role", "assistant");
                aiMsg.put("content", assistantMsg.getOrDefault("content", null));
                aiMsg.put("tool_calls", toolCalls);
                messages.add(aiMsg);

                // 执行所有工具调用
                String usedTool = null;
                for (Map<String, Object> tc : toolCalls) {
                    Map<String, Object> func = (Map<String, Object>) tc.get("function");
                    String toolName = (String) func.get("name");
                    String argsJson = (String) func.get("arguments");
                    Map<String, Object> args = objectMapper.readValue(argsJson,
                            new TypeReference<Map<String, Object>>() {});

                    log.info("AI 调用工具: {} args={}", toolName, args);
                    usedTool = toolName;

                    Map<String, Object> result = toolExecutor.execute(toolName, args);
                    String resultJson = objectMapper.writeValueAsString(result);

                    messages.add(Map.of(
                        "role", "tool",
                        "tool_call_id", tc.get("id"),
                        "content", resultJson
                    ));
                }

                // 第二轮：把工具结果传给 AI，让它组织语言（不再给 tools）
                Map<String, Object> resp2 = callDeepSeek(messages, false);
                Map<String, Object> choice2 = getFirstChoice(resp2);
                Map<String, Object> assistantMsg2 = (Map<String, Object>) choice2.get("message");
                String reply = (String) assistantMsg2.get("content");

                // 提取商品列表
                List<ChatDTO.Response.ProductRef> products = extractProducts(messages, usedTool);

                ChatDTO.Response response = new ChatDTO.Response();
                response.setReply(reply);
                response.setUsedTool(usedTool);
                response.setProducts(products);
                return response;
            }

            // 不需要调工具，直接回复
            String reply = (String) assistantMsg.get("content");
            ChatDTO.Response response = new ChatDTO.Response();
            response.setReply(reply);
            return response;

        } catch (Exception e) {
            log.error("AI 调用失败", e);
            ChatDTO.Response response = new ChatDTO.Response();
            response.setReply("抱歉，我暂时无法处理您的请求，请稍后再试或联系人工客服。");
            return response;
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> callDeepSeek(List<Map<String, Object>> messages, boolean withTools) throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("model", MODEL);
        body.put("messages", messages);
        body.put("temperature", 0.3);
        body.put("max_tokens", 1024);

        if (withTools) {
            body.put("tools", ToolDefinitions.getAll());
            body.put("tool_choice", "auto");
        }

        String json = objectMapper.writeValueAsString(body);
        log.debug("DeepSeek 请求: {}", json.substring(0, Math.min(200, json.length())));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/v1/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofSeconds(30))
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> httpResp = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        log.debug("DeepSeek 响应 status={}, body={}", httpResp.statusCode(),
                httpResp.body().substring(0, Math.min(300, httpResp.body().length())));

        if (httpResp.statusCode() != 200) {
            log.error("DeepSeek API 返回错误: {}", httpResp.body());
            throw new RuntimeException("DeepSeek API error: " + httpResp.statusCode());
        }

        return objectMapper.readValue(httpResp.body(), new TypeReference<Map<String, Object>>() {});
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getFirstChoice(Map<String, Object> resp) {
        List<Map<String, Object>> choices = (List<Map<String, Object>>) resp.get("choices");
        if (choices == null || choices.isEmpty()) throw new RuntimeException("AI 返回为空");
        return choices.get(0);
    }

    /** 从工具调用结果中提取商品列表 */
    @SuppressWarnings("unchecked")
    private List<ChatDTO.Response.ProductRef> extractProducts(List<Map<String, Object>> messages, String tool) {
        if (!"search_products".equals(tool) && !"get_product_detail".equals(tool)) return null;
        try {
            for (Map<String, Object> msg : messages) {
                if ("tool".equals(msg.get("role"))) {
                    String content = (String) msg.get("content");
                    Map<String, Object> data = objectMapper.readValue(content,
                            new TypeReference<Map<String, Object>>() {});
                    Object products = data.get("products");
                    if (products instanceof List<?> list) {
                        return list.stream().map(item -> {
                            Map<String, Object> m = (Map<String, Object>) item;
                            ChatDTO.Response.ProductRef ref = new ChatDTO.Response.ProductRef();
                            ref.setId(toLong(m.get("id")));
                            ref.setName((String) m.get("name"));
                            ref.setImage((String) m.get("image"));
                            Object price = m.get("price");
                            if (price instanceof Number n) ref.setPrice(java.math.BigDecimal.valueOf(n.doubleValue()));
                            return ref;
                        }).limit(5).collect(Collectors.toList());
                    }
                }
            }
        } catch (Exception e) { log.warn("提取商品列表失败", e); }
        return null;
    }

    private Long toLong(Object v) {
        if (v instanceof Long l) return l;
        if (v instanceof Integer i) return i.longValue();
        if (v instanceof String s) return Long.parseLong(s);
        return null;
    }
}
