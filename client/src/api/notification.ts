import type { NotificationResponse } from "@/types/notification";
import type { PageResponse, PaginationParams } from "@/types/pagination";
import ApiError from "@/utils/ApiError";
import ApiRoutes from "@/utils/ApiRoutes";
import { withoutEmptyParams } from "@/utils/ApiUtils";
import client from "./client";

const notificationApi = {
	getMyNotifications: (params: PaginationParams): Promise<PageResponse<NotificationResponse>> =>
		client
			.get(ApiRoutes.NOTIFICATIONS, { params: withoutEmptyParams(params as Record<string, unknown>) })
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách thông báo.",
					error.response?.status || 500,
				);
			}),

	getUnreadCount: (): Promise<number> =>
		client
			.get(ApiRoutes.NOTIFICATIONS_UNREAD_COUNT)
			.then((r) => r.data)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể tải danh sách thông báo.",
					error.response?.status || 500,
				);
			}),

	markAsRead: (id: string): Promise<void> =>
		client
			.patch(ApiRoutes.NOTIFICATION_READ(id))
			.then(() => undefined)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể đánh dấu thông báo là đã đọc.",
					error.response?.status || 500,
				);
			}),

	markAllAsRead: (): Promise<void> =>
		client
			.patch(ApiRoutes.NOTIFICATIONS_READ_ALL)
			.then(() => undefined)
			.catch((error) => {
				throw new ApiError(
					error.response?.data?.message || error.message || "Không thể đánh dấu tất cả thông báo là đã đọc.",
					error.response?.status || 500,
				);
			}),
};

export default notificationApi;
