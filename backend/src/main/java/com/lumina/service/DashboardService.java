package com.lumina.service;

import java.util.Map;

public interface DashboardService {
    Map<String, Object> getStats();
    byte[] exportExcel();
}
