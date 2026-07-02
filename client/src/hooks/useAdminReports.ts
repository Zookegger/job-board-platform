import adminApi from "@/api/admin";
import type { ReportsParams } from "@/types/admin";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";


export const ADMIN_REPORT_KEYS = {
	all: (params: ReportsParams) => ["admin", "reports", params] as const,
	root: ["admin", "reports"] as const,
};

export function useReports(params: ReportsParams) {
	return useQuery({
		queryKey: ADMIN_REPORT_KEYS.all(params),
		queryFn: () => adminApi.getReports({ page: params.page, size: params.size, status: params.status, reason: params.reason }),
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useReviewReport() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: ({ id, reviewNotes }: { id: string; reviewNotes?: string }) =>
			adminApi.reviewReport(id, { reviewNotes }),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_REPORT_KEYS.root });
		},
	});
}

export function useDismissReport() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: ({ id, reviewNotes }: { id: string; reviewNotes?: string }) =>
			adminApi.dismissReport(id, { reviewNotes }),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_REPORT_KEYS.root });
		},
	});
}

export function useResolveReport() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: ({ id, reviewNotes }: { id: string; reviewNotes?: string }) =>
			adminApi.resolveReport(id, { reviewNotes }),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_REPORT_KEYS.root });
		},
	});
}
