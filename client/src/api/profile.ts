import type {
	CandidateProfileRequest,
	CandidateProfileResponse,
	EmployerProfileRequest,
	EmployerProfileResponse,
} from "@/types/profile";
import ApiError from "@/utils/ApiError";
import client from "./client";

const profileApi = {
	// Candidate
	getCandidateProfile: (): Promise<CandidateProfileResponse | null> =>
		client
			.get("/profile/candidate")
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
	updateCandidateProfile: (request: CandidateProfileRequest) =>
		client
			.put("/profile/candidate", request)
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),

	// Employer
	getEmployerProfile: (): Promise<EmployerProfileResponse | null> =>
		client
			.get("/profile/employer")
			.then((response) => response.data)
			.catch((error) => {
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
	updateEmployerProfile: (request: EmployerProfileRequest) =>
		client
			.put("/profile/employer", request)
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),

	uploadAvatar: (file: File) => {
		const formData = new FormData();
		formData.append("file", file);
		if (file.size > 5 * 1024 * 1024) {
			throw new ApiError("Kích thước file vượt quá 5MB", 400);
		}

		if (file.name.match(/\.(jpg|jpeg|png|gif|pdf|webp|)$/i) === null) {
			throw new ApiError("Định dạng file không hợp lệ. Vui lòng chọn ảnh.", 400);
		}

		return client
			.post("/profile/avatar", formData, {
				headers: {
					"Content-Type": "multipart/form-data",
				},
			})
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			});
	},

	uploadCompanyLogo: (file: File) => {
		const ACCEPTED = ["image/png", "image/jpeg", "image/svg+xml", "image/webp"];
		if (!ACCEPTED.includes(file.type))
			throw new ApiError("Định dạng không hợp lệ. Chỉ chấp nhận PNG, JPG, SVG, WEBP.", 400);
		if (file.size > 2 * 1024 * 1024)
			throw new ApiError("File quá lớn. Dung lượng tối đa: 2MB", 400);

		const formData = new FormData();
		formData.append("file", file);
		return client
			.post<string>("/profile/company-logo", formData, {
				headers: { "Content-Type": "multipart/form-data" },
			})
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) throw new ApiError("", 401);
				throw new ApiError(
					error.response?.data?.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			});
	},

	getResume: () =>
		client
			.get("/profile/resume")
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
	uploadResume: (file: File) => {
		const formData = new FormData();
		formData.append("file", file);
		return client
			.post("/profile/resume", formData, {
				headers: {
					"Content-Type": "multipart/form-data",
				},
			})
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			});
	},
	updateResume: () =>
		client
			.put("/profile/resume")
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
	deleteResume: () =>
		client
			.delete("/profile/resume")
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
	downloadResume: () =>
		client
			.get("/profile/resume/download")
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
	previewResume: () =>
		client
			.get("/profile/resume/preview")
			.then((response) => response.data)
			.catch((error) => {
				if (error.response?.status === 401) {
					throw new ApiError("", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),
};

export default profileApi;
