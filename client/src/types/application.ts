export type ApplicationStatus = "PENDING" | "REVIEWING" | "INTERVIEW" | "HIRED" | "REJECTED";

export interface ApplicationListResponse {
	id: string;
	jobId: string;
	jobSlug: string;
	jobTitle: string;
	companyName: string;
	companyLogoUrl: string | null;
	jobLocation: string | null;
	status: ApplicationStatus;
	appliedAt: string;
}

export const APPLICATION_STATUS_LABELS: Record<ApplicationStatus, string> = {
	PENDING: "Chờ xử lý",
	REVIEWING: "Đang xem xét",
	INTERVIEW: "Phỏng vấn",
	HIRED: "Đã tuyển",
	REJECTED: "Từ chối",
};

export interface ApplicationRequest {
	jobId: string;
	coverLetter?: string;
}

export interface ApplicationResponse {
	id: string;
	jobId: string;
	jobSlug: string;
	jobTitle: string;
	companyName: string;
	status: ApplicationStatus;
	coverLetter: string | null;
	resumeUrl: string | null;
	appliedAt: string;
}
