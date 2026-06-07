import type {
	AuthResponse,
	CandidateRegisterRequest,
	LoginRequest,
	UserResponse,
} from "../types/auth";
import client from "./client";

export const AuthApi = {
	login: (request: LoginRequest): Promise<AuthResponse> =>
		client.post<AuthResponse>("/auth/login", request).then((response) => response.data),

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
