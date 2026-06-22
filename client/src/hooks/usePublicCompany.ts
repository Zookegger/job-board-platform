import publicCompanyApi from "@/api/publicCompany";
import { keepPreviousData, useQuery } from "@tanstack/react-query";

export const PUBLIC_COMPANY_KEYS = {
	detail: (slug: string) => ["public", "company", slug] as const,
	jobs: (slug: string, page: number, size: number) =>
		["public", "company", slug, "jobs", page, size] as const,
};

export function usePublicCompany(slug: string) {
	return useQuery({
		queryKey: PUBLIC_COMPANY_KEYS.detail(slug),
		queryFn: () => publicCompanyApi.getDetail(slug),
		enabled: !!slug,
		retry: false,
	});
}

export function usePublicCompanyJobs(slug: string, page = 0, size = 6) {
	return useQuery({
		queryKey: PUBLIC_COMPANY_KEYS.jobs(slug, page, size),
		queryFn: () => publicCompanyApi.getJobs(slug, page, size),
		enabled: !!slug,
		placeholderData: keepPreviousData,
		retry: false,
	});
}
