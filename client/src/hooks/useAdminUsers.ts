import adminApi from "@/api/admin";
import type { AdminUsersQueryParams } from "@/types/admin";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const ADMIN_USER_KEYS = {
	all: ["admin", "users"] as const,
	list: (params: AdminUsersQueryParams) => ["admin", "users", params] as const,
	detail: (userId: string) => ["admin", "users", userId] as const,
};

export function useAdminUsers(params: AdminUsersQueryParams) {
	return useQuery({
		queryKey: ADMIN_USER_KEYS.list(params),
		queryFn: () => adminApi.getUsers(params),
		placeholderData: keepPreviousData,
		retry: false,
		staleTime: 3 * 60 * 1000, // 3 minutes
	});
}

export function useSuspendUser() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (userId: string) => adminApi.suspendUser(userId),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ADMIN_USER_KEYS.all }),
	});
}

export function useReactivateUser() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (userId: string) => adminApi.reactivateUser(userId),
		onSuccess: () => queryClient.invalidateQueries({ queryKey: ADMIN_USER_KEYS.all }),
	});
}

export function useUserDetail(userId: string) {
	return useQuery({
		queryKey: ADMIN_USER_KEYS.detail(userId),
		queryFn: () => adminApi.getUserDetail(userId),
		retry: false,
	});
}
