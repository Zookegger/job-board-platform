package com.yoedu.job_board_platform;

import org.springframework.boot.SpringApplication;

public class TestJobBoardPlatformApplication {

	public static void main(String[] args) {
		SpringApplication.from(JobBoardPlatformApplication::main).with(TestcontainersConfiguration.class).run(args);
	}

}
