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

    /**
     * Lấy CV hiện tại của ứng viên.
     *
     * @return ResumeResponse thông tin CV
     */
    ResumeResponse getCurrentResume();

    /**
     * Upload CV mới cho ứng viên hiện tại.
     * Chỉ chấp nhận định dạng PDF, kích thước tối đa 10MB.
     * Nếu đã có CV, file cũ sẽ được ghi đè.
     *
     * @param file  file PDF cần upload
     * @param title tiêu đề của CV
     * @return ResumeResponse thông tin CV sau khi upload
     */
    ResumeResponse uploadResume(MultipartFile file, String title);

    /**
     * Cập nhật thông tin CV (tiêu đề).
     *
     * @param request thông tin CV cần cập nhật
     * @return ResumeResponse thông tin CV sau khi cập nhật
     */
    ResumeResponse updateResume(ResumeRequest request);

    /**
     * Xóa CV của ứng viên hiện tại.
     * Xóa cả file trên disk và bản ghi trong cơ sở dữ liệu.
     */
    void deleteResume();

    /**
     * Tải xuống file CV của ứng viên hiện tại.
     *
     * @return Resource file CV để tải xuống
     */
    Resource downloadResume();

    /**
     * Lấy danh sách tất cả CV (dành cho admin).
     *
     * @return danh sách ResumeResponse
     */
    List<ResumeResponse> listResumes();
}
