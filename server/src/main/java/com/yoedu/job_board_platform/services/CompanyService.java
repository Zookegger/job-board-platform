package com.yoedu.job_board_platform.services;

import java.util.List;
import java.util.UUID;

import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;

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
    List<CompanyResponse> listCompanies();
}
