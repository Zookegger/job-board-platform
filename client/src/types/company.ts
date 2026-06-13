export interface CompanyRequest {
	companyName?: string;
	address?: string;
	description?: string;
	website?: string;
	logoUrl?: string;
	email?: string;
	phone?: string;
	taxCode?: string;
}

export interface CompanyResponse {
	id: string;
	companyName: string;
	slug: string;
	address: string;
	description: string | null;
	website: string | null;
	logoUrl: string | null;
	email: string | null;
	phone: string | null;
	taxCode: string | null;
	status: CompanyStatus;
	isApproved: boolean;
	createdAt: string;
	approvedAt: string | null;
}

export interface AdminPendingCompanyResponse extends CompanyResponse {
	employerName: string | null;
	employerEmail: string | null;
	employerPhone: string | null;
	roleInCompany: string | null;
}

export const CompanyStatus = {
	PENDING: "PENDING",
	APPROVED: "APPROVED",
	REJECTED: "REJECTED",
	SUSPENDED: "SUSPENDED",
} as const;

export type CompanyStatus = (typeof CompanyStatus)[keyof typeof CompanyStatus];
