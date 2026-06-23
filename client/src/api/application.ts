import type { ApplicationRequest, ApplicationResponse } from "@/types/application";
import ApiError from "@/utils/ApiError";
import client from "./client";

const applicationApi = {
	submit: (request: ApplicationRequest): Promise<ApplicationResponse> =>
		client
			.post("/applications", request)
			.then((res) => res.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
};

export default applicationApi;
