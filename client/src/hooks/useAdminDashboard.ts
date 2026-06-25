import adminApi from "@/api/admin";
import { useQuery } from "@tanstack/react-query";

export const ADMIN_DASHBOARD_KEYS = {
	stats: ["admin", "dashboard", "stats"] as const,
};

export function useAdminDashboardStats() {
	return useQuery({
		queryKey: ADMIN_DASHBOARD_KEYS.stats,
		queryFn: adminApi.getDashboardStats,
		retry: false,
	});
}