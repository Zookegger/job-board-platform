import ApiError from "@/utils/ApiError";
import client from "./client";

export interface EmployerDashboardStatsResponse {
	activeJobs: number;
	pendingApprovalJobs: number;
	draftJobs: number;
	expiredJobs: number;
	rejectedJobs: number;
	totalApplications: number;
	newApplicationsThisWeek: number;
	pendingApplications: number;
	reviewingApplications: number;
	interviewApplications: number;
	hiredApplications: number;
}

export const employerDashboardApi = {
	getEmployerDashboardStats: (): Promise<EmployerDashboardStatsResponse> =>
		client
			.get("/employer/dashboard/stats")
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải thống kê dashboard.",
					error.response?.status || 500,
				);
			}),
};
