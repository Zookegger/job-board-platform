import { useMutation, useQuery } from "@tanstack/react-query";
import applicationApi from "@/api/application";
import type { ApplicationRequest } from "@/types/application";
import { UserRole } from "@/types/auth";
import { useAuth } from "./useAuth";

export function useSubmitApplication() {
	return useMutation({
		mutationFn: (request: ApplicationRequest) => applicationApi.submit(request),
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
