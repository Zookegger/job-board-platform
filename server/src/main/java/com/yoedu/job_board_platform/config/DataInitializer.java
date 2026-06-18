package com.yoedu.job_board_platform.config;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.yoedu.job_board_platform.models.Application;
import com.yoedu.job_board_platform.models.ApplicationStatus;
import com.yoedu.job_board_platform.models.ApplicationStatusLog;
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
import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.models.ProficientLevel;
import com.yoedu.job_board_platform.models.Profile;
import com.yoedu.job_board_platform.models.Report;
import com.yoedu.job_board_platform.models.ReportReason;
import com.yoedu.job_board_platform.models.Resume;
import com.yoedu.job_board_platform.models.Skill;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.models.UserRole;
import com.yoedu.job_board_platform.repositories.ApplicationRepository;
import com.yoedu.job_board_platform.repositories.ApplicationStatusLogRepository;
import com.yoedu.job_board_platform.repositories.CandidateDetailRepository;
import com.yoedu.job_board_platform.repositories.CandidateSkillRepository;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.repositories.CompanyRepository;
import com.yoedu.job_board_platform.repositories.JobCategoryRepository;
import com.yoedu.job_board_platform.repositories.JobRepository;
import com.yoedu.job_board_platform.repositories.JobSkillRepository;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.ReportRepository;
import com.yoedu.job_board_platform.repositories.ResumeRepository;
import com.yoedu.job_board_platform.repositories.SkillRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
/**
 * Khởi tạo dữ liệu mẫu (seed) cho database khi chạy lần đầu.
 * Tạo tài khoản admin, nhà tuyển dụng (kèm công ty và tin tuyển dụng),
 * ứng viên (kèm kỹ năng, đơn ứng tuyển, CV), thông báo và báo cáo.
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
    private final ApplicationRepository applicationRepository;
    private final ApplicationStatusLogRepository applicationStatusLogRepository;
    private final NotificationRepository notificationRepository;
    private final ReportRepository reportRepository;
    private final ResumeRepository resumeRepository;
    private final PasswordEncoder passwordEncoder;

    private record EmployerSeedData(Profile employerProfile, Company company, List<Job> jobs) {}
    private record CandidateSeedData(User user, CandidateDetail detail) {}

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
        User admin = seedAdmin();
        EmployerSeedData employerData = seedEmployerWithCompanyAndJobs(categories, skills);
        seedPendingEmployers(categories, skills);
        CandidateSeedData candidateData = seedCandidate(skills);
        seedRemainingModels(admin, employerData, candidateData);

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

    private User seedAdmin() {
        User admin = userRepository.save(User.builder()
            .email("admin@yoedu.com")
            .password(passwordEncoder.encode("admin123"))
            .role(UserRole.ADMIN)
            .isActive(true)
            .build());
        log.info("Seeded admin user: {} — {}", admin.getId(), admin.getEmail());
        return admin;
    }

    private EmployerSeedData seedEmployerWithCompanyAndJobs(List<JobCategory> categories, List<Skill> skills) {
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

        List<Job> jobs = List.of(seniorJavaJob, marketingInternJob, uiDesignerJob);

        log.info("Seeded employer: {} — {}", employer.getId(), employer.getEmail());
        log.info("Seeded company: {} — {}", company.getId(), company.getCompanyName());
        log.info("Seeded {} jobs", jobs.size());

        return new EmployerSeedData(employerProfile, company, jobs);
    }

    private void seedPendingEmployers(List<JobCategory> categories, List<Skill> skills) {
        // ── Pending Employer 1: DataCraft Solutions ──
        User employer1 = User.builder()
            .email("employer-pending1@yoedu.com")
            .password(passwordEncoder.encode("employer123"))
            .role(UserRole.EMPLOYER)
            .isActive(true)
            .build();

        Profile profile1 = Profile.builder()
            .user(employer1)
            .fullName("Minh Tran")
            .phone("0912345678")
            .build();
        employer1.setProfile(profile1);
        userRepository.save(employer1);

        Company company1 = companyRepository.save(Company.builder()
            .companyName("DataCraft Solutions")
            .slug("datacraft-solutions")
            .address("456 Le Loi, District 1, Ho Chi Minh City")
            .description("An emerging data analytics and AI consulting firm.")
            .website("https://datacraft.io")
            .email("hr@datacraft.io")
            .phone("02823456789")
            .status(CompanyStatus.PENDING)
            .build());

        companyEmployerDetailRepository.save(CompanyEmployerDetail.builder()
            .profile(profile1)
            .company(company1)
            .roleInCompany("CEO")
            .build());

        Job dataEngineerJob = jobRepository.save(Job.builder()
            .company(company1)
            .category(categories.get(0))
            .title("Data Engineer")
            .slug("data-engineer")
            .description("Build and maintain data pipelines for our analytics platform.")
            .requirements("- 3+ years of Python\n- SQL expertise\n- ETL pipeline experience")
            .benefits("- Competitive salary\n- Equity options\n- Flexible hours")
            .numberOfOpenings(2)
            .salaryMin(new BigDecimal("25000000"))
            .salaryMax(new BigDecimal("40000000"))
            .currency("VND")
            .location("Ho Chi Minh City")
            .locationTypes(LocationTypes.HYBRID)
            .employmentType(EmploymentType.FULL_TIME)
            .experienceLevel(ExperienceLevel.MID)
            .status(JobStatus.DRAFT)
            .build());

        jobSkillRepository.save(JobSkill.builder()
            .jobId(dataEngineerJob.getId())
            .skillId(skills.get(2).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(dataEngineerJob.getId())
            .skillId(skills.get(5).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(dataEngineerJob.getId())
            .skillId(skills.get(6).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(dataEngineerJob.getId())
            .skillId(skills.get(7).getId())
            .build());

        log.info("Seeded pending employer 1: {} — {} (company: {})",
            employer1.getId(), employer1.getEmail(), company1.getCompanyName());

        // ── Pending Employer 2: GreenLeaf Marketing ──
        User employer2 = User.builder()
            .email("employer-pending2@yoedu.com")
            .password(passwordEncoder.encode("employer123"))
            .role(UserRole.EMPLOYER)
            .isActive(true)
            .build();

        Profile profile2 = Profile.builder()
            .user(employer2)
            .fullName("Anh Nguyen")
            .phone("0923456789")
            .build();
        employer2.setProfile(profile2);
        userRepository.save(employer2);

        Company company2 = companyRepository.save(Company.builder()
            .companyName("GreenLeaf Marketing")
            .slug("greenleaf-marketing")
            .address("789 Vo Van Kiet, District 5, Ho Chi Minh City")
            .description("A boutique marketing agency specializing in digital campaigns and brand strategy.")
            .website("https://greenleafmarketing.vn")
            .email("contact@greenleafmarketing.vn")
            .phone("02834567890")
            .status(CompanyStatus.PENDING)
            .build());

        companyEmployerDetailRepository.save(CompanyEmployerDetail.builder()
            .profile(profile2)
            .company(company2)
            .roleInCompany("Marketing Director")
            .build());

        Job contentCreatorJob = jobRepository.save(Job.builder()
            .company(company2)
            .category(categories.get(1))
            .title("Content Creator")
            .slug("content-creator")
            .description("Create engaging content for social media and digital campaigns.")
            .requirements("- 1+ year of content creation\n- Photography skills\n- Basic video editing")
            .benefits("- Creative environment\n- Equipment provided\n- Performance bonus")
            .numberOfOpenings(1)
            .salaryMin(new BigDecimal("12000000"))
            .salaryMax(new BigDecimal("20000000"))
            .currency("VND")
            .location("Ho Chi Minh City")
            .locationTypes(LocationTypes.ONSITE)
            .employmentType(EmploymentType.FULL_TIME)
            .experienceLevel(ExperienceLevel.JUNIOR)
            .status(JobStatus.DRAFT)
            .build());

        jobSkillRepository.save(JobSkill.builder()
            .jobId(contentCreatorJob.getId())
            .skillId(skills.get(2).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(contentCreatorJob.getId())
            .skillId(skills.get(9).getId())
            .build());

        log.info("Seeded pending employer 2: {} — {} (company: {})",
            employer2.getId(), employer2.getEmail(), company2.getCompanyName());

        // ── Pending Employer 3: BlueWave Finance ──
        User employer3 = User.builder()
            .email("employer-pending3@yoedu.com")
            .password(passwordEncoder.encode("employer123"))
            .role(UserRole.EMPLOYER)
            .isActive(true)
            .build();

        Profile profile3 = Profile.builder()
            .user(employer3)
            .fullName("Lan Hoang")
            .phone("0934567890")
            .build();
        employer3.setProfile(profile3);
        userRepository.save(employer3);

        Company company3 = companyRepository.save(Company.builder()
            .companyName("BlueWave Finance")
            .slug("bluewave-finance")
            .address("12 Ham Nghi, District 1, Ho Chi Minh City")
            .description("A fintech startup building next-generation payment solutions for Southeast Asia.")
            .website("https://bluewavefin.com")
            .email("talent@bluewavefin.com")
            .phone("02845678901")
            .status(CompanyStatus.PENDING)
            .build());

        companyEmployerDetailRepository.save(CompanyEmployerDetail.builder()
            .profile(profile3)
            .company(company3)
            .roleInCompany("CTO")
            .build());

        Job backendDevJob = jobRepository.save(Job.builder()
            .company(company3)
            .category(categories.get(0))
            .title("Backend Developer (Node.js)")
            .slug("backend-developer-nodejs")
            .description("Develop and maintain microservices for our payment platform.")
            .requirements("- 2+ years Node.js/TypeScript\n- REST API design\n- MongoDB experience")
            .benefits("- Competitive salary\n- Stock options\n- Remote-friendly")
            .numberOfOpenings(3)
            .salaryMin(new BigDecimal("20000000"))
            .salaryMax(new BigDecimal("35000000"))
            .currency("VND")
            .location("Ho Chi Minh City")
            .locationTypes(LocationTypes.REMOTE)
            .employmentType(EmploymentType.FULL_TIME)
            .experienceLevel(ExperienceLevel.MID)
            .status(JobStatus.DRAFT)
            .build());

        jobSkillRepository.save(JobSkill.builder()
            .jobId(backendDevJob.getId())
            .skillId(skills.get(8).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(backendDevJob.getId())
            .skillId(skills.get(3).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(backendDevJob.getId())
            .skillId(skills.get(5).getId())
            .build());
        jobSkillRepository.save(JobSkill.builder()
            .jobId(backendDevJob.getId())
            .skillId(skills.get(6).getId())
            .build());

        log.info("Seeded pending employer 3: {} — {} (company: {})",
            employer3.getId(), employer3.getEmail(), company3.getCompanyName());
    }

    private CandidateSeedData seedCandidate(List<Skill> skills) {
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

        return new CandidateSeedData(candidate, candidateDetail);
    }

    private void seedRemainingModels(User admin, EmployerSeedData employerData, CandidateSeedData candidateData) {
        Profile employerProfile = employerData.employerProfile();
        Company company = employerData.company();
        List<Job> jobs = employerData.jobs();

        User candidate = candidateData.user();
        CandidateDetail candidateDetail = candidateData.detail();
        Profile candidateProfile = candidate.getProfile();

        Job seniorJavaJob = jobs.get(0);
        Job marketingInternJob = jobs.get(1);
        Job uiDesignerJob = jobs.get(2);

        // ── Applications ──
        Application app1 = applicationRepository.save(Application.builder()
            .candidate(candidateProfile)
            .job(seniorJavaJob)
            .status(ApplicationStatus.PENDING)
            .coverLetter("I have 5+ years of Java experience and I'm very interested in this position.")
            .appliedAt(OffsetDateTime.now().minusDays(3))
            .build());

        Application app2 = applicationRepository.save(Application.builder()
            .candidate(candidateProfile)
            .job(marketingInternJob)
            .status(ApplicationStatus.REVIEWING)
            .coverLetter("I am currently studying Marketing and looking for hands-on experience.")
            .appliedAt(OffsetDateTime.now().minusDays(2))
            .build());

        Application app3 = applicationRepository.save(Application.builder()
            .candidate(candidateProfile)
            .job(uiDesignerJob)
            .status(ApplicationStatus.INTERVIEW)
            .coverLetter("I have a strong portfolio in UI/UX design with 2+ years of experience.")
            .appliedAt(OffsetDateTime.now().minusDays(1))
            .build());

        // ── Application Status Logs ──
        applicationStatusLogRepository.save(ApplicationStatusLog.builder()
            .application(app1)
            .status(ApplicationStatus.PENDING)
            .changedBy(candidate)
            .note("Application submitted")
            .build());

        applicationStatusLogRepository.save(ApplicationStatusLog.builder()
            .application(app2)
            .status(ApplicationStatus.PENDING)
            .changedBy(candidate)
            .note("Application submitted")
            .build());
        applicationStatusLogRepository.save(ApplicationStatusLog.builder()
            .application(app2)
            .status(ApplicationStatus.REVIEWING)
            .changedBy(employerProfile.getUser())
            .note("Application reviewed by employer")
            .build());

        applicationStatusLogRepository.save(ApplicationStatusLog.builder()
            .application(app3)
            .status(ApplicationStatus.PENDING)
            .changedBy(candidate)
            .note("Application submitted")
            .build());
        applicationStatusLogRepository.save(ApplicationStatusLog.builder()
            .application(app3)
            .status(ApplicationStatus.REVIEWING)
            .changedBy(employerProfile.getUser())
            .note("Application reviewed by employer")
            .build());
        applicationStatusLogRepository.save(ApplicationStatusLog.builder()
            .application(app3)
            .status(ApplicationStatus.INTERVIEW)
            .changedBy(employerProfile.getUser())
            .note("Candidate invited for interview")
            .build());

        // ── Notifications ──
        notificationRepository.save(Notification.builder()
            .user(employerProfile.getUser())
            .type(NotificationStatus.APPLICATION_RECEIVED)
            .entityId(app1.getId())
            .message("New application received for Senior Java Developer")
            .build());

        notificationRepository.save(Notification.builder()
            .user(candidate)
            .type(NotificationStatus.APPLICATION_STATUS_CHANGED)
            .entityId(app2.getId())
            .message("Your application for Marketing Intern is now being reviewed")
            .build());

        notificationRepository.save(Notification.builder()
            .user(candidate)
            .type(NotificationStatus.APPLICATION_STATUS_CHANGED)
            .entityId(app3.getId())
            .message("Your application for UI/UX Designer has advanced to the interview stage")
            .build());

        notificationRepository.save(Notification.builder()
            .user(admin)
            .type(NotificationStatus.JOB_PENDING_REVIEW)
            .entityId(seniorJavaJob.getId())
            .message("New job Senior Java Developer is pending review")
            .build());

        notificationRepository.save(Notification.builder()
            .user(employerProfile.getUser())
            .type(NotificationStatus.COMPANY_STATUS_CHANGED)
            .entityId(company.getId())
            .message("Your company " + company.getCompanyName() + " has been approved and is now active")
            .build());

        // ── Report ──
        reportRepository.save(Report.builder()
            .jobId(marketingInternJob.getId())
            .reason(ReportReason.SPAM)
            .details("This job posting appears to be a duplicate of another listing.")
            .reportedBy(candidate.getId())
            .build());

        // ── Resume ──
        resumeRepository.save(Resume.builder()
            .id(UUID.randomUUID())
            .candidateDetail(candidateDetail)
            .title("Job Seeker - Full Stack Developer")
            .originalFileName("job_seeker_cv.pdf")
            .filePath("/uploads/resumes/job_seeker_cv.pdf")
            .fileSize(245760)
            .fileType("application/pdf")
            .build());

        log.info("Seeded {} applications, {} status logs, {} notifications, {} report, {} resume",
            3, 6, 5, 1, 1);
    }
}
