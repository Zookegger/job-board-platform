import type { JobListResponse, JobResponse } from "@/types/job";
import type { PageResponse } from "@/types/pagination";
import ApiRoutes from "@/utils/ApiRoutes";
import client from "./client";

export interface JobRelatedSearchParams extends JobSearchParams {
	related?: boolean;
}

export interface JobSearchParams {
	keyword?: string;
	categoryIds?: number[];
	locationTypes?: string[];
	employmentTypes?: string[];
	experienceLevels?: string[];
	minSalary?: number;
	maxSalary?: number;
	skillIds?: number[];
	sort?: string;
	page?: number;
	size?: number;
}

const publicJobApi = {
	getJobs: (page = 0, size = 12, sort?: string): Promise<PageResponse<JobListResponse>> =>
		client
			.get<PageResponse<JobListResponse>>(ApiRoutes.PUBLIC_JOBS, { params: { page, size, sort } })
			.then((r) => r.data),

	getRelatedJobs: (id: string, params: JobSearchParams): Promise<PageResponse<JobListResponse>> =>
		client
			.get<PageResponse<JobListResponse>>(ApiRoutes.PUBLIC_RELATED_JOBS(id), { params })
			.then((r) => r.data),

	searchJobs: (params: JobSearchParams): Promise<PageResponse<JobListResponse>> =>
		client
			.get<PageResponse<JobListResponse>>(ApiRoutes.PUBLIC_JOBS, { params })
			.then((r) => r.data),

	getJobDetail: (slug: string): Promise<JobResponse> =>
		client.get<JobResponse>(ApiRoutes.PUBLIC_JOB_DETAIL(slug)).then((r) => r.data),
};

export default publicJobApi;
