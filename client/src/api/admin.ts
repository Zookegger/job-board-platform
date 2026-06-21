import type { AdminPendingCompanyResponse } from "@/types/company";
import type { AdminPendingJobResponse } from "@/types/job";
import { toPageableParams, type PageResponse, type PaginationParams } from "@/types/pagination";
import type { SkillRequest, SkillResponse } from "@/types/skill";
import ApiError from "@/utils/ApiError";
import client from "./client";

export interface PendingCompaniesParams {
	page?: number;
	size?: number;
	keyword?: string;
	hasTaxCode?: boolean;
	hasContact?: boolean;
	sortBy?: "createdAt" | "companyName";
	direction?: "asc" | "desc";
}

const withoutEmptyParams = (params: PendingCompaniesParams) =>
	Object.fromEntries(Object.entries(params).filter(([, value]) => value !== undefined && value !== ""));

const adminApi = {
	// Companies Review
	getPendingCompanies: (params: PendingCompaniesParams): Promise<PageResponse<AdminPendingCompanyResponse>> =>
		client
			.get("/admin/companies/pending", { params: withoutEmptyParams(params) })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể tải danh sách công ty chờ duyệt.",
					error.response?.status || 500,
				);
			}),

	approveCompany: (companyId: string) =>
		client
			.post(`/admin/companies/${companyId}/approve`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể duyệt công ty.",
					error.response?.status || 500,
				);
			}),

	rejectCompany: (companyId: string, reason: string) =>
		client
			.post(`/admin/companies/${companyId}/reject`, null, { params: { reason } })
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể từ chối công ty.",
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
			.post(`/admin/jobs/${jobId}/approve`)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Lỗi, Không thể duyệt tin tuyển dụng.",
					error.response?.status || 500,
				);
			}),

	rejectJob: (jobId: string, reason: string) =>
		client
			.post(`/admin/jobs/${jobId}/reject`, { reason })
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
};

export default adminApi;
