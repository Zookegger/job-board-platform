export const JobStatus = {
	DRAFT: "DRAFT",
	PENDING_APPROVAL: "PENDING_APPROVAL",
	ACTIVE: "ACTIVE",
	EXPIRED: "EXPIRED",
	REJECTED: "REJECTED",
} as const;

export type JobStatus = (typeof JobStatus)[keyof typeof JobStatus];

export const LocationTypes = {
	ONSITE: "ONSITE",
	REMOTE: "REMOTE",
	HYBRID: "HYBRID",
} as const;

export type LocationTypes = (typeof LocationTypes)[keyof typeof LocationTypes];

export const EmploymentTypes = {
	FULL_TIME: "FULL_TIME",
	PART_TIME: "PART_TIME",
	CONTRACT: "CONTRACT",
} as const;

export type EmploymentTypes = (typeof EmploymentTypes)[keyof typeof EmploymentTypes];

export const ExperienceLevels = {
	NOT_REQUIRED: "NOT_REQUIRED",
	INTERN: "INTERN",
	JUNIOR: "JUNIOR",
	MID: "MID",
	SENIOR: "SENIOR",
	LEAD: "LEAD",
} as const;

export type ExperienceLevels = (typeof ExperienceLevels)[keyof typeof ExperienceLevels];

export interface AdminPendingJobResponse {
	id: string;
	title: string;
	status: JobStatus;
	description: string | null;
	requirements: string | null;
	benefits: string | null;
	location: string | null;
	locationTypes: LocationTypes;
	employmentType: EmploymentTypes;
	experienceLevel: ExperienceLevels;
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

export const EMPLOYMENT_TYPE_LABELS: Record<EmploymentTypes, string> = {
	FULL_TIME: "Toàn thời gian",
	PART_TIME: "Bán thời gian",
	CONTRACT: "Hợp đồng",
};

export const LOCATION_TYPES_LABELS: Record<LocationTypes, string> = {
	ONSITE: "Tại văn phòng",
	REMOTE: "Remote",
	HYBRID: "Hybrid",
};

export const EXPERIENCE_LEVEL_LABELS: Record<ExperienceLevels, string> = {
	NOT_REQUIRED: "Không yêu cầu",
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
	employmentType: EmploymentTypes;
	experienceLevel: ExperienceLevels;
	skillIds?: number[] | null;
}

export interface JobListResponse {
	id: string;
	slug: string;
	title: string;
	status: JobStatus;
	locationTypes: LocationTypes;
	employmentType: EmploymentTypes;
	experienceLevel: ExperienceLevels;
	salaryMin: number | null;
	salaryMax: number | null;
	currency: string | null;
	numberOfOpenings: number | null;
	companyName: string;
	companyLogoUrl: string | null;
	postedDate: string | null;
	expirationDate: string | null;
	updatedAt: string;
	createdAt: string;
}

export interface SkillResponse {
	id: number;
	name: string;
	isActive: boolean;
}

export interface JobResponse extends JobListResponse {
	description: string;
	requirements?: string | null;
	benefits?: string | null;
	location?: string | null;
	companyId: string;
	companySlug: string;
	companyAddress?: string | null;
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
