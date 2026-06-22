import type { EmployerJobParams, JobListResponse, JobRequest, JobResponse } from "@/types/job";
import type { PageResponse } from "@/types/pagination";
import ApiError from "@/utils/ApiError";
import { withoutEmptyParams } from "@/utils/ApiUtils";
import client from "./client";

export const employerJobApi = {
	getEmployerJobs: (params: EmployerJobParams): Promise<PageResponse<JobListResponse>> =>
		client
			.get("/employer/jobs", { params: withoutEmptyParams(params as Record<string, unknown>) })
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách việc làm.",
					error.response?.status || 500,
				);
			}),

	getEmployerJobDetail: (id: string): Promise<JobResponse> =>
		client
			.get(`/employer/jobs/${id}`)
			.then((r) => r.data)
			.catch((error) => {
				if (error.response?.status === 404) throw new ApiError("Không tìm thấy tin tuyển dụng.", 404);
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải chi tiết tin tuyển dụng.",
					error.response?.status || 500,
				);
			}),

	createEmployerJob: (request: JobRequest): Promise<JobResponse> =>
		client
			.post("/employer/jobs", request)
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Tạo tin tuyển dụng thất bại.",
					error.response?.status || 500,
				);
			}),

	updateEmployerJob: (id: string, request: JobRequest): Promise<JobResponse> =>
		client
			.put(`/employer/jobs/${id}`, request)
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Cập nhật tin tuyển dụng thất bại.",
					error.response?.status || 500,
				);
			}),

	submitForReview: (id: string): Promise<{ message: string }> =>
		client
			.post(`/employer/jobs/${id}/submit`)
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Gửi duyệt thất bại.",
					error.response?.status || 500,
				);
			}),

	deleteEmployerJob: (id: string): Promise<{ message: string }> =>
		client
			.delete(`/employer/jobs/${id}`)
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Xóa tin tuyển dụng thất bại.",
					error.response?.status || 500,
				);
			}),
};
