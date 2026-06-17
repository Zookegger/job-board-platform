import adminApi, { type PendingCompaniesParams } from "@/api/admin";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const ADMIN_COMPANY_KEYS = {
	pending: (params: PendingCompaniesParams) => ["admin", "companies", "pending", params] as const,
	pendingRoot: ["admin", "companies", "pending"] as const,
};

export function usePendingCompanies(params: PendingCompaniesParams) {
	return useQuery({
		queryKey: ADMIN_COMPANY_KEYS.pending(params),
		queryFn: () => adminApi.getPendingCompanies(params),
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
		},
	});
}
