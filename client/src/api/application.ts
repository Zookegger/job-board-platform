import type { ApplicationRequest, ApplicationResponse } from "@/types/application";
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

	checkApplied: (jobId: string): Promise<{ applied: boolean }> =>
		client
			.get(ApiRoutes.APPLICATION_CHECK(jobId))
			.then((res) => res.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Đã có lỗi xảy ra.",
					error.response?.status || 500,
				);
			}),
};

export default applicationApi;
