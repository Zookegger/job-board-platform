import type { JobListResponse, JobResponse } from "@/types/job";
import type { PageResponse } from "@/types/pagination";
import ApiRoutes from "@/utils/ApiRoutes";
import client from "./client";

const publicJobApi = {
	getJobs: (page = 0, size = 12): Promise<PageResponse<JobListResponse>> =>
		client
			.get<PageResponse<JobListResponse>>(ApiRoutes.PUBLIC_JOBS, { params: { page, size } })
			.then((r) => r.data),

	getJobDetail: (id: string): Promise<JobResponse> =>
		client.get<JobResponse>(ApiRoutes.PUBLIC_JOB_DETAIL(id)).then((r) => r.data),
};

export default publicJobApi;
