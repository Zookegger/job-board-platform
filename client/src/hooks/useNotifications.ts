import notificationApi from "@/api/notification";
import type { NotificationResponse } from "@/types/notification";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

// ---------------------------------------------------------------------------
// Dữ liệu mẫu — chỉ dùng khi backend chưa sẵn sàng (DEV mode)
// ---------------------------------------------------------------------------
const MOCK_NOTIFICATIONS: NotificationResponse[] = [
	{
		id: "mock-1",
		type: "APPLICATION_STATUS_CHANGED",
		entityId: "app-001",
		message: "Chúc mừng! Bạn đã được mời phỏng vấn cho vị trí Frontend Developer tại Công ty ABC.",
		isRead: false,
		createdAt: new Date(Date.now() - 10 * 60 * 1000).toISOString(),
		readAt: null,
	},
	{
		id: "mock-2",
		type: "APPLICATION_STATUS_CHANGED",
		entityId: "app-002",
		message: "Đơn ứng tuyển của bạn cho vị trí Backend Engineer tại StartupXYZ đã được xem xét.",
		isRead: false,
		createdAt: new Date(Date.now() - 2 * 60 * 60 * 1000).toISOString(),
		readAt: null,
	},
	{
		id: "mock-3",
		type: "APPLICATION_STATUS_CHANGED",
		entityId: "app-003",
		message: "Rất tiếc, đơn ứng tuyển của bạn cho vị trí Data Analyst tại VinTech đã không được chọn.",
		isRead: true,
		createdAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000).toISOString(),
		readAt: new Date(Date.now() - 20 * 60 * 60 * 1000).toISOString(),
	},
];

const MOCK_UNREAD_COUNT = MOCK_NOTIFICATIONS.filter((n) => !n.isRead).length;

const MOCK_PAGE = {
	content: MOCK_NOTIFICATIONS,
	totalElements: MOCK_NOTIFICATIONS.length,
	totalPages: 1,
	size: 15,
	number: 0,
};

// ---------------------------------------------------------------------------

export const NOTIFICATION_KEYS = {
	all: ["notifications"] as const,
	list: (params: { page?: number; size?: number }) => ["notifications", "list", params] as const,
	unreadCount: ["notifications", "unread-count"] as const,
};

export function useNotifications(params: { page?: number; size?: number } = {}) {
	return useQuery({
		queryKey: NOTIFICATION_KEYS.list(params),
		queryFn: async () => {
			try {
				return await notificationApi.getMyNotifications(params);
			} catch (e) {
				if (import.meta.env.DEV) return MOCK_PAGE;
				throw e;
			}
		},
		placeholderData: keepPreviousData,
		retry: false,
	});
}

export function useUnreadCount() {
	return useQuery({
		queryKey: NOTIFICATION_KEYS.unreadCount,
		queryFn: async () => {
			try {
				return await notificationApi.getUnreadCount();
			} catch (e) {
				if (import.meta.env.DEV) return MOCK_UNREAD_COUNT;
				throw e;
			}
		},
		refetchInterval: 30_000, // poll every 30s
	});
}

export function useMarkAsRead() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: (id: string) => notificationApi.markAsRead(id),
		onMutate: () => {
			// Giảm ngay 1 đơn vị trước khi API trả về
			queryClient.setQueryData<number>(NOTIFICATION_KEYS.unreadCount, (prev = 0) => Math.max(0, prev - 1));
		},
		onError: () => {
			queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
		},
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
		},
	});
}

export function useMarkAllAsRead() {
	const queryClient = useQueryClient();
	return useMutation({
		mutationFn: () => notificationApi.markAllAsRead(),
		onMutate: () => {
			// Set về 0 ngay lập tức trước khi API trả về
			queryClient.setQueryData<number>(NOTIFICATION_KEYS.unreadCount, 0);
		},
		onError: () => {
			queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
		},
		onSuccess: () => {
			queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
		},
	});
}
