package com.yoedu.job_board_platform.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.yoedu.job_board_platform.dtos.auth.AuthResponse;
import com.yoedu.job_board_platform.dtos.auth.AuthResult;

@Mapper(componentModel = "spring")
/**
 * MapStruct mapper cho xác thực.
 * Chuyển đổi AuthResult thành AuthResponse với tokenType mặc định là "Bearer".
 */
public interface AuthMapper {

    @Mapping(target = "tokenType", constant = "Bearer")
    AuthResponse toAuthResponse(AuthResult authResult);
}
