package com.yoedu.job_board_platform.config;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.models.CandidateDetail;
import com.yoedu.job_board_platform.models.CandidateSkill;
import com.yoedu.job_board_platform.models.CandidateSkillId;
import com.yoedu.job_board_platform.models.Company;
import com.yoedu.job_board_platform.models.CompanyEmployerDetail;
import com.yoedu.job_board_platform.models.CompanyStatus;
import com.yoedu.job_board_platform.models.EmploymentType;
import com.yoedu.job_board_platform.models.ExperienceLevel;
import com.yoedu.job_board_platform.models.Job;
import com.yoedu.job_board_platform.models.JobCategory;
import com.yoedu.job_board_platform.models.JobSkill;
import com.yoedu.job_board_platform.models.JobStatus;
import com.yoedu.job_board_platform.models.LocationTypes;
import com.yoedu.job_board_platform.models.ProficientLevel;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.CandidateDetailRepository;
import com.yoedu.job_board_platform.repositories.CandidateSkillRepository;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@RequiredArgsConstructor
@Slf4j
/**
 * Khởi tạo dữ liệu mẫu (seed) cho database khi chạy lần đầu.
 * Tạo tài khoản admin, nhà tuyển dụng (kèm công ty và tin tuyển dụng),
 * và ứng viên (kèm kỹ năng).
 */
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CandidateDetailRepository candidateDetailRepository;
    private final CompanyEmployerDetailRepository companyEmployerDetailRepository;
    private final CompanyRepository companyRepository;
    private final JobRepository jobRepository;
    private final JobCategoryRepository jobCategoryRepository;
    private final SkillRepository skillRepository;
    private final CandidateSkillRepository candidateSkillRepository;
    private final JobSkillRepository jobSkillRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Thực thi seed dữ liệu khi ứng dụng khởi động.
     * Bỏ qua nếu database đã có dữ liệu.
     */
    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains data — skipping seed");
            return;
        }

        log.info("Seeding database...");

        List<JobCategory> categories = seedJobCategories();
        List<Skill> skills = seedSkills();
        seedAdmin();
        seedEmployerWithCompanyAndJobs(categories, skills);
        seedCandidate(skills);

        log.info("Database seeding complete");
    }

    private List<JobCategory> seedJobCategories() {
        List<JobCategory> categories = List.of(
            JobCategory.builder().name("Software Engineering").build(),
            JobCategory.builder().name("Marketing").build(),
            JobCategory.builder().name("Design").build(),
            JobCategory.builder().name("Finance").build(),
            JobCategory.builder().name("Healthcare").build()
        );
        return jobCategoryRepository.saveAll(categories);
    }

    private List<Skill> seedSkills() {
        List<Skill> skills = List.of(
            Skill.builder().name("Java").isActive(true).build(),
            Skill.builder().name("React").isActive(true).build(),
            Skill.builder().name("Python").isActive(true).build(),
            Skill.builder().name("TypeScript").isActive(true).build(),
            Skill.builder().name("Spring Boot").isActive(true).build(),
            Skill.builder().name("PostgreSQL").isActive(true).build(),
            Skill.builder().name("Docker").isActive(true).build(),
            Skill.builder().name("AWS").isActive(true).build(),
            Skill.builder().name("Node.js").isActive(true).build(),
            Skill.builder().name("Figma").isActive(true).build()
        );
        return skillRepository.saveAll(skills);
    }

    private void seedAdmin() {
        User admin = userRepository.save(User.builder()
            .email("admin@yoedu.com")
            .password(passwordEncoder.encode("admin123"))
            .role(UserRole.ADMIN)
            .isActive(true)
            .build());
        log.info("Seeded admin user: {} — {}", admin.getId(), admin.getEmail());
    }

    private void seedEmployerWithCompanyAndJobs(List<JobCategory> categories, List<Skill> skills) {
        User employer = User.builder()
            .email("employer@yoedu.com")
            .password(passwordEncoder.encode("employer123"))
            .role(UserRole.EMPLOYER)
            .isActive(true)
            .build();

        Profile employerProfile = Profile.builder()
            .user(employer)
            .fullName("HR Manager")
            .phone("0901234567")
            .build();

        employer.setProfile(employerProfile);
        userRepository.save(employer);

        Company company = companyRepository.save(Company.builder()
            .companyName("Yoedu Tech")
            .slug("yoedu-tech")
            .address("123 Nguyen Hue, District 1, Ho Chi Minh City")
            .description("A leading technology company specializing in software development and IT solutions.")
            .website("https://yoedu.com")
            .email("hr@yoedu.com")
            .phone("02812345678")
            .status(CompanyStatus.APPROVED)
            .isApproved(true)
            .approvedAt(OffsetDateTime.now())
            .build());

        companyEmployerDetailRepository.save(CompanyEmployerDetail.builder()
            .profile(employerProfile)
            .company(company)
            .roleInCompany("HR Manager")
            .build());

        JobCategory softwareCategory = categories.get(0);
        JobCategory marketingCategory = categories.get(1);
        JobCategory designCategory = categories.get(2);

        Job seniorJavaJob = jobRepository.save(Job.builder()
            .company(company)
            .category(softwareCategory)
            .title("Senior Java Developer")
            .slug("senior-java-developer")
            .description("We are looking for an experienced Java developer to join our core engineering team.")
            .requirements("- 5+ years of Java experience\n- Spring Boot expertise\n- Strong SQL skills")
            .benefits("- Competitive salary\n- Remote work option\n- Health insurance")
            .numberOfOpenings(2)
            .salaryMin(new BigDecimal("30000000"))
            .salaryMax(new BigDecimal("50000000"))
            .currency("VND")
            .location("Ho Chi Minh City")
            .locationTypes(LocationTypes.HYBRID)
            .employmentType(EmploymentType.FULL_TIME)
            .experienceLevel(ExperienceLevel.SENIOR)
            .status(JobStatus.ACTIVE)
            .postedDate(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plusDays(30))
            .build());

        jobSkillRepository.save(JobSkill.builder()
            .jobId(seniorJavaJob.getId())
            .skillId(skills.get(0).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(seniorJavaJob.getId())
            .skillId(skills.get(4).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(seniorJavaJob.getId())
            .skillId(skills.get(5).getId())
            .build());

        Job marketingInternJob = jobRepository.save(Job.builder()
            .company(company)
            .category(marketingCategory)
            .title("Marketing Intern")
            .slug("marketing-intern")
            .description("Join our marketing team to help with content creation and social media management.")
            .requirements("- Currently pursuing a degree in Marketing or related field")
            .benefits("- Hands-on experience\n- Mentorship\n- Lunch allowance")
            .numberOfOpenings(1)
            .salaryMin(new BigDecimal("5000000"))
            .salaryMax(new BigDecimal("8000000"))
            .currency("VND")
            .location("Ho Chi Minh City")
            .locationTypes(LocationTypes.ONSITE)
            .employmentType(EmploymentType.CONTRACT)
            .experienceLevel(ExperienceLevel.INTERN)
            .status(JobStatus.ACTIVE)
            .postedDate(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plusDays(30))
            .build());

        Job uiDesignerJob = jobRepository.save(Job.builder()
            .company(company)
            .category(designCategory)
            .title("UI/UX Designer")
            .slug("ui-ux-designer")
            .description("Design beautiful and intuitive interfaces for our web and mobile applications.")
            .requirements("- 2+ years of UI/UX design experience\n- Proficiency in Figma\n- Portfolio required")
            .benefits("- Creative work environment\n- Latest MacBook Pro\n- Flexible hours")
            .numberOfOpenings(1)
            .salaryMin(new BigDecimal("15000000"))
            .salaryMax(new BigDecimal("25000000"))
            .currency("VND")
            .location("Ho Chi Minh City")
            .locationTypes(LocationTypes.REMOTE)
            .employmentType(EmploymentType.FULL_TIME)
            .experienceLevel(ExperienceLevel.MID)
            .status(JobStatus.ACTIVE)
            .postedDate(OffsetDateTime.now())
            .expirationDate(OffsetDateTime.now().plusDays(30))
            .build());

        jobSkillRepository.save(JobSkill.builder()
            .jobId(marketingInternJob.getId())
            .skillId(skills.get(2).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(marketingInternJob.getId())
            .skillId(skills.get(9).getId())
            .build());

        jobSkillRepository.save(JobSkill.builder()
            .jobId(uiDesignerJob.getId())
            .skillId(skills.get(9).getId())
            .build());

        log.info("Seeded employer: {} — {}", employer.getId(), employer.getEmail());
        log.info("Seeded company: {} — {}", company.getId(), company.getCompanyName());
        log.info("Seeded {} jobs", 3);
    }

    private void seedCandidate(List<Skill> skills) {
        User candidate = User.builder()
            .email("candidate@yoedu.com")
            .password(passwordEncoder.encode("candidate123"))
            .role(UserRole.CANDIDATE)
            .isActive(true)
            .build();

        Profile candidateProfile = Profile.builder()
            .user(candidate)
            .fullName("Job Seeker")
            .phone("0909876543")
            .build();
        candidate.setProfile(candidateProfile);
        candidate = userRepository.save(candidate);

        CandidateDetail candidateDetail = CandidateDetail.builder()
            .profileId(candidate.getId())
            .build();
        candidateDetailRepository.saveAndFlush(candidateDetail);

        UUID candidateUuid = candidate.getId();

        CandidateSkillId javaSkillId = new CandidateSkillId();
        javaSkillId.setCandidateId(candidateUuid);
        javaSkillId.setSkillId(skills.get(0).getId());
        candidateSkillRepository.save(CandidateSkill.builder()
            .id(javaSkillId)
            .proficientLevel(ProficientLevel.ADVANCED)
            .build());

        CandidateSkillId reactSkillId = new CandidateSkillId();
        reactSkillId.setCandidateId(candidateUuid);
        reactSkillId.setSkillId(skills.get(1).getId());
        candidateSkillRepository.save(CandidateSkill.builder()
            .id(reactSkillId)
            .proficientLevel(ProficientLevel.INTERMEDIATE)
            .build());

        CandidateSkillId tsSkillId = new CandidateSkillId();
        tsSkillId.setCandidateId(candidateUuid);
        tsSkillId.setSkillId(skills.get(3).getId());
        candidateSkillRepository.save(CandidateSkill.builder()
            .id(tsSkillId)
            .proficientLevel(ProficientLevel.ADVANCED)
            .build());

        log.info("Seeded candidate: {} — {}", candidateUuid, candidate.getEmail());
    }
}
