package com.yoedu.job_board_platform.services;

import java.util.List;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import com.yoedu.job_board_platform.dtos.profile.ResumeRequest;
import com.yoedu.job_board_platform.dtos.profile.ResumeResponse;

/**
 * Service quản lý CV (Resume) của ứng viên.
 * Hỗ trợ upload, cập nhật, xóa, tải xuống và danh sách CV.
 */
public interface ResumeService {
    ResumeResponse getCurrentResume();
    ResumeResponse uploadResume(MultipartFile file, String title);
    ResumeResponse updateResume(ResumeRequest request);
    void deleteResume();
    Resource downloadResume();
    List<ResumeResponse> listResumes();
}
