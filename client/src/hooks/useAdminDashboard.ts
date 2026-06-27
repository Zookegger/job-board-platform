import adminApi from "@/api/admin";
import { useQuery } from "@tanstack/react-query";

export const ADMIN_DASHBOARD_KEYS = {
	stats: ["admin", "dashboard", "stats"] as const,
	applicationsChart: (days: number) =>
		["admin", "statistics", "applications-chart", days] as const,
};

export function useAdminDashboardStats() {
	return useQuery({
		queryKey: ADMIN_DASHBOARD_KEYS.stats,
		queryFn: adminApi.getDashboardStats,
		retry: false,
	});
}

export function useAdminApplicationChartStats(days: number) {
	return useQuery({
		queryKey: ADMIN_DASHBOARD_KEYS.applicationsChart(days),
		queryFn: () => adminApi.getApplicationChartStats(days),
		retry: false,
	});
}