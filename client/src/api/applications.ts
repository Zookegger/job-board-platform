import type { ApplicationListResponse, ApplicationStatus } from "@/types/application";
import type { PaginationParams, PageResponse } from "@/types/pagination";
import ApiError from "@/utils/ApiError";
import client from "./client";

export interface MyApplicationsParams extends PaginationParams {
	status?: ApplicationStatus;
}

const withoutEmptyParams = (params: MyApplicationsParams) =>
	Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== ""));

const applicationsApi = {
	getMyApplications: (params: MyApplicationsParams = {}): Promise<PageResponse<ApplicationListResponse>> =>
		client
			.get("/applications", { params: withoutEmptyParams(params) })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách đơn ứng tuyển.",
					error.response?.status || 500,
				);
			}),
};

export default applicationsApi;
