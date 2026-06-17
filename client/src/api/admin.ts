import type { AdminPendingCompanyResponse } from "@/types/company";
import ApiError from "@/utils/ApiError";
import client from "./client";

export interface PageResponse<T> {
	content: T[];
	totalElements: number;
	totalPages: number;
	size: number;
	number: number;
	first: boolean;
	last: boolean;
	numberOfElements: number;
	empty: boolean;
}

export interface PendingCompaniesParams {
	page?: number;
	size?: number;
	keyword?: string;
	hasTaxCode?: boolean;
	hasContact?: boolean;
	sortBy?: "createdAt" | "companyName";
	direction?: "asc" | "desc";
}

const withoutEmptyParams = (params: PendingCompaniesParams) =>
	Object.fromEntries(
		Object.entries(params).filter(([, value]) => value !== undefined && value !== ""),
	);

const adminApi = {
	getPendingCompanies: (
		params: PendingCompaniesParams,
	): Promise<PageResponse<AdminPendingCompanyResponse>> =>
		client
			.get("/admin/companies/pending", { params: withoutEmptyParams(params) })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Khong the tai danh sach cong ty cho duyet.",
					error.response?.status || 500,
				);
			}),

	approveCompany: (companyId: string) =>
		client
			.post(`/admin/companies/${companyId}/approve`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Khong the duyet cong ty.",
					error.response?.status || 500,
				);
			}),

	rejectCompany: (companyId: string, reason: string) =>
		client
			.post(`/admin/companies/${companyId}/reject`, null, { params: { reason } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Khong the tu choi cong ty.",
					error.response?.status || 500,
				);
			}),
};

export default adminApi;
