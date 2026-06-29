import type { PageResponse } from "@/types/pagination";
import type { NotificationResponse } from "@/types/notification";
import client from "./client";

const notificationApi = {
	getMyNotifications: (params: { page?: number; size?: number } = {}): Promise<PageResponse<NotificationResponse>> =>
		client.get("/notifications", { params }).then((r) => r.data),

	getUnreadCount: (): Promise<number> => client.get("/notifications/unread-count").then((r) => r.data),

	markAsRead: (id: string): Promise<void> => client.patch(`/notifications/${id}/read`).then(() => undefined),

	markAllAsRead: (): Promise<void> => client.patch("/notifications/read-all").then(() => undefined),
};

export default notificationApi;
