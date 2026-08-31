package com.lumina.service.impl;

import com.lumina.mapper.OrderItemMapper;
import com.lumina.mapper.OrderMapper;
import com.lumina.service.DashboardService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final OrderMapper orderMapper;
    private final OrderItemMapper orderItemMapper;

    @PostConstruct
    public void repairData() {
        int fixed = orderMapper.repairNullCreateTime();
        if (fixed > 0) {
            log.info("修复了 {} 条订单的 create_time 空值", fixed);
        }
    }

    @Override
    public Map<String, Object> getStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime tomorrowStart = today.plusDays(1).atStartOfDay();

        // 本周一
        LocalDate weekStart = today.with(WeekFields.of(java.util.Locale.CHINA).dayOfWeek(), 1);
        LocalDateTime weekStartDt = weekStart.atStartOfDay();

        // 本月1号
        LocalDate monthStart = today.withDayOfMonth(1);
        LocalDateTime monthStartDt = monthStart.atStartOfDay();

        // ===== 当前周期 =====
        int todayOrders = orderMapper.countOrdersBetween(todayStart, tomorrowStart);
        int weekOrders = orderMapper.countOrdersBetween(weekStartDt, tomorrowStart);
        int monthOrders = orderMapper.countOrdersBetween(monthStartDt, tomorrowStart);

        BigDecimal todayRevenue = nvl(orderMapper.sumRevenueBetween(todayStart, tomorrowStart));
        BigDecimal weekRevenue = nvl(orderMapper.sumRevenueBetween(weekStartDt, tomorrowStart));
        BigDecimal monthRevenue = nvl(orderMapper.sumRevenueBetween(monthStartDt, tomorrowStart));

        // ===== 上一周期（用于计算趋势）=====
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
        int yesterdayOrders = orderMapper.countOrdersBetween(yesterdayStart, todayStart);
        BigDecimal yesterdayRevenue = nvl(orderMapper.sumRevenueBetween(yesterdayStart, todayStart));

        LocalDateTime lastWeekStart = weekStartDt.minusWeeks(1);
        LocalDateTime lastWeekEnd = weekStartDt; // 到本周一为止 = 上周同期长度
        int lastWeekOrders = orderMapper.countOrdersBetween(lastWeekStart, lastWeekEnd);
        BigDecimal lastWeekRevenue = nvl(orderMapper.sumRevenueBetween(lastWeekStart, lastWeekEnd));

        LocalDate lastMonthStartDate = monthStart.minusMonths(1);
        LocalDate lastMonthEndDate = monthStart; // 到本月1号为止
        int lastMonthOrders = orderMapper.countOrdersBetween(
                lastMonthStartDate.atStartOfDay(), lastMonthEndDate.atStartOfDay());
        BigDecimal lastMonthRevenue = nvl(orderMapper.sumRevenueBetween(
                lastMonthStartDate.atStartOfDay(), lastMonthEndDate.atStartOfDay()));

        // ===== TOP5 =====
        List<Map<String, Object>> topProducts = orderItemMapper.topProducts(5);

        // ===== 组装结果 =====
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("todayOrders", todayOrders);
        result.put("weekOrders", weekOrders);
        result.put("monthOrders", monthOrders);
        result.put("todayRevenue", todayRevenue);
        result.put("weekRevenue", weekRevenue);
        result.put("monthRevenue", monthRevenue);

        result.put("todayOrdersTrend", calcTrend(todayOrders, yesterdayOrders));
        result.put("weekOrdersTrend", calcTrend(weekOrders, lastWeekOrders));
        result.put("monthOrdersTrend", calcTrend(monthOrders, lastMonthOrders));
        result.put("todayRevenueTrend", calcTrend(todayRevenue, yesterdayRevenue));
        result.put("weekRevenueTrend", calcTrend(weekRevenue, lastWeekRevenue));
        result.put("monthRevenueTrend", calcTrend(monthRevenue, lastMonthRevenue));

        result.put("topProducts", topProducts);
        return result;
    }

    private BigDecimal nvl(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    /**
     * 计算趋势百分比 (较上一周期)，返回 0~999 的整数百分比，或 null（不可比）
     */
    private Integer calcTrend(BigDecimal current, BigDecimal previous) {
        if (previous.compareTo(BigDecimal.ZERO) == 0) {
            if (current.compareTo(BigDecimal.ZERO) == 0) return 0;
            return null; // 上期为0，无法计算百分比，前端显示 "--"
        }
        return current.subtract(previous)
                .divide(previous, 2, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .intValue();
    }

    private Integer calcTrend(int current, int previous) {
        if (previous == 0) {
            if (current == 0) return 0;
            return null;
        }
        return (int) Math.round((current - previous) * 100.0 / previous);
    }

    @Override
    public byte[] exportExcel() {
        Map<String, Object> stats = getStats();
        StringBuilder sb = new StringBuilder();
        sb.append("<table border='1'>");
        sb.append("<tr><th>指标</th><th>数值</th><th>趋势</th></tr>");

        String[][] rows = {
            {"今日订单", String.valueOf(stats.get("todayOrders")), trendStr(stats.get("todayOrdersTrend"))},
            {"本周订单", String.valueOf(stats.get("weekOrders")), trendStr(stats.get("weekOrdersTrend"))},
            {"本月订单", String.valueOf(stats.get("monthOrders")), trendStr(stats.get("monthOrdersTrend"))},
            {"今日成交额", "¥" + stats.get("todayRevenue"), trendStr(stats.get("todayRevenueTrend"))},
            {"本周成交额", "¥" + stats.get("weekRevenue"), trendStr(stats.get("weekRevenueTrend"))},
            {"本月成交额", "¥" + stats.get("monthRevenue"), trendStr(stats.get("monthRevenueTrend"))},
        };
        for (String[] row : rows) {
            sb.append("<tr><td>").append(row[0]).append("</td><td>").append(row[1])
              .append("</td><td>").append(row[2]).append("</td></tr>");
        }

        sb.append("<tr><td colspan='3'><b>TOP5 商品</b></td></tr>");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> top5 = (List<Map<String, Object>>) stats.get("topProducts");
        for (Map<String, Object> p : top5) {
            sb.append("<tr><td>").append(p.get("productName"))
              .append("</td><td>").append(p.get("totalQuantity")).append("单")
              .append("</td><td>¥").append(p.get("totalRevenue")).append("</td></tr>");
        }
        sb.append("</table>");
        try {
            return sb.toString().getBytes("UTF-8");
        } catch (Exception e) {
            return sb.toString().getBytes();
        }
    }

    private String trendStr(Object t) {
        if (t == null) return "--";
        int v = ((Number) t).intValue();
        return v > 0 ? "↑" + v + "%" : v < 0 ? "↓" + Math.abs(v) + "%" : "→0%";
    }
}
