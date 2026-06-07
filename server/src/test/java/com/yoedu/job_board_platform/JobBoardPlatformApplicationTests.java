package com.yoedu.job_board_platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@ActiveProfiles("test")
@org.junit.jupiter.api.Disabled("Context load verified by AuthControllerValidationTest outer class — avoids duplicate Docker container")
class JobBoardPlatformApplicationTests {

    @Test
    void contextLoads() {
    }
}
