import { useQuery } from "@tanstack/react-query";
import publicJobApi from "@/api/jobs";

export const publicJobKeys = {
	all: ["public", "jobs"] as const,
	list: (page: number, size: number) => ["public", "jobs", "list", page, size] as const,
	detail: (slug: string) => ["public", "jobs", "detail", slug] as const,
};

export function usePublicJobs(page = 0, size = 12) {
	return useQuery({
		queryKey: publicJobKeys.list(page, size),
		queryFn: () => publicJobApi.getJobs(page, size),
	});
}

export function usePublicJobDetail(slug: string) {
	return useQuery({
		queryKey: publicJobKeys.detail(slug),
		queryFn: () => publicJobApi.getJobDetail(slug),
		enabled: !!slug,
	});
}
