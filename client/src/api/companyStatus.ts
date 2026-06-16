import ApiError from "@/utils/ApiError";
import client from "./client";

export interface CompanyStatusResponse {
	companyId: string;
	name: string;
	taxCode: string | null;
	approvalStatus: "PENDING" | "APPROVED" | "REJECTED" | "SUSPENDED";
	submittedAt: string;
	reviewNote: string | null;
	reviewedAt: string | null;
}

export interface ApprovalLogResponse {
	actorId: string;
	oldStatus: "PENDING" | "APPROVED" | "REJECTED" | "SUSPENDED" | null;
	newStatus: "PENDING" | "APPROVED" | "REJECTED" | "SUSPENDED";
	note: string | null;
	createdAt: string;
}

const companyStatusApi = {
	/** Lấy trạng thái phê duyệt công ty của employer hiện tại. */
	getStatus: (): Promise<CompanyStatusResponse> =>
		client
			.get("/company/status")
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải trạng thái công ty.",
					error.response?.status || 500,
				);
			}),

	/** Lấy lịch sử phê duyệt công ty của employer hiện tại. */
	getHistory: (): Promise<ApprovalLogResponse[]> =>
		client
			.get("/company/approval-history")
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải lịch sử phê duyệt.",
					error.response?.status || 500,
				);
			}),
};

export default companyStatusApi;
