package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.mapper.OrderMapper;
import com.lumina.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Tag(name = "数据大屏")
@RestController
@RequestMapping("/api/shop")
@RequiredArgsConstructor
public class BigScreenController {

    private final DashboardService dashboardService;
    private final OrderMapper orderMapper;

    @Operation(summary = "获取大屏全部数据")
    @GetMapping("/getShopBigScreenData")
    public Result<Map<String, Object>> getBigScreenData() {
        Map<String, Object> data = new LinkedHashMap<>();

        // 1. 指标卡片数据
        Map<String, Object> stats = dashboardService.getStats();
        data.put("cards", buildCards(stats));

        // 2. 近30天趋势
        LocalDate today = LocalDate.now();
        LocalDateTime thirtyDaysAgo = today.minusDays(30).atStartOfDay();
        LocalDateTime tomorrow = today.plusDays(1).atStartOfDay();
        List<Map<String, Object>> trends = orderMapper.dailyTrend(thirtyDaysAgo, tomorrow);

        List<String> dates = new ArrayList<>();
        List<Integer> orderCounts = new ArrayList<>();
        List<Double> amounts = new ArrayList<>();
        for (Map<String, Object> row : trends) {
            dates.add(row.get("date").toString());
            orderCounts.add(((Number) row.get("count")).intValue());
            amounts.add(((Number) row.get("amount")).doubleValue());
        }
        data.put("trendDates", dates);
        data.put("trendOrders", orderCounts);
        data.put("trendAmounts", amounts);

        // 3. TOP5 商品
        data.put("topProducts", stats.get("topProducts"));

        // 4. 饼图数据
        List<Map<String, Object>> pieData = new ArrayList<>();
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> top5 = (List<Map<String, Object>>) stats.get("topProducts");
        for (Map<String, Object> p : top5) {
            pieData.add(Map.of("name", p.get("productName"), "value", p.get("totalRevenue")));
        }
        data.put("pieData", pieData);

        return Result.success(data);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> buildCards(Map<String, Object> stats) {
        List<Map<String, Object>> cards = new ArrayList<>();
        String[][] cardDefs = {
            {"今日订单", "todayOrders", "todayOrdersTrend", "单"},
            {"本周订单", "weekOrders", "weekOrdersTrend", "单"},
            {"本月订单", "monthOrders", "monthOrdersTrend", "单"},
            {"今日成交额", "todayRevenue", "todayRevenueTrend", "¥"},
            {"本周成交额", "weekRevenue", "weekRevenueTrend", "¥"},
            {"本月成交额", "monthRevenue", "monthRevenueTrend", "¥"},
        };
        for (String[] def : cardDefs) {
            Map<String, Object> card = new LinkedHashMap<>();
            card.put("label", def[0]);
            Object value = stats.get(def[1]);
            card.put("value", def[3].equals("¥") && value instanceof Number n
                    ? String.format("%.2f", n.doubleValue()) : value);
            card.put("trend", stats.get(def[2]));
            card.put("unit", def[3]);
            cards.add(card);
        }
        return cards;
    }
}
