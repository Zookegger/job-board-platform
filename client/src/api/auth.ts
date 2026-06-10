import ApiError from "@/utils/ApiError";
import type { AuthResponse, CandidateRegisterRequest, LoginRequest, UserResponse } from "../types/auth";
import client from "./client";

export const AuthApi = {
	login: (request: LoginRequest): Promise<AuthResponse | null> =>
		client
			.post<AuthResponse>("/auth/login", request)
			.then((response) => response.data)
			.catch((error) => {
				// Nếu lỗi là 401 Unauthorized, trả về null để hiển thị thông báo lỗi đăng nhập
				if (error.response?.status === 401) {
					throw new ApiError("Email hoặc mật khẩu không đúng", error.response.status);
				}
				throw new ApiError(
					error.message || "Đã có lỗi xảy ra. Vui lòng thử lại sau.",
					error.response?.status || 500,
				);
			}),

	logout: (): Promise<void> => client.post("/auth/logout"),

	refreshToken: (): Promise<void> => client.post("/auth/refresh-token"),

	registerCandidate: (
		request: Omit<CandidateRegisterRequest, "confirmPassword"> & { confirmPassword: string },
	): Promise<void> => client.post("/auth/register/candidate", request),

	// TODO: Sau này thêm form cập nhật email + số điện thoại công ty trong Employer Dashboard
	registerCompany: (data: {
		companyName: string;
		address: string;
		taxCode?: string;
		fullName: string;
		phone: string;
		userEmail: string;
		password: string;
		confirmPassword: string;
	}): Promise<void> =>
		client.post("/auth/register/company", {
			companyName: data.companyName,
			address: data.address,
			taxCode: data.taxCode,
			fullName: data.fullName,
			userPhone: data.phone,
			userEmail: data.userEmail,
			password: data.password,
			confirmPassword: data.confirmPassword,
		}),

	me: (): Promise<UserResponse> => client.get<UserResponse>("/auth/me").then((response) => response.data),
};
export default AuthApi;
