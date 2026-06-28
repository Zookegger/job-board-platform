package com.yoedu.job_board_platform.services;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.job.JobListResponse;
import com.yoedu.job_board_platform.dtos.job.JobRequest;
import com.yoedu.job_board_platform.dtos.job.JobResponse;
import com.yoedu.job_board_platform.dtos.job.JobSearchRequest;
import com.yoedu.job_board_platform.models.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

/**
 * Service quản lý tin tuyển dụng.
 * Xử lý CRUD, gửi duyệt và truy vấn tin tuyển dụng của nhà tuyển dụng.
 */
public interface JobService {

    /**
     * Tạo một tin tuyển dụng mới ở trạng thái DRAFT cho nhà tuyển dụng hiện tại.
     * Tự động sinh slug duy nhất từ tiêu đề.
     *
     * @param employerId UUID của nhà tuyển dụng
     * @param request    thông tin tin tuyển dụng
     * @return JobResponse thông tin tin tuyển dụng sau khi tạo
     * @throws NotFoundException  nếu không tìm thấy người dùng
     * @throws BadRequestException nếu danh mục không tồn tại
     */
    JobResponse createJob(UUID employerId, JobRequest request);

    /**
     * Cập nhật thông tin tin tuyển dụng.
     * Nếu tin không ở trạng thái DRAFT, tự động đưa về DRAFT.
     * Nếu tiêu đề thay đổi, sinh lại slug duy nhất.
     *
     * @param jobId      UUID của tin tuyển dụng
     * @param employerId UUID của nhà tuyển dụng
     * @param request    thông tin cập nhật
     * @return JobResponse thông tin tin tuyển dụng sau khi cập nhật
     * @throws NotFoundException   nếu không tìm thấy tin hoặc người dùng
     * @throws ForbiddenException   nếu tin không thuộc về công ty của nhà tuyển dụng
     * @throws BadRequestException nếu danh mục không tồn tại
     */
    JobResponse updateJob(UUID jobId, UUID employerId, JobRequest request);

    /**
     * Lấy danh sách tin tuyển dụng của công ty (có phân trang).
     * Có thể lọc theo trạng thái.
     *
     * @param employerId UUID của nhà tuyển dụng
     * @param status     lọc theo trạng thái (có thể null để lấy tất cả)
     * @param page       số trang (bắt đầu từ 0)
     * @param size       số phần tử mỗi trang
     * @return trang dữ liệu JobListResponse
     * @throws NotFoundException nếu không tìm thấy người dùng
     */
    Page<JobListResponse> getEmployerJobs(UUID employerId, JobStatus status, int page, int size);

    /**
     * Lấy chi tiết một tin tuyển dụng (bao gồm kỹ năng).
     * Chỉ trả về nếu tin thuộc về công ty của nhà tuyển dụng hiện tại.
     *
     * @param jobId      UUID của tin tuyển dụng
     * @param employerId UUID của nhà tuyển dụng
     * @return JobResponse thông tin chi tiết tin tuyển dụng
     * @throws NotFoundException  nếu không tìm thấy tin hoặc người dùng
     * @throws ForbiddenException  nếu tin không thuộc về công ty của nhà tuyển dụng
     */
    JobResponse getJobDetail(UUID jobId, UUID employerId);

    /**
     * Xoá vĩnh viễn một tin tuyển dụng và toàn bộ kỹ năng liên quan.
     *
     * @param jobId      UUID của tin tuyển dụng
     * @param employerId UUID của nhà tuyển dụng
     * @throws NotFoundException  nếu không tìm thấy tin hoặc người dùng
     * @throws ForbiddenException  nếu tin không thuộc về công ty của nhà tuyển dụng
     */
    void deleteJob(UUID jobId, UUID employerId);

    /**
     * Gửi tin tuyển dụng cho admin phê duyệt.
     * Chuyển trạng thái từ DRAFT sang PENDING_APPROVAL.
     *
     * @param jobId      UUID của tin tuyển dụng
     * @param employerId UUID của nhà tuyển dụng
     * @throws NotFoundException   nếu không tìm thấy tin hoặc người dùng
     * @throws ForbiddenException   nếu tin không thuộc về công ty của nhà tuyển dụng
     * @throws BadRequestException nếu tin không ở trạng thái DRAFT
     */
    void submitForReview(UUID jobId, UUID employerId);

    /**
     * Tìm kiếm việc làm công khai với bộ lọc và phân trang.
     * Chỉ trả về các job ở trạng thái ACTIVE.
     *
     * @param request  bộ lọc tìm kiếm
     * @param pageable thông tin phân trang
     * @return trang kết quả JobListResponse
     */
    Page<JobListResponse> searchPublicJobs(JobSearchRequest request, Pageable pageable);

    /**
     * Lấy chi tiết việc làm công khai theo slug.
     *
     * @param slug slug của công việc
     * @return JobResponse thông tin chi tiết
     * @throws NotFoundException nếu không tìm thấy
     */
    JobResponse getPublicJobDetail(String slug);

    /**
     *
     * @param jobId
     * @param pageable
     *
     * @return
     */
    Page<JobListResponse> getRelatedJobs(UUID jobId, Pageable pageable);
}
