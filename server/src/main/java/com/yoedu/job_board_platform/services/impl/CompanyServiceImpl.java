package com.yoedu.job_board_platform.services.impl;

import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ForbiddenException;
import com.yoedu.job_board_platform.common.exceptions.NotFoundException;
import com.yoedu.job_board_platform.dtos.company.CompanyRequest;
import com.yoedu.job_board_platform.dtos.company.CompanyResponse;
import com.yoedu.job_board_platform.mappers.CompanyMapper;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.CompanyService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
/**
 * Triển khai CompanyService. Xử lý các thao tác liên quan đến công ty
 * như cập nhật thông tin, lấy thông tin công ty của nhà tuyển dụng hiện tại.
 */
public class CompanyServiceImpl implements CompanyService {
    private final CompanyRepository companyRepository;
    private final CompanyMapper companyMapper;
    private final UserRepository userRepository;
    private final JobRepository jobRepository;

    @Override
    @Transactional
    public CompanyResponse update(UUID userId, CompanyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        if (!user.getRole().equals(UserRole.EMPLOYER)) {
            throw new ForbiddenException("Chỉ có nhà tuyển dụng mới dùng được chức năng này");
        }

        Company company = user.getProfile().getEmployerDetail().getCompany();

        companyMapper.updateEntity(request, company);

        return companyMapper.toResponse(companyRepository.save(company));
    }

    @Override
    public CompanyResponse findCompanyByEmployerId(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Không tìm thấy người dùng"));

        if (!user.getRole().equals(UserRole.EMPLOYER)) {
            throw new BadRequestException("Người dùng không phải nhà tuyển dụng");
        }

        Company company = user.getProfile().getEmployerDetail().getCompany();
        return companyMapper.toResponse(company);
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

}
