import type { CreateReportRequest, ReportResponse } from "@/types/report";
import ApiError from "@/utils/ApiError";
import client from "./client";

const reportApi = {
	createReport: (data: CreateReportRequest): Promise<ReportResponse> =>
		client
			.post("/reports", data)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể gửi báo cáo.",
					error.response?.status || 500,
				);
			}),
};

export default reportApi;
