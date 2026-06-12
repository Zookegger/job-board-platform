export interface CandidateProfileRequest {
	fullName?: string;
	phone?: string;
	avatarUrl?: string;
}

export interface CandidateProfileResponse {
	id: string;
	email: string;
	role: string;
	fullName: string;
	phone: string;
	avatarUrl: string;
}

export interface EmployerProfileRequest {
	fullName?: string;
	phone?: string;
	avatarUrl?: string;
	roleInCompany?: string;
	companyName?: string;
	address?: string;
	description?: string;
	website?: string;
	logoUrl?: string;
}

export interface EmployerProfileResponse {
	id: string;
	email: string;
	role: string;
	fullName: string;
	phone: string;
	avatarUrl: string;
	companyId: string;
	companyName: string;
	roleInCompany: string;
}

export type UploadResumeRequest = {
	file: File;
	onUploadProgress?: (progress: number) => void;
};

export interface ResumeRequest {
	title: string;
}

export interface ResumeResponse {
	id: string;
	title: string;
	originalFileName: string;
	fileSize: number;
	fileType: string;
	createdAt: string;
	updatedAt: string;
}
