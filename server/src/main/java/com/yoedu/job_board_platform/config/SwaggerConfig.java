package com.yoedu.job_board_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

/**
 * Swagger/OpenAPI Configuration
 * Enables interactive API documentation at: /swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Job Board Platform API")
                        .description("Hệ thống tuyển dụng online ")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Yoedu Team")
                                .email("support@yoedu.com"))
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer_jwt"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer_jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT token (nhận từ /api/login)")
                        )
                );
    }
}
