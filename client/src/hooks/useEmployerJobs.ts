import { employerJobApi } from "@/api/employerJob";
import type { EmployerJobParams, JobRequest } from "@/types/job";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const EMPLOYER_JOB_KEY = {
	lists: () => ["employer", "jobs"] as const,
	list: (params: EmployerJobParams) => ["employer", "jobs", "list", params] as const,
	details: () => ["employer", "jobs", "details"] as const,
	detail: (id: string) => ["employer", "jobs", "detail", id] as const,
};

export function useEmployerJobs(params: EmployerJobParams) {
	return useQuery({
		queryKey: EMPLOYER_JOB_KEY.list(params),
		queryFn: () => employerJobApi.getEmployerJobs(params),
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useEmployerJobDetail(id: string, options?: { enabled?: boolean }) {
	return useQuery({
		queryKey: EMPLOYER_JOB_KEY.detail(id),
		queryFn: () => employerJobApi.getEmployerJobDetail(id),
		retry: false,
		enabled: options?.enabled ?? true,
	});
}

export function useCreateEmployerJob() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (request: JobRequest) => employerJobApi.createEmployerJob(request),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEY.lists() });
		},
	});
}

export function useUpdateEmployerJob(id: string) {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (request: JobRequest) => employerJobApi.updateEmployerJob(id, request),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEY.detail(id) });
		},
	});
}

export function useSubmitForReview(id: string) {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: () => employerJobApi.submitForReview(id),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEY.detail(id) });
		},
	});
}

export function useDeleteEmployerJob(id: string) {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: () => employerJobApi.deleteEmployerJob(id),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEY.lists() });
		},
	});
}
