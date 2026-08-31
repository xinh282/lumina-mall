package com.lumina.ai;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ChatDTO {

    @Data
    public static class Request {
        private String message;
        private List<Map<String, String>> history; // [{role, content}, ...]
    }

    @Data
    public static class Response {
        private String reply;
        private String usedTool;
        private List<ProductRef> products;

        @Data
        public static class ProductRef {
            private Long id;
            private String name;
            private String image;
            private BigDecimal price;
        }
    }
}
