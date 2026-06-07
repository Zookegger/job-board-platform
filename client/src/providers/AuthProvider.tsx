import { useMutation, useQuery, useQueryClient } from "@tanstack/react-query";
import AuthApi from "../api/auth";
import { AuthContext } from "../contexts/AuthContext";
import type { LoginRequest, UserResponse } from "../types/auth";

export default function AuthProvider({ children }: { children: React.ReactNode }) {
    const queryClient = useQueryClient();

    const { data: user = null, isLoading } = useQuery<UserResponse | null>({
        queryKey: ['auth-user'],
        queryFn: async () => {
            try {
                const res = await AuthApi.me();
                if (!res || !res.id) return null;
                return res;
            } catch {
                return null; // Nếu lỗi (hết hạn token) xem như chưa login
            }
        },
        staleTime: Infinity, // Giữ dữ liệu profile cố định, không tự refetch liên tục
        retry: false, // Không cố thử lại khi lỗi kết nối / không có token
    });

    const loginMutation = useMutation({
        mutationFn: async (data: LoginRequest) => {
            await AuthApi.login(data);
            return await AuthApi.me();
        },
        onSuccess: (userData) => {
            queryClient.setQueryData(['auth-user'], userData);
        },
    });

    const logoutMutation = useMutation({
        mutationFn: async () => await AuthApi.logout(),
        onSuccess: () => {
            queryClient.setQueryData(['auth-user'], null);
            queryClient.clear();
        }
    });

    const login = async (data: LoginRequest) => {
        await loginMutation.mutateAsync(data);
    }

    const logout = async () => {
        await logoutMutation.mutateAsync();
    }

    return (
        <AuthContext.Provider value={{ user, isLoading, isAuthenticated: !!user, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
}
