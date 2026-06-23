import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import applicationApi from "@/api/application";
import type { ApplicationRequest } from "@/types/application";
import { UserRole } from "@/types/auth";
import { useAuth } from "./useAuth";

export function useSubmitApplication() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (request: ApplicationRequest) => applicationApi.submit(request),
		onSuccess: (_data, variables) => {
			// Invalidate check query so the "Đã ứng tuyển" state updates immediately
			queryClient.invalidateQueries({ queryKey: ["applications", "check", variables.jobId] });
		},
	});
}

export function useHasApplied(jobId: string | undefined) {
	const { user } = useAuth();
	return useQuery({
		queryKey: ["applications", "check", jobId],
		queryFn: () => applicationApi.checkApplied(jobId!),
		enabled: !!jobId && user?.role === UserRole.CANDIDATE,
	});
}

export function useWithdrawApplication() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: ({ id, jobId }: { id: string; jobId: string }) =>
			applicationApi.withdraw(id).then(() => jobId),
		onSuccess: (jobId) => {
			queryClient.invalidateQueries({ queryKey: ["applications", "check", jobId] });
		},
	});
}
