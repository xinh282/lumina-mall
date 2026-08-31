package com.lumina.controller;

import com.lumina.common.Result;
import com.lumina.entity.Notification;
import com.lumina.security.UserContext;
import com.lumina.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "通知")
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "我的通知列表")
    @GetMapping
    public Result<List<Notification>> list() {
        return Result.success(notificationService.listByUser(UserContext.getUserId()));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Map<String, Integer>> unreadCount() {
        int count = notificationService.countUnread(UserContext.getUserId());
        return Result.success(Map.of("count", count));
    }

    @Operation(summary = "标记已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id, UserContext.getUserId());
        return Result.success(null);
    }

    @Operation(summary = "全部已读")
    @PutMapping("/read-all")
    public Result<Void> markAllRead() {
        notificationService.markAllRead(UserContext.getUserId());
        return Result.success(null);
    }
}
