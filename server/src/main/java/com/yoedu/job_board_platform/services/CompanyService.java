package com.yoedu.job_board_platform.services;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.dtos.publics.PublicCompanyJobResponse;
import com.yoedu.job_board_platform.dtos.publics.PublicCompanyResponse;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.JobCategory;

/**
 * Service quản lý thông tin công ty.
 * Xử lý các thao tác CRUD và truy vấn thông tin công ty của nhà tuyển dụng.
 */
public interface CompanyService {

    /**
     * Cập nhật thông tin công ty của nhà tuyển dụng hiện tại.
     * Chỉ nhà tuyển dụng mới có quyền cập nhật thông tin công ty của mình.
     * Các trường null trong request được bỏ qua.
     * Nếu companyName hoặc taxCode thay đổi và công ty đã được duyệt,
     * công ty sẽ bị đưa về trạng thái chờ duyệt lại.
     *
     * @param userId  UUID của nhà tuyển dụng
     * @param request thông tin công ty cần cập nhật
     * @return CompanyResponse thông tin công ty sau khi cập nhật
     * @throws ForbiddenException nếu người dùng hiện tại không phải nhà tuyển dụng
     */
    CompanyResponse update(UUID userId, CompanyRequest request);

    /**
     * Lấy thông tin công ty theo nhà tuyển dụng.
     *
     * @param userUuid UUID của nhà tuyển dụng
     * @return CompanyResponse thông tin công ty
     * @throws BadRequestException nếu người dùng không phải nhà tuyển dụng
     */
    CompanyResponse findCompanyByEmployerId(UUID userUuid);

    /**
     * Lấy thông tin công ty từ bài đăng tuyển dụng.
     *
     * @param jobPostId UUID của bài đăng tuyển dụng
     * @return CompanyResponse thông tin công ty
     * @throws NotFoundException nếu không tìm thấy bài đăng tuyển dụng
     */
    CompanyResponse getCompanyByJobPost(UUID jobPostId);

    /**
     * Lấy danh sách tất cả các công ty.
     *
     * @return danh sách CompanyResponse
     */
    Page<Company> listCompanies(String keyword, JobCategory jobCategory, Pageable pageable);

    PublicCompanyResponse getCompanyPublic(String slug);

    /**
     * Lấy danh sách tin tuyển dụng đang Active của một công ty, có phân trang.
     *
     * @param slug     slug của công ty
     * @param pageable thông tin phân trang
     * @return trang kết quả chứa danh sách PublicCompanyJobResponse
     */
    Page<PublicCompanyJobResponse> getPublicJobsByCompany(String slug, Pageable pageable);

    /**
     * Lấy trạng thái phê duyệt của công ty thuộc employer đang đăng nhập.
     *
     * @param employerId UUID của employer (= userId)
     * @return thông tin trạng thái công ty
     */
    CompanyStatusResponse getStatusByEmployerId(UUID employerId);

    /**
     * Lấy lịch sử thay đổi trạng thái phê duyệt của công ty thuộc employer.
     *
     * @param employerId UUID của employer (= userId)
     * @return danh sách log sắp xếp mới nhất lên đầu
     */
    List<ApprovalLogResponse> getHistoryByEmployerId(UUID employerId);
}
