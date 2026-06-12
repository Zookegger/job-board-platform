package com.yoedu.job_board_platform.controllers;

import java.io.IOException;

import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.ResumeRequest;
import com.yoedu.job_board_platform.dtos.profile.ResumeResponse;
import com.yoedu.job_board_platform.services.ProfileService;
import com.yoedu.job_board_platform.services.ResumeService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@Tag(name = "Hồ sơ người dùng", description = "Xem và cập nhật hồ sơ cá nhân, quản lý CV.")
@RequiredArgsConstructor
public class ProfileController {
	private final ProfileService profileService;
	private final ResumeService resumeService;

	@GetMapping("/candidate")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Hồ sơ ứng viên", description = "Lấy hồ sơ chi tiết của ứng viên.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Thông tin hồ sơ ứng viên", content = @Content),
			@ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
	})
	public ResponseEntity<CandidateProfileResponse> getCandidateProfile() {
		return ResponseEntity.ok(profileService.getCurrentCandidateProfile());
	}

	@GetMapping("/employer")
	@PreAuthorize("hasRole('EMPLOYER')")
	@Operation(summary = "Hồ sơ nhà tuyển dụng", description = "Lấy hồ sơ chi tiết của nhà tuyển dụng bao gồm thông tin công ty.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Thông tin hồ sơ nhà tuyển dụng", content = @Content),
			@ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
	})
	public ResponseEntity<EmployerProfileResponse> getEmployerProfile() {
		return ResponseEntity.ok(profileService.getCurrentEmployerProfile());
	}

	@PutMapping("/candidate")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Cập nhật hồ sơ ứng viên", description = "Cập nhật thông tin hồ sơ của ứng viên.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
			@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
	})
	public ResponseEntity<CandidateProfileResponse> updateCandidateProfile(
			@Valid @RequestBody CandidateProfileRequest request) {
		return ResponseEntity.ok(profileService.updateCurrentCandidateProfile(request));
	}

	@PutMapping("/employer")
	@PreAuthorize("hasRole('EMPLOYER')")
	@Operation(summary = "Cập nhật hồ sơ nhà tuyển dụng", description = "Cập nhật thông tin hồ sơ của nhà tuyển dụng bao gồm thông tin công ty.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
			@ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content),
			@ApiResponse(responseCode = "404", description = "Không tìm thấy hồ sơ", content = @Content)
	})
	public ResponseEntity<EmployerProfileResponse> updateEmployerProfile(
			@Valid @RequestBody EmployerProfileRequest request) {
		return ResponseEntity.ok(profileService.updateCurrentEmployerProfile(request));
	}

	@PostMapping("/avatar")
	public ResponseEntity<String> uploadProfileAvatar(
			@Parameter(description = "Ảnh avatar (multipart/form-data, max 5MB)", required = true) @RequestParam MultipartFile file) {
		return ResponseEntity.ok(profileService.uploadAvatar(file));
	}

	// -----------------------------------------------------------------
	// Resume (CV) — single PDF per candidate
	// -----------------------------------------------------------------

	@GetMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Thông tin CV", description = "Lấy thông tin CV đã upload của ứng viên (tiêu đề, dung lượng, ngày tạo...).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Thông tin CV", content = @Content),
			@ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
	})
	public ResponseEntity<ResumeResponse> getResume() {
		return ResponseEntity.ok(resumeService.getCurrentResume());
	}

	@PostMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Upload CV (PDF)", description = """
			Upload file CV PDF.
			Định dạng: PDF. Dung lượng tối đa: 10MB.
			Nếu đã có CV, file cũ sẽ được thay thế.
			""")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Upload CV thành công", content = @Content),
			@ApiResponse(responseCode = "400", description = "File không hợp lệ", content = @Content)
	})
	public ResponseEntity<ResumeResponse> uploadResume(
			@Parameter(description = "File PDF CV (multipart/form-data, max 10MB)", required = true) @RequestParam MultipartFile file,
			@Parameter(description = "Tiêu đề CV (tùy chọn)") @RequestParam(required = false) String title) {
		return ResponseEntity.ok(resumeService.uploadResume(file, title));
	}

	@PutMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Cập nhật thông tin CV", description = "Cập nhật tiêu đề CV (không thay đổi file).")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Cập nhật thành công", content = @Content),
			@ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
	})
	public ResponseEntity<ResumeResponse> updateResume(@Valid @RequestBody ResumeRequest request) {
		return ResponseEntity.ok(resumeService.updateResume(request));
	}

	@DeleteMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Xóa CV", description = "Xóa vĩnh viễn CV PDF đã upload.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "Xóa thành công", content = @Content),
			@ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
	})
	public ResponseEntity<Void> deleteResume() {
		resumeService.deleteResume();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/resume/download")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Tải CV (PDF)", description = "Tải file PDF CV đã upload.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "File PDF CV", content = @Content),
			@ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
	})
	public ResponseEntity<Resource> downloadResume() {
		Resource resource = resumeService.downloadResume();

		long contentLength;
		try {
			contentLength = resource.contentLength();
		} catch (IOException e) {
			throw new RuntimeException("Không thể xác định kích thước file");
		}

		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.contentLength(contentLength)
				.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"cv.pdf\"")
				.body(resource);
	}

	@GetMapping("/resume/preview")
	@PreAuthorize("hasRole('CANDIDATE')")
	@Operation(summary = "Xem trước CV", description = "Xem trước CV: trả về thông tin metadata + URL tải. Frontend dùng iframe/react-pdf để hiển thị.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "Thông tin CV", content = @Content),
			@ApiResponse(responseCode = "404", description = "Chưa upload CV", content = @Content)
	})
	public ResponseEntity<Resource> previewResume() {
		var resource = resumeService.downloadResume();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cv.pdf\"")
				.body(resource);
	}
}
