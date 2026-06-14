package com.yoedu.job_board_platform.services;

import java.util.List;
import java.util.UUID;

import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;

public interface CompanyStatusService {

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
