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

export type AdminUserRole = "ADMIN" | "EMPLOYER" | "CANDIDATE";

export type AdminUserStatus = "ACTIVE" | "INACTIVE";

export interface AdminUserListResponse {
	id: string;
	email: string;
	fullName: string | null;
	role: AdminUserRole;
	status: AdminUserStatus;
	isActive: boolean;
	createdAt: string;
}

export interface AdminUsersQueryParams {
	page?: number;
	size?: number;
	role?: string;
	status?: string;
	sortBy?: string;
	direction?: "asc" | "desc";
}