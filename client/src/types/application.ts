export type ApplicationStatus = "PENDING" | "REVIEWING" | "INTERVIEW" | "HIRED" | "REJECTED";

export interface ApplicationListResponse {
	id: string;
	jobId: string;
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
