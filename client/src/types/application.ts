export type ApplicationStatus = "PENDING" | "REVIEWING" | "INTERVIEW" | "HIRED" | "REJECTED";

export interface ApplicationRequest {
	jobId: string;
	coverLetter?: string;
}

export interface ApplicationResponse {
	id: string;
	jobId: string;
	jobTitle: string;
	companyName: string;
	status: ApplicationStatus;
	coverLetter: string | null;
	appliedAt: string;
}
