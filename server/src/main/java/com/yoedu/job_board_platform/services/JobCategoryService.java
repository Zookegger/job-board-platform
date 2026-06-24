package com.yoedu.job_board_platform.services;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.dtos.job.JobCategoryRequest;
import com.yoedu.job_board_platform.models.JobCategory;

/**
 * Service quản lý danh mục ngành nghề.
 */
public interface JobCategoryService {

    /**
     * Lấy toàn bộ danh mục ngành nghề.
     *
     * @return Danh sách tất cả danh mục ngành nghề.
     */
    List<JobCategory> getAllCategories();

    /**
     * Lấy danh sách danh mục ngành nghề có phân trang và tìm kiếm theo từ khóa.
     *
     * @param keyword  Từ khóa tìm kiếm theo tên danh mục. Có thể để trống.
     * @param pageable Thông tin phân trang và sắp xếp.
     * @return Trang dữ liệu chứa các danh mục ngành nghề phù hợp.
     */
    Page<JobCategory> getAllCategoriesPage(String keyword, Pageable pageable);

    /**
     * Tạo mới một danh mục ngành nghề.
     *
     * @param request Thông tin danh mục cần tạo.
     * @return Danh mục ngành nghề đã được tạo.
     */
    JobCategory createCategory(JobCategoryRequest request);

    /**
     * Cập nhật thông tin danh mục ngành nghề.
     *
     * @param id      ID của danh mục cần cập nhật.
     * @param request Thông tin cập nhật.
     * @return Danh mục ngành nghề sau khi cập nhật.
     */
    JobCategory updateCategory(Integer id, JobCategoryRequest request);

    /**
     * Xóa một danh mục ngành nghề.
     *
     * @param id ID của danh mục cần xóa.
     */
    void deleteCategory(Integer id);
}