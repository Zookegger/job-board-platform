package com.yoedu.job_board_platform.controllers.api;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;

import com.yoedu.job_board_platform.dtos.skill.CandidateSkillResponse;
import com.yoedu.job_board_platform.dtos.skill.SkillFilterRequest;
import com.yoedu.job_board_platform.dtos.skill.SkillResponse;
import com.yoedu.job_board_platform.dtos.skill.UpdateCandidateSkillsRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Kỹ năng (Skills)", description = "API công khai và của ứng viên để quản lý kỹ năng")
public interface SkillApi {

    @Operation(summary = "Danh sách kỹ năng hoạt động", description = "Trả về danh sách kỹ năng đang active dựa trên từ khóa tìm kiếm. Chỉ trả về kỹ năng có isActive = true (mặc định). Không yêu cầu đăng nhập.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách kỹ năng tìm thấy thành công"),
    })
    ResponseEntity<Page<SkillResponse>> getAllSkills(
            @ParameterObject @PageableDefault(page = 0, size = 20) Pageable pageable,
            @Parameter(description = "Bộ lọc: từ khóa tên kỹ năng (không phân biệt hoa-thường) và trạng thái hoạt động") SkillFilterRequest request);

    @Operation(summary = "Kỹ năng của ứng viên", description = "Lấy danh sách kỹ năng của ứng viên đang đăng nhập. Yêu cầu quyền truy cập CANDIDATE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Danh sách kỹ năng của ứng viên thu thập thành công"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (Forbidden)"),
    })
    ResponseEntity<List<CandidateSkillResponse>> getCandidateSkills();

    @Operation(summary = "Cập nhật kỹ năng ứng viên", description = "Thay thế toàn bộ danh sách kỹ năng hiện tại của ứng viên đang đăng nhập. Yêu cầu quyền truy cập CANDIDATE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật danh sách kỹ năng thành công"),
            @ApiResponse(responseCode = "400", description = "Dữ liệu yêu cầu không hợp lệ hoặc ID kỹ năng không tồn tại"),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (Forbidden)"),
    })
    ResponseEntity<List<CandidateSkillResponse>> updateCandidateSkills(UpdateCandidateSkillsRequest request);
}