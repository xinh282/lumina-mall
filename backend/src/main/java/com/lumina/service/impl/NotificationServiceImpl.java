package com.lumina.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lumina.entity.Notification;
import com.lumina.mapper.NotificationMapper;
import com.lumina.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;

    @Override
    public void create(Long userId, String title, String content, String type, Long refId) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setTitle(title);
        n.setContent(content);
        n.setType(type);
        n.setRefId(refId);
        n.setIsRead(0);
        notificationMapper.insert(n);
    }

    @Override
    public List<Notification> listByUser(Long userId) {
        return notificationMapper.listByUserId(userId);
    }

    @Override
    public int countUnread(Long userId) {
        return notificationMapper.countUnread(userId);
    }

    @Override
    @Transactional
    public void markRead(Long id, Long userId) {
        Notification n = notificationMapper.selectById(id);
        if (n != null && n.getUserId().equals(userId)) {
            n.setIsRead(1);
            notificationMapper.updateById(n);
        }
    }

    @Override
    @Transactional
    public void markAllRead(Long userId) {
        LambdaQueryWrapper<Notification> w = new LambdaQueryWrapper<>();
        w.eq(Notification::getUserId, userId).eq(Notification::getIsRead, 0);
        List<Notification> unread = notificationMapper.selectList(w);
        unread.forEach(n -> n.setIsRead(1));
        for (Notification n : unread) {
            notificationMapper.updateById(n);
        }
    }
}
