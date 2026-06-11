package com.yoedu.job_board_platform.models;

import java.util.UUID;

import org.springframework.data.domain.Persistable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PostLoad;
import jakarta.persistence.PostPersist;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "company_employer_details")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
/**
 * Liên kết giữa Profile của nhà tuyển dụng và Company.
 * Mỗi nhà tuyển dụng chỉ thuộc về một công ty duy nhất.
 * Triển khai Persistable để kiểm soát INSERT/merge behavior
 * khi profileId được gán thủ công.
 */
public class CompanyEmployerDetail implements Persistable<UUID> {
    @Id
    @Column(name = "profile_id", columnDefinition = "uuid")
    private UUID profileId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "profile_id")
    private Profile profile;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "role_in_company", length = 50)
    private String roleInCompany;

    // Cờ này chỉ tồn tại trong bộ nhớ (không lưu vào DB)
    // Mặc định là true khi entity mới được tạo bằng new() hoặc Builder
    @Transient
    @Builder.Default
    private boolean isNewEntity = true;

    @Override
    public UUID getId() {
        return profileId;
    }

    // Spring Data gọi isNew() để quyết định:
    //   true  → entityManager.persist() → INSERT
    //   false → entityManager.merge()   → SELECT rồi INSERT/UPDATE
    // Vì profileId được gán thủ công (không phải @GeneratedValue),
    // nên nếu không có Persistable, Spring Data thấy ID != null
    // và gọi merge() → gây lỗi StaleObjectStateException.
    @Override
    @Transient
    public boolean isNew() {
        return isNewEntity;
    }

    // Sau khi INSERT thành công hoặc load từ DB,
    // đánh dấu entity là "cũ" để lần save sau dùng merge()
    @PostLoad
    @PostPersist
    void markNotNew() {
        this.isNewEntity = false;
    }
}
