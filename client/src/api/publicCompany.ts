import type { PageResponse } from "@/types/pagination";
import ApiError from "@/utils/ApiError";
import client from "./client";

export interface PublicCompany {
	name: string;
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
	categories?: Array<{ id: number; name: string }>;
}

export interface PublicCompanyJob {
	id: string;
	title: string;
	slug?: string;
	location?: string;
	status?: string;
	postedDate?: string;
	createdAt?: string;
	companyId?: string;
	companyName?: string;
	companySlug?: string;
	categoryName?: string;
	skills?: Array<{ id: number; name: string; isActive: boolean }>;
}

export interface PublicCompanyListItem {
	name: string;
	slug: string;
	logoUrl?: string;
	description?: string;
	address?: string;
	website?: string;
	totalOpenJobs?: number;
	categories?: Array<{ id: number; name: string }>;
}

export interface PublicCompanyListParams {
	keyword?: string;
	categoryId?: number[];
	page?: number;
	size?: number;
}

const publicCompanyApi = {
	getDetail: (slug: string): Promise<PublicCompany> =>
		client
			.get(`/companies/public/${slug}`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải thông tin công ty.",
					error.response?.status || 500,
				);
			}),

	getJobs: (slug: string, page = 0, size = 6): Promise<PageResponse<PublicCompanyJob>> =>
		client
			.get(`/companies/public/${slug}/jobs`, { params: { page, size } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách việc làm.",
					error.response?.status || 500,
				);
			}),

	getList: (params: PublicCompanyListParams = {}): Promise<PageResponse<PublicCompanyListItem>> =>
		client
			.get("/companies/public/search", { params: { page: params.page ?? 0, size: params.size ?? 12, keyword: params.keyword, categoryId: params.categoryId } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách công ty.",
					error.response?.status || 500,
				);
			}),
};

export default publicCompanyApi;
