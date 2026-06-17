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

import com.yoedu.job_board_platform.controllers.api.ProfileApi;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.CandidateProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileRequest;
import com.yoedu.job_board_platform.dtos.profile.EmployerProfileResponse;
import com.yoedu.job_board_platform.dtos.profile.ResumeRequest;
import com.yoedu.job_board_platform.dtos.profile.ResumeResponse;
import com.yoedu.job_board_platform.services.ProfileService;
import com.yoedu.job_board_platform.services.ResumeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController implements ProfileApi {
	private final ProfileService profileService;
	private final ResumeService resumeService;

	@GetMapping("/candidate")
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResponseEntity<CandidateProfileResponse> getCandidateProfile() {
		return ResponseEntity.ok(profileService.getCurrentCandidateProfile());
	}

	@GetMapping("/employer")
	@PreAuthorize("hasRole('EMPLOYER')")
	public ResponseEntity<EmployerProfileResponse> getEmployerProfile() {
		return ResponseEntity.ok(profileService.getCurrentEmployerProfile());
	}

	@PutMapping("/candidate")
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResponseEntity<CandidateProfileResponse> updateCandidateProfile(
			@Valid @RequestBody CandidateProfileRequest request) {
		return ResponseEntity.ok(profileService.updateCurrentCandidateProfile(request));
	}

	@PutMapping("/employer")
	@PreAuthorize("hasRole('EMPLOYER')")
	public ResponseEntity<EmployerProfileResponse> updateEmployerProfile(
			@Valid @RequestBody EmployerProfileRequest request) {
		return ResponseEntity.ok(profileService.updateCurrentEmployerProfile(request));
	}

	@PostMapping("/avatar")
	public ResponseEntity<String> uploadProfileAvatar(@RequestParam MultipartFile file) {
		return ResponseEntity.ok(profileService.uploadAvatar(file));
	}

	@PostMapping("/logo")
	@PreAuthorize("hasRole('EMPLOYER')")
	public ResponseEntity<String> uploadCompanyLogo(@RequestParam MultipartFile file) {
		return ResponseEntity.ok(profileService.uploadCompanyLogo(file));
	}

	@GetMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResponseEntity<ResumeResponse> getResume() {
		return ResponseEntity.ok(resumeService.getCurrentResume());
	}

	@PostMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResponseEntity<ResumeResponse> uploadResume(
			@RequestParam MultipartFile file,
			@RequestParam(required = false) String title) {
		return ResponseEntity.ok(resumeService.uploadResume(file, title));
	}

	@PutMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResponseEntity<ResumeResponse> updateResume(@Valid @RequestBody ResumeRequest request) {
		return ResponseEntity.ok(resumeService.updateResume(request));
	}

	@DeleteMapping("/resume")
	@PreAuthorize("hasRole('CANDIDATE')")
	public ResponseEntity<Void> deleteResume() {
		resumeService.deleteResume();
		return ResponseEntity.noContent().build();
	}

	@GetMapping("/resume/download")
	@PreAuthorize("hasRole('CANDIDATE')")
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
	public ResponseEntity<Resource> previewResume() {
		var resource = resumeService.downloadResume();
		return ResponseEntity.ok()
				.contentType(MediaType.APPLICATION_PDF)
				.header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"cv.pdf\"")
				.body(resource);
	}
}
