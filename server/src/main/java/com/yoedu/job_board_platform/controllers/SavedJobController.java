package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;
import com.yoedu.job_board_platform.controllers.api.SavedJobApi;

@RestController
@RequestMapping(ApiPaths.BASE + "/saved-jobs")
@PreAuthorize("hasRole('CANDIDATE')")
public class SavedJobController implements SavedJobApi {

    @GetMapping
    public ResponseEntity<?> getSavedJobs(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok("Danh sách việc đã lưu");
    }

    @PostMapping
    public ResponseEntity<?> saveJob(@RequestParam Long jobId) {
        return ResponseEntity.ok("Lưu việc thành công");
    }

    @DeleteMapping("/{jobId}")
    public ResponseEntity<?> unsaveJob(@PathVariable Long jobId) {
        return ResponseEntity.ok("Bỏ lưu thành công");
    }
}
