package com.yoedu.job_board_platform;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class JobBoardPlatformApplicationTests {

	@Test
	void contextLoads() {
	}

}
