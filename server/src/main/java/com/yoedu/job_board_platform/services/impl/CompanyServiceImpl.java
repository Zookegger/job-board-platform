package com.yoedu.job_board_platform.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.company.ApprovalLogResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;
import com.yoedu.job_board_platform.dtos.company.CompanyStatusResponse;
import com.yoedu.job_board_platform.mappers.CompanyMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyApprovalLog;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyReviewReason;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyApprovalLogRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.CompanyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Triển khai CompanyService. Xử lý các thao tác liên quan đến công ty
 * như cập nhật thông tin, lấy thông tin công ty của nhà tuyển dụng hiện tại.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyApprovalLogRepository companyApprovalLogRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @Override
    /**
     * Cập nhật thông tin công ty.
     * Kiểm tra quyền EMPLOYER, cập nhật các trường không null,
     * và đưa công ty về trạng thái chờ duyệt nếu companyName hoặc taxCode thay đổi.
     */
    public CompanyResponse update(UUID userId, CompanyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        if (!user.getRole().equals(UserRole.EMPLOYER)) {
            throw new ForbiddenException("Chỉ có nhà tuyển dụng mới dùng được chức năng này");
        }

        Company company = user.getProfile().getEmployerDetail().getCompany();

        boolean requiresReview = hasChanged(request.companyName(), company.getCompanyName())
                || hasChanged(request.taxCode(), company.getTaxCode());

        companyMapper.updateEntity(request, company);

        if (requiresReview && company.isApproved()) {
            company.markForReview(CompanyReviewReason.INFO_UPDATED);
        }

        Company saved = companyRepository.save(company);

        return companyMapper.toResponse(saved);
    }

    @Override
    public CompanyResponse findCompanyByEmployerId(UUID userId) {
        return companyMapper.toResponse(getCompanyEntityForEmployer(userId));
    }

    @Override
    public CompanyStatusResponse getStatusByEmployerId(UUID userId) {
        return companyMapper.toStatusResponse(getCompanyEntityForEmployer(userId));
    }

    @Override
    public List<ApprovalLogResponse> getHistoryByEmployerId(UUID userId) {
        Company company = getCompanyEntityForEmployer(userId);
        List<CompanyApprovalLog> logs = companyApprovalLogRepository
                .findByCompanyIdOrderByCreatedAtDesc(company.getId());
        return logs.stream().map(companyMapper::toApprovalLogResponse).toList();
    }

    @Override
    public CompanyResponse getCompanyByJobPost(UUID jobPostId) {
        Job jobPost = jobRepository.findById(jobPostId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy bài đăng tuyển dụng yêu cầu"));

        return companyMapper.toResponse(jobPost.getCompany());
    }

    @Override
    public List<CompanyResponse> listCompanies() {
        return companyRepository.findAll().stream().map(companyMapper::toResponse).toList();
    }

    /**
     * Kiểm tra xem giá trị mới có khác giá trị cũ hay không.
     * Trả về false nếu giá trị mới là null (không có thay đổi).
     *
     * @param newValue giá trị mới (có thể null)
     * @param oldValue giá trị cũ
     * @return true nếu newValue khác null và khác oldValue
     */
    private boolean hasChanged(String newValue, String oldValue) {
        return newValue != null && !newValue.equals(oldValue);
    }

    /**
     * Tìm Company entity cho employer theo userId.
     * Ném NotFoundException nếu user không tồn tại,
     * BadRequestException nếu không phải EMPLOYER,
     * ResourceNotFoundException nếu chưa có thông tin công ty.
     */
    private Company getCompanyEntityForEmployer(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));
        if (!user.getRole().equals(UserRole.EMPLOYER)) {
            throw new BadRequestException("Người dùng không phải nhà tuyển dụng");
        }
        CompanyEmployerDetail detail = user.getProfile().getEmployerDetail();
        if (detail == null) {
            throw new ResourceNotFoundException("Không tìm thấy thông tin công ty cho tài khoản này");
        }
        return detail.getCompany();
    }
}
