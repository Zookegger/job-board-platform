import type { PageResponse } from "@/types/pagination";
import ApiError from "@/utils/ApiError";
import client from "./client";

export interface PublicCompany {
	id: string;
	companyName: string;
	slug: string;
	logoUrl?: string;
	description?: string;
	website?: string;
	email?: string;
	phone?: string;
	address?: string;
	taxCode?: string;
	createdAt?: string;
	totalOpenJobs: number;
}

export interface PublicCompanyJob {
	id: string;
	title: string;
	location?: string;
	status?: string;
	createdAt?: string;
}

const publicCompanyApi = {
	getDetail: (slug: string): Promise<PublicCompany> =>
		client
			.get(`/public/companies/${slug}`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải thông tin công ty.",
					error.response?.status || 500,
				);
			}),

	getJobs: (slug: string, page = 0, size = 6): Promise<PageResponse<PublicCompanyJob>> =>
		client
			.get(`/public/companies/${slug}/jobs`, { params: { page, size } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách việc làm.",
					error.response?.status || 500,
				);
			}),
};

export default publicCompanyApi;
