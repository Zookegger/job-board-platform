import type { ApplicationRequest, ApplicationResponse, ApplicationTimelineResponse } from "@/types/application";
import ApiError from "@/utils/ApiError";
import ApiRoutes from "@/utils/ApiRoutes";
import client from "./client";

const applicationApi = {
	submit: (request: ApplicationRequest): Promise<ApplicationResponse> =>
		client
			.post(ApiRoutes.APPLICATIONS, request)
			.then((res) => res.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),

	getDetail: (id: string): Promise<ApplicationResponse> =>
		client
			.get(ApiRoutes.APPLICATION_DETAIL(id))
			.then((res) => res.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải chi tiết đơn ứng tuyển.",
					error.response?.status || 500,
				);
			}),

	getByJob: (jobId: string): Promise<{ applied: boolean; applicationId?: string }> =>
		client
			.get(ApiRoutes.APPLICATION_BY_JOB(jobId))
			.then((res) => res.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Đã có lỗi xảy ra.",
					error.response?.status || 500,
				);
			}),

	withdraw: (id: string): Promise<void> =>
		client
			.delete(ApiRoutes.APPLICATION_DETAIL(id))
			.then(() => undefined)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Rút đơn thất bại.",
					error.response?.status || 500,
				);
			}),

	getTimeline: (id: string): Promise<ApplicationTimelineResponse[]> =>
		client
			.get(ApiRoutes.APPLICATION_TIMELINE(id))
			.then((res) => res.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải lịch sử trạng thái.",
					error.response?.status || 500,
				);
			}),
};

export default applicationApi;
