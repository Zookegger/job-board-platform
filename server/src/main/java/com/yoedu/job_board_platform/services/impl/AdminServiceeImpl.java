package com.yoedu.job_board_platform.services.impl;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.yoedu.job_board_platform.services.AdminService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
