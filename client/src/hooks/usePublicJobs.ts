import publicJobApi, { type JobRelatedSearchParams, type JobSearchParams } from "@/api/jobs";
import { keepPreviousData, useQuery } from "@tanstack/react-query";

export const publicJobKeys = {
	all: ["public", "jobs"] as const,
	list: (page: number, size: number) => ["public", "jobs", "list", page, size] as const,
	search: (params: JobSearchParams) => ["public", "jobs", "search", params] as const,
	detail: (slug: string) => ["public", "jobs", "detail", slug] as const,
};

export function usePublicJobs(page = 0, size = 12) {
	return useQuery({
		queryKey: publicJobKeys.list(page, size),
		queryFn: () => publicJobApi.getJobs(page, size),
	});
}

export function usePublicJobSearch(params: JobSearchParams) {
	return useQuery({
		queryKey: publicJobKeys.search(params),
		queryFn: () => publicJobApi.searchJobs(params),
		placeholderData: keepPreviousData,
	});
}

export function usePublicJobDetail(slug: string) {
	return useQuery({
		queryKey: publicJobKeys.detail(slug),
		queryFn: () => publicJobApi.getJobDetail(slug),
		enabled: !!slug,
	});
}

export function usePublicRelatedJobList(id: string, params: JobRelatedSearchParams) {
	return useQuery({
		queryKey: ["public", "jobs", "related", id, params],
		queryFn: () => publicJobApi.getRelatedJobs(id, params),
		enabled: !!id,
	});
}
