import applicationApi from "@/api/application";
import applicationsApi, { type MyApplicationsParams } from "@/api/applications";
import type { ApplicationRequest } from "@/types/application";
import { UserRole } from "@/types/auth";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import { useAuth } from "./useAuth";

export function useSubmitApplication() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (request: ApplicationRequest) => applicationApi.submit(request),
		onSuccess: (_data, variables) => {
			queryClient.invalidateQueries({ queryKey: ["applications", "by-job", variables.jobId] });
		},
	});
}

export function useApplicationByJob(jobId: string | undefined) {
	const { user } = useAuth();
	return useQuery({
		queryKey: ["applications", "by-job", jobId],
		queryFn: () => applicationApi.getByJob(jobId!),
		enabled: !!jobId && user?.role === UserRole.CANDIDATE,
	});
}

export function useWithdrawApplication() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: ({ id, jobId }: { id: string; jobId: string }) =>
			applicationApi.withdraw(id).then(() => jobId),
		onSuccess: (jobId) => {
			queryClient.invalidateQueries({ queryKey: ["applications", "by-job", jobId] });
		},
	});
}

export const APPLICATION_KEYS = {
	my: (params: MyApplicationsParams) => ["applications", "my", params] as const,
	myRoot: ["applications", "my"] as const,
};

export function useMyApplications(params: MyApplicationsParams) {
	return useQuery({
		queryKey: APPLICATION_KEYS.my(params),
		queryFn: () => applicationsApi.getMyApplications(params),
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useApplicationTimeline(id: string | undefined) {
	return useQuery({
		queryKey: ["applications", "timeline", id],
		queryFn: () => applicationApi.getTimeline(id!),
		enabled: !!id,
	});
}

export function useApplicationDetail(id: string | undefined) {
	return useQuery({
		queryKey: ["applications", "detail", id],
		queryFn: () => applicationApi.getDetail(id!),
		enabled: !!id,
	});
}
