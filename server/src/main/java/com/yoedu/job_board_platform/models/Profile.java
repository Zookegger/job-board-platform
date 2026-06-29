package com.yoedu.job_board_platform.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Hồ sơ người dùng, liên kết 1-1 với User.
 * Chứa thông tin cá nhân như họ tên, số điện thoại, avatar.
 * Có thể mở rộng thành CandidateDetail (ứng viên) hoặc CompanyEmployerDetail
 * (nhà tuyển dụng).
 */
@Entity
@Table(name = "profiles")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Profile {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id;

    @MapsId // Share the same primary key with User
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Pattern(regexp = "^(|(\\+84|84|0)([35789])[0-9]{8,9})$", message = "Số điện thoại không hợp lệ")
    @Column(nullable = false, length = 15)
    private String phone;

    @Column(name = "avatar_url", columnDefinition = "text")
    private String avatarUrl;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL)
    private CandidateDetail candidateDetail;

    @OneToOne(mappedBy = "profile", cascade = CascadeType.ALL)
    private CompanyEmployerDetail employerDetail;

    @Column(name = "updated_at", nullable = false)
    @LastModifiedDate
    private OffsetDateTime updatedAt;
}
