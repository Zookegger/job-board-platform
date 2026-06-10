import { createContext } from "react";
import type { LoginRequest, UserResponse } from "../types/auth";

interface AuthContextType {
	user: UserResponse | null;
	isLoading: boolean;
	isAuthenticated: boolean;
	login: (data: LoginRequest) => Promise<UserResponse | null>;
	logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | null>(null);
