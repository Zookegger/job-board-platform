import adminApi from "@/api/admin";
import type { AdminUsersQueryParams } from "@/types/admin";
import { keepPreviousData, useQuery } from "@tanstack/react-query";

export const ADMIN_USER_KEYS = {
	all: ["admin", "users"] as const,
	list: (params: AdminUsersQueryParams) => ["admin", "users", params] as const,
};

export function useAdminUsers(params: AdminUsersQueryParams) {
	return useQuery({
		queryKey: ADMIN_USER_KEYS.list(params),
		queryFn: () => adminApi.getUsers(params),
		placeholderData: keepPreviousData,
		retry: false,
	});
}
