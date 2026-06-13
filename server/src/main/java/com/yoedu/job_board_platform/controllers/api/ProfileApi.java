package com.yoedu.job_board_platform.controllers.api;

import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.ResumeRequest;
import com.yoedu.job_board_platform.dtos.profile.ResumeResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Hồ sơ người dùng", description = "Xem và cập nhật hồ sơ cá nhân, quản lý CV.")
public interface ProfileApi {

    @Operation(summary = "Hồ sơ ứng viên", description = "Lấy hồ sơ chi tiết của ứng viên.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin hồ sơ ứng viên", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
    })
    ResponseEntity<CandidateProfileResponse> getCandidateProfile();

    @Operation(summary = "Hồ sơ nhà tuyển dụng", description = "Lấy hồ sơ chi tiết của nhà tuyển dụng bao gồm thông tin công ty.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin hồ sơ nhà tuyển dụng", content = @Content),
            @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
    })
    ResponseEntity<EmployerProfileResponse> getEmployerProfile();

    @Operation(summary = "Cập nhật hồ sơ ứng viên", description = "Cập nhật thông tin hồ sơ của ứng viên.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
    })
    ResponseEntity<CandidateProfileResponse> updateCandidateProfile(CandidateProfileRequest request);

    @Operation(summary = "Cập nhật hồ sơ nhà tuyển dụng", description = "Cập nhật thông tin hồ sơ của nhà tuyển dụng bao gồm thông tin công ty.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
            @ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
    })
    ResponseEntity<EmployerProfileResponse> updateEmployerProfile(EmployerProfileRequest request);

    ResponseEntity<String> uploadProfileAvatar(
            @Parameter(description = "Ảnh avatar (multipart/form-data, max 5MB)", required = true) MultipartFile file);

    @Operation(summary = "Thông tin CV", description = "Lấy thông tin CV đã upload của ứng viên (tiêu đề, dung lượng, ngày tạo...).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin CV", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
    })
    ResponseEntity<ResumeResponse> getResume();

    @Operation(summary = "Upload CV (PDF)", description = """
            Upload file CV PDF.
            Định dạng: PDF. Dung lượng tối đa: 10MB.
            Nếu đã có CV, file cũ sẽ được thay thế.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Upload CV thành công", content = @Content),
            @ApiResponse(responseCode = "400", description = "File không hợp lệ", content = @Content)
    })
    ResponseEntity<ResumeResponse> uploadResume(
            @Parameter(description = "File PDF CV (multipart/form-data, max 10MB)", required = true) MultipartFile file,
            @Parameter(description = "Tiêu đề CV (tùy chọn)") String title);

    @Operation(summary = "Cập nhật thông tin CV", description = "Cập nhật tiêu đề CV (không thay đổi file).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
    })
    ResponseEntity<ResumeResponse> updateResume(ResumeRequest request);

    @Operation(summary = "Xóa CV", description = "Xóa vĩnh viễn CV PDF đã upload.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Xóa thành công", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
    })
    ResponseEntity<Void> deleteResume();

    @Operation(summary = "Tải CV (PDF)", description = "Tải file PDF CV đã upload.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "File PDF CV", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
    })
    ResponseEntity<Resource> downloadResume();

    @Operation(summary = "Xem trước CV", description = "Xem trước CV: trả về thông tin metadata + URL tải. Frontend dùng iframe/react-pdf để hiển thị.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Thông tin CV", content = @Content),
            @ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
    })
    ResponseEntity<Resource> previewResume();
}
