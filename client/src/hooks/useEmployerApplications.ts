import { employerApplicationApi } from "@/api/employerApplication";
import type { CandidateApplicationParams } from "@/types/application";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

export const EMPLOYER_APPLICATION_KEY = {
	lists: () => ["employer", "applications"] as const,
	list: (params: CandidateApplicationParams) => ["employer", "applications", "list", params] as const,
};

export function useEmployerApplications(params: CandidateApplicationParams) {
	return useQuery({
		queryKey: EMPLOYER_APPLICATION_KEY.list(params),
		queryFn: () => employerApplicationApi.getEmployerApplications(params),
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useUpdateApplicationStatus() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: ({ id, status, reason }: { id: string; status: string; reason?: string }) =>
			employerApplicationApi.updateApplicationStatus(id, status, reason),
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: EMPLOYER_APPLICATION_KEY.lists() });
		},
	});
}
