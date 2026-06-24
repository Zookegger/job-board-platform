package com.yoedu.job_board_platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class JobBoardPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.run(JobBoardPlatformApplication.class, args);
	}

}
