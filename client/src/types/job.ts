export type JobStatus = "DRAFT" | "PENDING_APPROVAL" | "ACTIVE" | "EXPIRED" | "REJECTED";
export type LocationTypes = "ONSITE" | "REMOTE" | "HYBRID";
export type EmploymentType = "FULL_TIME" | "PART_TIME" | "CONTRACT" | "INTERNSHIP";
export type ExperienceLevel = "INTERN" | "JUNIOR" | "MID" | "SENIOR" | "LEAD";

export interface AdminPendingJobResponse {
	id: string;
	title: string;
	status: JobStatus;
	description: string | null;
	requirements: string | null;
	benefits: string | null;
	location: string | null;
	locationTypes: LocationTypes;
	employmentType: EmploymentType;
	experienceLevel: ExperienceLevel;
	salaryMin: number | null;
	salaryMax: number | null;
	currency: string;
	numberOfOpenings: number;
	companyName: string | null;
	companyLogoUrl: string | null;
	categoryName: string | null;
	createdAt: string;
}
// ── Append to existing file ──

export const JOB_STATUS_LABELS: Record<JobStatus, string> = {
	DRAFT: "Bản nháp",
	PENDING_APPROVAL: "Chờ duyệt",
	ACTIVE: "Đã đăng",
	EXPIRED: "Hết hạn",
	REJECTED: "Bị từ chối",
};

export const EMPLOYMENT_TYPE_LABELS: Record<EmploymentType, string> = {
	FULL_TIME: "Toàn thời gian",
	PART_TIME: "Bán thời gian",
	CONTRACT: "Hợp đồng",
	INTERNSHIP: "Thực tập",
};

export const LOCATION_TYPES_LABELS: Record<LocationTypes, string> = {
	ONSITE: "Tại văn phòng",
	REMOTE: "Remote",
	HYBRID: "Hybrid",
};

export const EXPERIENCE_LEVEL_LABELS: Record<ExperienceLevel, string> = {
	INTERN: "Thực tập sinh",
	JUNIOR: "Junior",
	MID: "Mid-level",
	SENIOR: "Senior",
	LEAD: "Lead / Manager",
};

export interface JobRequest {
	title: string;
	description: string;
	requirements?: string | null;
	benefits?: string | null;
	categoryId: number;
	numberOfOpenings?: number | null;
	salaryMin?: number | null;
	salaryMax?: number | null;
	currency?: string | null;
	location?: string | null;
	locationTypes: LocationTypes;
	employmentType: EmploymentType;
	experienceLevel: ExperienceLevel;
	skillIds?: number[] | null;
}

export interface JobListResponse {
	id: string;
	slug: string;
	title: string;
	status: JobStatus;
	locationTypes: LocationTypes;
	employmentType: EmploymentType;
	experienceLevel: ExperienceLevel;
	salaryMin: number | null;
	salaryMax: number | null;
	currency: string | null;
	numberOfOpenings: number | null;
	companyName: string;
	createdAt: string;
}

export interface SkillResponse {
	id: number;
	name: string;
	isActive: boolean;
}

export interface JobResponse extends JobListResponse {
	slug: string;
	description: string;
	requirements?: string | null;
	benefits?: string | null;
	location?: string | null;
	postedDate?: string | null;
	expirationDate?: string | null;
	updatedAt: string;
	companyId: string;
	categoryId: number;
	categoryName: string;
	skills: SkillResponse[];
}

export interface CategoryResponse {
	id: number;
	name: string;
}

export interface EmployerJobParams {
	page?: number;
	size?: number;
	status?: JobStatus;
	keyword?: string;
}
