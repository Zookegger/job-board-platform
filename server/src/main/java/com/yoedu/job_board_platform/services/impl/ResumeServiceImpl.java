package com.yoedu.job_board_platform.services.impl;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.yoedu.job_board_platform.common.exceptions.BadRequestException;
import com.yoedu.job_board_platform.common.exceptions.ResourceNotFoundException;
import com.yoedu.job_board_platform.dtos.profile.ResumeRequest;
import com.yoedu.job_board_platform.dtos.profile.ResumeResponse;
import com.yoedu.job_board_platform.mappers.ResumeMapper;
import com.yoedu.job_board_platform.models.CandidateDetail;
import com.yoedu.job_board_platform.models.Resume;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.CandidateDetailRepository;
import com.yoedu.job_board_platform.repositories.ResumeRepository;
import com.yoedu.job_board_platform.services.ResumeService;
import com.yoedu.job_board_platform.services.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ResumeServiceImpl implements ResumeService {
    private final ResumeRepository resumeRepository;
    private final CandidateDetailRepository candidateDetailRepository;
    private final UserService userService;
    private final ResumeMapper resumeMapper;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private CandidateDetail getCurrentCandidateDetail() {
        User user = userService.getCurrentUser();
        return candidateDetailRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thông tin ứng viên"));
    }

    private Resume getCurrentResumeEntity() {
        User user = userService.getCurrentUser();
        return resumeRepository.findByCandidateDetailProfileId(user.getId())
                .orElse(null);
    }

    @Override
    public ResumeResponse getCurrentResume() {
        Resume resume = getCurrentResumeEntity();
        if (resume == null) {
            throw new ResourceNotFoundException("Chưa upload CV");
        }
        return resumeMapper.toResponse(resume);
    }

    @Override
    @Transactional
    public ResumeResponse uploadResume(MultipartFile file, String title) {
        if (file.isEmpty()) {
            throw new BadRequestException("File không được để trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new BadRequestException("Chỉ hỗ trợ định dạng PDF");
        }

        if (file.getSize() > 10 * 1024 * 1024) {
            throw new BadRequestException("File quá lớn. Dung lượng tối đa: 10MB");
        }

        CandidateDetail detail = getCurrentCandidateDetail();
        Resume existingResume = getCurrentResumeEntity();
        OffsetDateTime now = OffsetDateTime.now();

        var resumeDir = Paths.get(uploadDir, "resumes");
        try {
            Files.createDirectories(resumeDir);
        } catch (IOException e) {
            log.error("Lỗi tạo thư mục upload", e);
            throw new RuntimeException("Lỗi tạo thư mục upload", e);
        }

        var fileId = existingResume != null ? existingResume.getId() : UUID.randomUUID();
        var fileName = fileId + ".pdf";
        var targetPath = resumeDir.resolve(fileName);

        try {
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            log.error("Lỗi lưu file CV", e);
            throw new RuntimeException("Lỗi lưu file CV", e);
        }

        Resume resume;
        if (existingResume != null) {
            resume = existingResume;
            resume.setTitle(title != null ? title : resume.getTitle());
            resume.setOriginalFileName(file.getOriginalFilename());
            resume.setFilePath(targetPath.toString());
            resume.setFileSize(file.getSize());
            resume.setFileType("application/pdf");
            resume.setUpdatedAt(now);
        } else {
            resume = new Resume();
            resume.setId(fileId);
            resume.setCandidateDetail(detail);
            resume.setTitle(title != null ? title : "CV của tôi");
            resume.setOriginalFileName(file.getOriginalFilename());
            resume.setFilePath(targetPath.toString());
            resume.setFileSize(file.getSize());
            resume.setFileType("application/pdf");
            resume.setCreatedAt(now);
            resume.setUpdatedAt(now);
        }

        resumeRepository.save(resume);
        return resumeMapper.toResponse(resume);
    }

    @Override
    @Transactional
    public ResumeResponse updateResume(ResumeRequest request) {
        Resume resume = getCurrentResumeEntity();
        if (resume == null) {
            throw new ResourceNotFoundException("Chưa upload CV");
        }

        if (request.title() != null) {
            resume.setTitle(request.title());
        }
        resume.setUpdatedAt(OffsetDateTime.now());
        resumeRepository.save(resume);
        return resumeMapper.toResponse(resume);
    }

    @Override
    @Transactional
    public void deleteResume() {
        Resume resume = getCurrentResumeEntity();
        if (resume == null) {
            throw new ResourceNotFoundException("Chưa upload CV");
        }

        try {
            Files.deleteIfExists(Paths.get(resume.getFilePath()));
        } catch (IOException e) {
            log.warn("Không thể xóa file CV trên disk: {}", resume.getFilePath(), e);
        }

        resumeRepository.delete(resume);
    }

    @Override
    public Resource downloadResume() {
        Resume resume = getCurrentResumeEntity();
        if (resume == null) {
            throw new ResourceNotFoundException("Chưa upload CV");
        }

        try {
            var path = Paths.get(resume.getFilePath());
            var resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new ResourceNotFoundException("Không tìm thấy file CV trên hệ thống");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Lỗi đọc file CV", e);
        }
    }

    @Override
    public List<ResumeResponse> listResumes() {
        return resumeRepository.findAll().stream().map(resumeMapper::toResponse).toList();
    }
}
