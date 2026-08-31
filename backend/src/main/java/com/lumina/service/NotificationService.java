package com.lumina.service;

import com.lumina.entity.Notification;
import java.util.List;

public interface NotificationService {
    void create(Long userId, String title, String content, String type, Long refId);
    List<Notification> listByUser(Long userId);
    int countUnread(Long userId);
    void markRead(Long id, Long userId);
    void markAllRead(Long userId);
}
