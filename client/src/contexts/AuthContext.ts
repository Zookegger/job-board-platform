import { createContext } from "react";
import type { LoginRequest, UserResponse } from "../types/auth";

interface AuthContextType {
	user: UserResponse | null;
	isLoading: boolean;
	isAuthenticated: boolean;
	login: (data: LoginRequest) => Promise<void>;
	logout: () => Promise<void>;
}

export const AuthContext = createContext<AuthContextType | null>(null);
