export interface LoginRequest {
	email: string;
	password: string;
}

export interface AuthResponse {
	accessToken: string;
	refreshToken: string;
	tokenType: string;
	expiresIn: number;
}

export interface UserResponse {
	id: string;
	email: string;
	role: UserRole;
	fullName: string;
	isActive: boolean;
}

export const UserRole = {
	ADMIN: "ADMIN",
	EMPLOYER: "EMPLOYER",
	CANDIDATE: "CANDIDATE",
} as const;

// Tự động trích xuất định dạng Type tương ứng từ các giá trị của object trên
export type UserRole = (typeof UserRole)[keyof typeof UserRole];

export interface CandidateRegisterRequest {
	email: string;
	fullName: string;
	password: string;
	confirmPassword: string;
}

export interface CompanyRegisterRequest {
	companyName: string;
	taxCode?: string;
	address: string;
	fullName: string;
	userEmail: string;
	userPhone: string;
	password: string;
	confirmPassword: string;
}