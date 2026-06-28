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