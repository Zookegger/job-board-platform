import { Button } from "@/components/ui/button";
import { useMarkAllAsRead, useMarkAsRead, useNotifications } from "@/hooks/useNotifications";
import type { NotificationResponse, NotificationType } from "@/types/notification";
import { TimeAgo } from "@/utils/DateUtils";
import { BellOff, Briefcase, Building2, CheckCheck, ChevronLeft, ChevronRight, Inbox } from "lucide-react";
import { useState } from "react";

const PAGE_SIZE = 15;

function notificationIcon(type: NotificationType) {
	switch (type) {
		case "APPLICATION_STATUS_CHANGED":
		case "APPLICATION_RECEIVED":
			return <Inbox className='h-5 w-5 shrink-0 text-blue-500' />;
		case "JOB_STATUS_CHANGED":
		case "JOB_PENDING_REVIEW":
			return <Briefcase className='h-5 w-5 shrink-0 text-violet-500' />;
		case "COMPANY_STATUS_CHANGED":
		case "COMPANY_PENDING_REVIEW":
			return <Building2 className='h-5 w-5 shrink-0 text-amber-500' />;
		default:
			return <Inbox className='h-5 w-5 shrink-0 text-muted-foreground' />;
	}
}

function NotificationItem({ notification }: { notification: NotificationResponse }) {
	const { mutate: markAsRead } = useMarkAsRead();

	const handleClick = () => {
		if (!notification.isRead) {
			markAsRead(notification.id);
		}
	};

	return (
		<div
			onClick={handleClick}
			className={`flex cursor-pointer items-start gap-4 rounded-xl border px-4 py-3 transition-colors hover:bg-accent/60 ${
				!notification.isRead ? "border-primary/20 bg-primary/5" : "border-border bg-background"
			}`}
		>
			<div className='mt-0.5 flex h-9 w-9 shrink-0 items-center justify-center rounded-full border bg-background'>
				{notificationIcon(notification.type)}
			</div>

			<div className='flex min-w-0 flex-1 flex-col gap-0.5'>
				<p className={`text-sm leading-snug ${!notification.isRead ? "font-semibold text-foreground" : "font-normal text-foreground/80"}`}>
					{notification.message}
				</p>
				<span className='text-xs text-muted-foreground'>{TimeAgo(notification.createdAt)}</span>
			</div>

			{!notification.isRead && (
				<span className='mt-1.5 h-2 w-2 shrink-0 rounded-full bg-primary' />
			)}
		</div>
	);
}

export default function NotificationsPage() {
	const [page, setPage] = useState(0);
	const { data, isLoading, isFetching, isError } = useNotifications({ page, size: PAGE_SIZE });
	const { mutate: markAllAsRead, isPending: isMarkingAll } = useMarkAllAsRead();

	const notifications = data?.content ?? [];
	const totalPages = data?.totalPages ?? 0;
	const totalElements = data?.totalElements ?? 0;
	const unreadCount = notifications.filter((n) => !n.isRead).length;

	return (
		<div className='mx-auto flex w-full max-w-3xl flex-col gap-6 p-4 md:p-6'>
			{/* Header */}
			<div className='flex flex-col gap-1 sm:flex-row sm:items-center sm:justify-between'>
				<div>
					<h1 className='text-2xl font-bold tracking-tight text-foreground'>Thông báo</h1>
					<p className='text-sm text-muted-foreground'>
						{totalElements > 0 ? `${totalElements.toLocaleString("vi-VN")} thông báo` : "Không có thông báo nào"}
					</p>
				</div>

				{unreadCount > 0 && (
					<Button
						variant='outline'
						size='sm'
						className='gap-2 self-start sm:self-auto'
						onClick={() => markAllAsRead()}
						disabled={isMarkingAll}
					>
						<CheckCheck className='h-4 w-4' />
						Đánh dấu tất cả đã đọc
					</Button>
				)}
			</div>

			{/* List */}
			{isLoading ? (
				<div className='flex flex-col gap-2'>
					{Array.from({ length: 5 }).map((_, i) => (
						<div key={i} className='h-16 animate-pulse rounded-xl border bg-muted' />
					))}
				</div>
			) : isError || notifications.length === 0 ? (
				<div className='flex flex-col items-center gap-3 rounded-xl border border-dashed py-16 text-center text-muted-foreground'>
					<BellOff className='h-10 w-10 opacity-40' />
					<p className='text-sm font-medium'>Bạn chưa có thông báo nào</p>
				</div>
			) : (
				<div className={`flex flex-col gap-2 transition-opacity ${isFetching ? "opacity-60" : ""}`}>
					{notifications.map((n) => (
						<NotificationItem key={n.id} notification={n} />
					))}
				</div>
			)}

			{/* Pagination */}
			{totalPages > 1 && (
				<div className='flex items-center justify-between border-t pt-4'>
					<span className='text-sm text-muted-foreground'>
						Trang {page + 1} / {totalPages}
					</span>
					<div className='flex gap-2'>
						<Button
							variant='outline'
							size='sm'
							disabled={page === 0}
							onClick={() => setPage((p) => p - 1)}
						>
							<ChevronLeft className='h-4 w-4' />
							Trước
						</Button>
						<Button
							variant='outline'
							size='sm'
							disabled={page >= totalPages - 1}
							onClick={() => setPage((p) => p + 1)}
						>
							Tiếp
							<ChevronRight className='h-4 w-4' />
						</Button>
					</div>
				</div>
			)}
		</div>
	);
}
