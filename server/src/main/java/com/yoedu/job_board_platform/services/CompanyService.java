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
     *
     * @param request thông tin công ty cần cập nhật
     * @return CompanyResponse thông tin công ty sau khi cập nhật
     * @throws ForbiddenException nếu người dùng hiện tại không phải nhà tuyển dụng
     */
    CompanyResponse update(UUID userId, CompanyRequest request);

    /**
     * Lấy thông tin công ty theo nhà tuyển dụng.
     *
     * @return CompanyResponse thông tin công ty
     */
    CompanyResponse findCompanyByEmployerId(UUID userUuid);

    /**
     * Lấy thông tin công ty của nhà tuyển dụng.
     *
     * @return CompanyResponse thông tin công ty
     * @throws ForbiddenException nếu người dùng hiện tại không phải nhà tuyển dụng
     */
    CompanyResponse getCompanyFromRecruiter();

    /**
     * Lấy thông tin công ty từ bài đăng tuyển dụng.
     *
     * @return CompanyResponse thông tin công ty
     */
    CompanyResponse getCompanyByJobPost(UUID jobPostId);

    /**
     * Lấy danh sách tất cả các công ty.
     *
     * @return danh sách CompanyResponse
     */
    List<CompanyResponse> listCompanies();
}
