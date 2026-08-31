package com.lumina.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.entity.Refund;

public interface RefundService {
    Refund apply(Long userId, Long orderId, String reason);
    IPage<Refund> userList(Long userId, int page, int size);
    IPage<Refund> adminList(String status, int page, int size);
    void approve(Long id, String adminNote);
    void reject(Long id, String adminNote);
    void confirmRefunded(Long id);
}
