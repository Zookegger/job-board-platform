import type { UserRole } from "./auth";
import type { PaginationParams } from "./pagination";

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
