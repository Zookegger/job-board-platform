import type { ApplicationListResponse } from "./application";
import type { UserRole } from "./auth";
import type { CompanyResponse } from "./company";
import type { PaginationParams } from "./pagination";
import type { ResumeResponse } from "./profile";
import type { ReportReason, ReportStatus } from "./report";
import type { CandidateSkillResponse } from "./skill";

export interface AdminDashboardStatsResponse {
	totalUsers: number;
	totalCompanies: number;
	totalJobs: number;
	totalApplications: number;
	newUsers: number;
	pendingJobs: number;
	pendingCompanies: number;
}
export interface AdminApplicationChartResponse {
	days: number;
	fromDate: string;
	toDate: string;
	totalApplications: number;
	dailyApplications: DailyApplicationPoint[];
	statusDistribution: StatusDistributionPoint[];
}

export interface DailyApplicationPoint {
	date: string;
	total: number;
}

export interface StatusDistributionPoint {
	status: string;
	total: number;
	percentage: number;
}

export interface AdminUserListResponse {
	id: string;
	avatarUrl: string | null;
	phone: string | null;
	email: string;
	fullName: string | null;
	role: UserRole;
	isActive: boolean;
	createdAt: string;
	updatedAt: string | null;
}

export interface AdminUsersQueryParams extends PaginationParams {
	keyword?: string | null;
	role?: UserRole | null;
	isActive?: boolean | null;
}

export interface ReportsParams extends PaginationParams {
	status?: ReportStatus;
	reason?: ReportReason;
}

export interface HiringActivityInfo {
	totalApplications: number;
	pendingApplications: number;
	reviewingApplications: number;
	interviewApplications: number;
	hiredApplications: number;
	rejectedApplications: number;
}

export interface UserFullJobPosting {
	id: string;
	title: string;
	slug: string;
	status: string;
	categoryName: string;
	location: string | null;
	createdAt: string;
}

export interface UserFullResponse {
	id: string;
	email: string;
	role: UserRole;
	isActive: boolean;
	fullName: string | null;
	phone: string | null;
	avatarUrl: string | null;
	createdAt: string;
	updatedAt: string | null;
	skills: CandidateSkillResponse[] | null;
	resume: ResumeResponse | null;
	applications: ApplicationListResponse[] | null;
	company: CompanyResponse | null;
	roleInCompany: string | null;
	jobPostings: UserFullJobPosting[] | null;
	hiringActivity: HiringActivityInfo | null;
}