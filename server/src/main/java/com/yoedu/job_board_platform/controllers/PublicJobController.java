package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.PublicJobApi;

@RestController
@RequestMapping(ApiPaths.BASE + "/public")
public class PublicJobController implements PublicJobApi {

    @GetMapping("/jobs")
    public ResponseEntity<?> getJobs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "date_created") String sortBy
    ) {
        return ResponseEntity.ok("Danh sách việc");
    }

    @GetMapping("/jobs/search")
    public ResponseEntity<?> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String location
    ) {
        return ResponseEntity.ok("Kết quả tìm kiếm");
    }

    @GetMapping("/jobs/{id}")
    public ResponseEntity<?> getJobDetail(@PathVariable Long id) {
        return ResponseEntity.ok("Chi tiết job");
    }

    @GetMapping("/jobs/filter-options")
    public ResponseEntity<?> getFilterOptions() {
        return ResponseEntity.ok("Filter options");
    }

    @GetMapping("/companies/{id}")
    public ResponseEntity<?> getCompanyInfo(@PathVariable Long id) {
        return ResponseEntity.ok("Thông tin công ty");
    }
}
