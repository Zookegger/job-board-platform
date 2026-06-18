import adminApi from "@/api/admin";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const ADMIN_JOB_KEYS = {
	pending: (page: number, size: number) => ["admin", "jobs", "pending", page, size] as const,
	pendingRoot: ["admin", "jobs", "pending"] as const,
};

export function usePendingJobs(page: number, size: number) {
	return useQuery({
		queryKey: ADMIN_JOB_KEYS.pending(page, size),
		queryFn: () => adminApi.getPendingJobs(page, size),
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useApproveJob() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: (jobId: string) => adminApi.approveJob(jobId),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_JOB_KEYS.pendingRoot });
		},
	});
}

export function useRejectJob() {
	const queryClient = useQueryClient();

	return useMutation({
		mutationFn: ({ jobId, reason }: { jobId: string; reason: string }) => adminApi.rejectJob(jobId, reason),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: ADMIN_JOB_KEYS.pendingRoot });
		},
	});
}
