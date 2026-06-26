import type { EmployerApplicationListResponse, EmployerApplicationParams } from "@/types/application";
import type { PageResponse } from "@/types/pagination";
import ApiError from "@/utils/ApiError";
import { withoutEmptyParams } from "@/utils/ApiUtils";
import client from "./client";

export const employerApplicationApi = {
	getEmployerApplications: (
		params: EmployerApplicationParams,
	): Promise<PageResponse<EmployerApplicationListResponse>> =>
		client
			.get("/employer/applications", { params: withoutEmptyParams(params as Record<string, unknown>) })
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách ứng viên.",
					error.response?.status || 500,
				);
			}),

	updateApplicationStatus: (
		id: string,
		status: string,
		reason?: string,
	): Promise<void> =>
		client
			.put(`/employer/applications/${id}/status`, null, {
				params: withoutEmptyParams({ status, reason } as Record<string, unknown>),
			})
			.then(() => undefined)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Cập nhật trạng thái thất bại.",
					error.response?.status || 500,
				);
			}),
};
