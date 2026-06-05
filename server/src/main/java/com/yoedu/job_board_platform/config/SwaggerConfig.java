package com.yoedu.job_board_platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

/**
 * Swagger/OpenAPI Configuration
 * Enables interactive API documentation at: /swagger-ui.html
 */
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server()
                        .url("http://localhost:5000")
                        .description("Local development server"))
                .info(new Info()
                        .title("Job Board Platform API")
                        .description("""
                                API hệ thống tuyển dụng trực tuyến (Job Board Platform).
                                
                                ## Tính năng chính:
                                * **Xác thực & Phân quyền** — Đăng ký/đăng nhập, JWT + refresh token, phân quyền theo 3 vai trò (Admin, Employer, Candidate)
                                * **Quản lý tin tuyển dụng** — CRUD, duyệt tin, tìm kiếm/lọc, trạng thái (Draft, Pending Approval, Active, Expired, Rejected)
                                * **Quản lý hồ sơ ứng viên** — Profile, CV online/PDF, kỹ năng
                                * **Ứng tuyển & Timeline** — Nộp đơn, theo dõi trạng thái (Pending → Reviewing → Interview → Hired/Rejected), rút đơn
                                * **Quản trị hệ thống** — Dashboard, quản lý user/công ty/tin, kiểm duyệt, quản lý ngành nghề
                                * **Lưu việc làm** — Yêu thích và quản lý danh sách việc quan tâm
                                
                                ## Xác thực
                                API sử dụng **JWT Bearer Token**. Sau khi đăng nhập, gửi token qua header:
                                ```
                                Authorization: Bearer <access_token>
                                ```
                                Token cũng được set trong HttpOnly cookie.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Yoedu Team")
                                .email("support@yoedu.com")
                                .url("https://yoedu.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0"))
                )
                .addSecurityItem(new SecurityRequirement().addList("bearer_jwt"))
                .components(new io.swagger.v3.oas.models.Components()
                        .addSecuritySchemes("bearer_jwt", new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("""
                                        JWT access token nhận từ endpoint `/api/auth/login` hoặc `/api/auth/refresh-token`.
                                        
                                        Token được truyền qua header:
                                        ```
                                        Authorization: Bearer <token>
                                        ```
                                        
                                        Hoặc qua HttpOnly cookie `access_token`.
                                        """)
                        )
                );
    }
}
