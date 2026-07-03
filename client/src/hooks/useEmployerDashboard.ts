import { employerDashboardApi } from "@/api/employerDashboard";
import { useQuery } from "@tanstack/react-query";

export const EMPLOYER_DASHBOARD_KEYS = {
	stats: ["employer", "dashboard", "stats"] as const,
};

export function useEmployerDashboardStats() {
	return useQuery({
		queryKey: EMPLOYER_DASHBOARD_KEYS.stats,
		queryFn: employerDashboardApi.getEmployerDashboardStats,
		retry: false,
	});
}
