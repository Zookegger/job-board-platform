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
