export const ReportStatus = {
	PENDING: "PENDING",
	REVIEWED: "REVIEWED",
	DISMISSED: "DISMISSED",
	RESOLVED: "RESOLVED",
} as const;

export type ReportStatus = (typeof ReportStatus)[keyof typeof ReportStatus];

export const ReportReason = {
	SPAM: "SPAM",
	SCAM: "SCAM",
	INAPPROPRIATE: "INAPPROPRIATE",
	OTHER: "OTHER",
} as const;

export type ReportReason = (typeof ReportReason)[keyof typeof ReportReason];

export interface CreateReportRequest {
	jobId?: string;
	companyId?: string;
	reason: ReportReason;
	details?: string;
}

export interface ReportResponse {
	id: string;
	jobId?: string;
	jobTitle?: string;
	companyId?: string;
	companyName?: string;
	reportedById: string;
	reportedByName: string;
	reason: string;
	details?: string;
	reviewNotes?: string;
	status: ReportStatus;
	reviewedById?: string;
	reviewedByName?: string;
	reviewedAt?: string;
	createdAt: string;
}
