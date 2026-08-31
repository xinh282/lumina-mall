package com.lumina.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("LUMINA 精选好物商城 API")
                        .version("1.0.0")
                        .description("LUMINA 全栈电商毕业设计 — 后端接口文档")
                        .contact(new Contact()
                                .name("LUMINA Team")
                                .email("team@lumina.com")));
    }
}
