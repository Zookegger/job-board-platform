import { useQuery } from "@tanstack/react-query";
import publicJobApi from "@/api/jobs";

export const publicJobKeys = {
	all: ["public", "jobs"] as const,
	list: (page: number, size: number) => ["public", "jobs", "list", page, size] as const,
	detail: (id: string) => ["public", "jobs", "detail", id] as const,
};

export function usePublicJobs(page = 0, size = 12) {
	return useQuery({
		queryKey: publicJobKeys.list(page, size),
		queryFn: () => publicJobApi.getJobs(page, size),
	});
}

export function usePublicJobDetail(id: string) {
	return useQuery({
		queryKey: publicJobKeys.detail(id),
		queryFn: () => publicJobApi.getJobDetail(id),
		enabled: !!id,
	});
}
