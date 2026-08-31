package com.lumina.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lumina.dto.OrderCreateDTO;
import com.lumina.vo.OrderVO;

public interface OrderService {
    OrderVO createOrder(Long userId, OrderCreateDTO dto);
    IPage<OrderVO> listOrders(Long userId, String status, int page, int size);
    OrderVO getDetail(Long orderId, Long userId);
    void cancelOrder(Long orderId, Long userId);
    void confirmReceipt(Long orderId, Long userId);
    IPage<OrderVO> adminList(Long userId, String status, String orderNo, int page, int size);
    OrderVO adminGetDetail(Long orderId);
    void adminUpdateStatus(Long orderId, String status, String trackingNo, String logisticsCompany);
}
