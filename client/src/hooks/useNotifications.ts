import notificationApi from "@/api/notification";
import type { PaginationParams } from "@/types/pagination";
import { keepPreviousData, useMutation, useQuery, useQueryClient } from "@tanstack/react-query";

const NOTIFICATION_POLL_INTERVAL = 15_000; // poll every 15s

export const NOTIFICATION_KEYS = {
	all: ["notifications"] as const,
	list: (params: PaginationParams) => ["notifications", "list", params] as const,
	unreadCount: ["notifications", "unread-count"] as const,
};

export function useNotifications(params: PaginationParams) {
	return useQuery({
		queryKey: NOTIFICATION_KEYS.list(params),
		queryFn: () => notificationApi.getMyNotifications(params),
		placeholderData: keepPreviousData,
		retry: false,
		refetchInterval: NOTIFICATION_POLL_INTERVAL,
	});
}

export function useUnreadCount() {
	return useQuery({
		queryKey: NOTIFICATION_KEYS.unreadCount,
		queryFn: () => notificationApi.getUnreadCount(),
		retry: false,
		refetchInterval: NOTIFICATION_POLL_INTERVAL,
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
		onSettled: () => {
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
		onSettled: () => {
			queryClient.invalidateQueries({ queryKey: NOTIFICATION_KEYS.all });
		},
	});
}
