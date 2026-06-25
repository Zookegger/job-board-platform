import type { PageResponse } from "@/types/pagination";
import type {
	CandidateSkillResponse,
	SkillResponse,
	UpdateCandidateSkillsRequest,
} from "@/types/skill";
import ApiError from "@/utils/ApiError";
import client from "./client";

const skillApi = {
	/** Lấy toàn bộ danh sách kỹ năng có sẵn. */
	getAllSkills: (): Promise<PageResponse<SkillResponse>> =>
		client
			.get("/skills")
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.message || "Không thể tải danh sách kỹ năng.",
					error.response?.status || 500,
				);
			}),

	/** Lấy kỹ năng hiện tại của ứng viên. */
	getCandidateSkills: (): Promise<CandidateSkillResponse[]> =>
		client
			.get("/skills/profile")
			.then((r) => r.data)
			.catch((error) => {
				if (error.response?.status === 401) throw new ApiError("", 401);
				throw new ApiError(
					error.message || "Không thể tải kỹ năng của bạn.",
					error.response?.status || 500,
				);
			}),

	/** Lấy toàn bộ danh sách kỹ năng (public — không phân trang). */
	getAllSkillsPublic: (): Promise<SkillResponse[]> =>
		client
			.get("/skills/public")
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.message || "Không thể tải danh sách kỹ năng.",
					error.response?.status || 500,
				);
			}),

	/** Cập nhật toàn bộ kỹ năng của ứng viên (replace). */
	updateCandidateSkills: (request: UpdateCandidateSkillsRequest): Promise<CandidateSkillResponse[]> =>
		client
			.put("/skills/profile", request)
			.then((r) => r.data)
			.catch((error) => {
				if (error.response?.status === 401) throw new ApiError("", 401);
				throw new ApiError(
					error.response?.data?.message || error.message || "Cập nhật kỹ năng thất bại.",
					error.response?.status || 500,
				);
			}),
};

export default skillApi;
