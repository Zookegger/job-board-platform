export type ApplicationStatus = "PENDING" | "REVIEWING" | "INTERVIEW" | "HIRED" | "REJECTED" | "WITHDRAWN";

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
	WITHDRAWN: "Đã rút đơn",
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
	companyLogoUrl: string | null;
	jobLocation: string | null;
	status: ApplicationStatus;
	coverLetter: string | null;
	resumeUrl: string | null;
	appliedAt: string;
}

export interface ApplicationTimelineResponse {
	id: string;
	status: ApplicationStatus;
	statusLabel: string;
	changedByName: string;
	note: string | null;
	changedAt: string;
}

export interface EmployerApplicationListResponse {
	id: string;
	candidateId: string;
	candidateName: string;
	candidateAvatarUrl: string | null;
	candidateEmail: string;
	jobId: string;
	jobTitle: string;
	status: ApplicationStatus;
	coverLetter: string | null;
	resumeUrl: string | null;
	appliedAt: string;
}

export interface EmployerApplicationParams {
	jobId?: string;
	status?: string;
	page?: number;
	size?: number;
}
