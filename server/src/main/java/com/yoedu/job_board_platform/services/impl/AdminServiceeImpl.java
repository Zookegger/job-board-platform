package com.yoedu.job_board_platform.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.services.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * Triển khai AdminService. Xử lý phê duyệt/từ chối công ty.
 * (Chưa triển khai đầy đủ — các phương thức đang ném UnsupportedOperationException.)
 */
public class AdminServiceeImpl implements AdminService {@Override
    public void approveCompany(UUID companyId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'approveCompany'");
    }

    @Override
    public void rejectCompany(UUID companyId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rejectCompany'");
    }
    
}
