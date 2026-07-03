import { navigateTo } from "@/lib/navigate";
import ApiRoutes from "@/utils/ApiRoutes";
import RouterRoutes from "@/utils/RouterRoutes";
import axios from "axios";
import { toast } from "sonner";
import AuthApi from "./auth";

const API_BASE_URL = import.meta.env.VITE_API_URL ?? "http://localhost:5000";

const client = axios.create({
	baseURL: API_BASE_URL + "/api",
	withCredentials: true,
});

let isRefreshing = false;
let failedQueue: Array<{
	resolve: (value: unknown) => void;
	reject: (reason?: unknown) => void;
}> = [];

function processQueue(error: unknown) {
	failedQueue.forEach(({ resolve, reject }) => {
		if (error) {
			reject(error);
		} else {
			resolve(undefined);
		}
	});
	failedQueue = [];
}

client.interceptors.response.use(
	(response) => response,
	async (error) => {
		const originalConfig = error.config;

		if (
			error.response?.status === 401 &&
			!originalConfig._retry &&
			!originalConfig.url?.includes(ApiRoutes.REFRESH_TOKEN) 
			// && !originalConfig.url?.includes(ApiRoutes.ME)
		) {
			if (isRefreshing) {
				return new Promise((resolve, reject) => {
					failedQueue.push({ resolve, reject });
				}).then(() => client(originalConfig));
			}

			originalConfig._retry = true;
			isRefreshing = true;

			try {
				await AuthApi.refreshToken();
				processQueue(null);
				return client(originalConfig);
			} catch (refreshError) {
				processQueue(refreshError);
				navigateTo(RouterRoutes.LOGIN, { replace: true });
				return Promise.reject(refreshError);
			} finally {
				isRefreshing = false;
			}
		}

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
