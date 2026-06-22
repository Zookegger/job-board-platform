import type { PaginationParams } from "@/types/pagination";
import adminApi from "@/api/admin";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export interface PendingCompaniesParams extends PaginationParams {
	keyword?: string;
	hasTaxCode?: boolean;
	hasContact?: boolean;
	sortBy?: "createdAt" | "companyName";
	direction?: "asc" | "desc";
}

export interface AllCompaniesParams extends PaginationParams {
	keyword?: string;
	status?: string;
}

export const ADMIN_COMPANY_KEYS = {
	pending: (params: PendingCompaniesParams) => ["admin", "companies", "pending", params] as const,
	pendingRoot: ["admin", "companies", "pending"] as const,
	all: (params: AllCompaniesParams) => ["admin", "companies", "all", params] as const,
	allRoot: ["admin", "companies", "all"] as const,
};

export function usePendingCompanies(params: PendingCompaniesParams) {
	return useQuery({
		queryKey: ADMIN_COMPANY_KEYS.pending(params),
		queryFn: () =>
			adminApi.getPendingCompanies(
				{ page: params.page, size: params.size },
				params.keyword,
				params.hasTaxCode,
				params.hasContact,
				params.sortBy,
				params.direction,
			),
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useAllCompanies(params: AllCompaniesParams) {
	return useQuery({
		queryKey: ADMIN_COMPANY_KEYS.all(params),
		queryFn: () => adminApi.getAllCompanies({ page: params.page, size: params.size }, params.keyword, params.status),
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useApproveCompany() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (companyId: string) => adminApi.approveCompany(companyId),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.pendingRoot });
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.allRoot });
		},
	});
}

export function useRejectCompany() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: ({ companyId, reason }: { companyId: string; reason: string }) =>
			adminApi.rejectCompany(companyId, reason),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.pendingRoot });
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.allRoot });
		},
	});
}

export function useSuspendCompany() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: ({ companyId, reason }: { companyId: string; reason: string }) =>
			adminApi.suspendCompany(companyId, reason),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.pendingRoot });
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.allRoot });
		},
	});
}

export function useUnsuspendCompany() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (companyId: string) => adminApi.unsuspendCompany(companyId),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.pendingRoot });
			queryClient.invalidateQueries({ queryKey: ADMIN_COMPANY_KEYS.allRoot });
		},
	});
}
