import axios from "axios";
import { toast } from "sonner";
import AuthApi from "./auth";

const API_BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:8080/api";

const client = axios.create({
	baseURL: API_BASE_URL,
	withCredentials: true,
	headers: {
		"Content-Type": "application/json",
	},
});

client.interceptors.response.use(
	(response) => response,
	async (error) => {
		// Nếu nhận được lỗi 401 Unauthorized, thử làm mới token
		if (error.response?.status === 401 && !error.config._retry) {
			error.config._retry = true;
			try {
				await AuthApi.refreshToken();
				return client(error.config); // Thử lại request ban đầu sau khi làm mới token
			} catch {
				// Nếu làm mới token thất bại, chuyển hướng người dùng đến trang đăng nhập
				window.location.href = "/login";
			}
		}

		// Hiển thị toast cho các lỗi phổ biến (bỏ qua 401 đã xử lý ở trên)
		const status = error.response?.status;
		const message = error.response?.data?.message;
		if (status === 403) {
			toast.error(message || "Bạn không có quyền thực hiện hành động này", { position: "bottom-right" });
		} else if (status === 500) {
			toast.error("Lỗi hệ thống, vui lòng thử lại sau", { position: "bottom-right" });
		}

		return Promise.reject(error);
	},
);

export default client;
