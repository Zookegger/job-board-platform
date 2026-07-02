import type {
	AdminApplicationChartResponse,
	AdminDashboardStatsResponse,
	AdminUserListResponse,
	AdminUsersQueryParams,
	ReportsParams,
} from "@/types/admin";
import type { AdminCompanyListResponse, AdminPendingCompanyResponse } from "@/types/company";
import type { AdminPendingJobResponse } from "@/types/job";
import { toPageableParams, type PageResponse, type PaginationParams } from "@/types/pagination";
import type { SkillRequest, SkillResponse } from "@/types/skill";
import ApiError from "@/utils/ApiError";
import client from "./client";

const adminApi = {
	getDashboardStats: (): Promise<AdminDashboardStatsResponse> =>
		client
			.get("/admin/dashboard/stats")
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải thống kê dashboard.",
					error.response?.status || 500,
				);
			}),

	getApplicationChartStats: (days: number): Promise<AdminApplicationChartResponse> =>
		client
			.get("/admin/statistics/applications-chart", { params: { days } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải thống kê ứng tuyển.",
					error.response?.status || 500,
				);
			}),

	getUsers: (params: AdminUsersQueryParams): Promise<PageResponse<AdminUserListResponse>> =>
		client
			.get("/admin/users", {
				params: {
					...toPageableParams(params),
					keyword: params.keyword,
					role: params.role,
					isActive: params.isActive,
				},
			})
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải danh sách tài khoản.",
					error.response?.status || 500,
				);
			}),

	// Companies Review
	getAllCompanies: (
		params: PaginationParams,
		keyword?: string,
		status?: string,
	): Promise<PageResponse<AdminCompanyListResponse>> =>
		client
			.get("/admin/companies", { params: { ...params, keyword, status } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải danh sách công ty.",
					error.response?.status || 500,
				);
			}),

	getPendingCompanies: (
		params: PaginationParams,
		keyword?: string,
		hasTaxCode?: boolean,
		hasContact?: boolean,
		sortBy?: "createdAt" | "companyName",
		direction?: "asc" | "desc",
	): Promise<PageResponse<AdminPendingCompanyResponse>> =>
		client
			.get("/admin/companies/pending", {
				params: { ...params, keyword, hasTaxCode, hasContact, sortBy, direction },
			})
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải danh sách công ty chờ duyệt.",
					error.response?.status || 500,
				);
			}),

	approveCompany: (companyId: string) =>
		client
			.patch(`/admin/companies/${companyId}/approve`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể duyệt công ty.",
					error.response?.status || 500,
				);
			}),

	rejectCompany: (companyId: string, reason: string) =>
		client
			.patch(`/admin/companies/${companyId}/reject`, { reason })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể từ chối công ty.",
					error.response?.status || 500,
				);
			}),

	suspendCompany: (companyId: string, reason: string) =>
		client
			.patch(`/admin/companies/${companyId}/suspend`, { reason })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tạm ngưng công ty.",
					error.response?.status || 500,
				);
			}),

	unsuspendCompany: (companyId: string) =>
		client
			.patch(`/admin/companies/${companyId}/unsuspend`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể mở tạm ngưng công ty.",
					error.response?.status || 500,
				);
			}),

	// Jobs Review
	getPendingJobs: (page = 0, size = 10): Promise<PageResponse<AdminPendingJobResponse>> =>
		client
			.get("/admin/jobs/pending", { params: { page, size } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải danh sách tin chờ duyệt.",
					error.response?.status || 500,
				);
			}),

	approveJob: (jobId: string) =>
		client
			.patch(`/admin/jobs/${jobId}/approve`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể duyệt tin tuyển dụng.",
					error.response?.status || 500,
				);
			}),

	rejectJob: (jobId: string, reason: string) =>
		client
			.patch(`/admin/jobs/${jobId}/reject`, { reason })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể từ chối tin tuyển dụng.",
					error.response?.status || 500,
				);
			}),

	// Skills
	getAllSkills: (params: PaginationParams, keyword?: string, isActive?: boolean) =>
		client
			.get<PageResponse<SkillResponse>>("/admin/skills", {
				params: { ...toPageableParams(params), keyword, isActive },
			})
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải danh sách kỹ năng.",
					error.response?.status || 500,
				);
			}),

	createSkill: (request: SkillRequest) =>
		client
			.post<SkillResponse>("/admin/skills", request)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tạo kỹ năng.",
					error.response?.status || 500,
				);
			}),

	updateSkill: (skillId: number, request: SkillRequest) =>
		client
			.put<SkillResponse>(`/admin/skills/${skillId}`, request)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể cập nhật kỹ năng.",
					error.response?.status || 500,
				);
			}),

	deleteSkill: (skillId: number) =>
		client
			.delete(`/admin/skills/${skillId}`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể xóa kỹ năng.",
					error.response?.status || 500,
				);
			}),

	toggleSkillStatus: (skillId: number) =>
		client
			.patch<SkillResponse>(`/admin/skills/${skillId}/toggle-status`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể thay đổi trạng thái kỹ năng.",
					error.response?.status || 500,
				);
			}),

	getReports: (params: ReportsParams) =>
		client
			.get("/admin/reports", { params: { ...params } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải danh sách báo cáo.",
					error.response?.status || 500,
				);
			}),

	// Reports Actions
	reviewReport: (id: string, data?: { reviewNotes?: string }) =>
		client
			.patch(`/admin/reports/${id}/review`, data)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể duyệt báo cáo.",
					error.response?.status || 500,
				);
			}),

	dismissReport: (id: string, data?: { reviewNotes?: string }) =>
		client
			.patch(`/admin/reports/${id}/dismiss`, data)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể bác bỏ báo cáo.",
					error.response?.status || 500,
				);
			}),

	resolveReport: (id: string, data?: { reviewNotes?: string }) =>
		client
			.patch(`/admin/reports/${id}/resolve`, data)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể giải quyết báo cáo.",
					error.response?.status || 500,
				);
			}),
};

export default adminApi;
