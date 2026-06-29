import { useAuth } from "@/hooks/useAuth";
import { useMarkAllAsRead, useNotifications, useUnreadCount } from "@/hooks/useNotifications";
import type { NotificationResponse, NotificationType } from "@/types/notification";
import { UserRole } from "@/types/auth";
import RouterRoutes from "@/utils/RouterRoutes";
import { Bell, BellOff, Briefcase, Building2, CheckCheck, Inbox } from "lucide-react";
import { Link } from "react-router-dom";
import { Button } from "../ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuTrigger,
} from "../ui/dropdown-menu";

function notificationIcon(type: NotificationType) {
	switch (type) {
		case "APPLICATION_STATUS_CHANGED":
		case "APPLICATION_RECEIVED":
			return <Inbox className='h-4 w-4 text-blue-500' />;
		case "JOB_STATUS_CHANGED":
		case "JOB_PENDING_REVIEW":
			return <Briefcase className='h-4 w-4 text-violet-500' />;
		case "COMPANY_STATUS_CHANGED":
		case "COMPANY_PENDING_REVIEW":
			return <Building2 className='h-4 w-4 text-amber-500' />;
		default:
			return <Bell className='h-4 w-4 text-muted-foreground' />;
	}
}

function NotificationDropdownItem({ n }: { n: NotificationResponse }) {
	return (
		<div className={`flex items-start gap-3 px-4 py-3 transition-colors hover:bg-accent/60 ${!n.isRead ? "bg-primary/5" : ""}`}>
			<div className='mt-0.5 flex h-7 w-7 shrink-0 items-center justify-center rounded-full border bg-background'>
				{notificationIcon(n.type)}
			</div>
			<div className='flex min-w-0 flex-1 flex-col gap-0.5'>
				<p className={`text-xs leading-snug ${!n.isRead ? "font-semibold text-foreground" : "text-foreground/80"}`}>
					{n.message}
				</p>
			</div>
			{!n.isRead && <span className='mt-1 h-2 w-2 shrink-0 rounded-full bg-primary' />}
		</div>
	);
}

export function NotificationBell() {
	const { isAuthenticated, user } = useAuth();
	const { data: count = 0 } = useUnreadCount();
	const { data } = useNotifications({ page: 0, size: 5 });
	const { mutate: markAllAsRead, isPending } = useMarkAllAsRead();

	if (!isAuthenticated || user?.role === UserRole.ADMIN) return null;

	const notifications = data?.content ?? [];
	const displayCount = count > 99 ? "99+" : count;
	const hasNotifications = notifications.length > 0;

	return (
		<DropdownMenu>
			<DropdownMenuTrigger asChild>
				<Button
					variant='ghost'
					size='icon'
					className='relative text-muted-foreground hover:text-foreground'
					aria-label={`Thông báo${count > 0 ? ` (${displayCount} chưa đọc)` : ""}`}
				>
					<Bell className='h-5 w-5' />
					{count > 0 && (
						<span className='absolute top-1 right-1 min-w-[18px] h-[18px] translate-x-1/2 -translate-y-1/2 rounded-full bg-red-500 text-white text-[10px] font-bold flex items-center justify-center px-1 leading-none'>
							{displayCount}
						</span>
					)}
				</Button>
			</DropdownMenuTrigger>

			<DropdownMenuContent align='end' className='w-80 p-0' sideOffset={8}>
				{/* Header */}
				<div className='flex items-center justify-between border-b px-4 py-3'>
					<span className='text-sm font-semibold'>Thông báo</span>
					{count > 0 && (
						<button
							onClick={() => markAllAsRead()}
							disabled={isPending}
							className='flex items-center gap-1 text-xs text-primary hover:underline disabled:opacity-50'
						>
							<CheckCheck className='h-3.5 w-3.5' />
							Đánh dấu đã đọc tất cả
						</button>
					)}
				</div>

				{/* Body */}
				{hasNotifications ? (
					<div className='max-h-80 overflow-y-auto divide-y divide-border'>
						{notifications.map((n) => (
							<NotificationDropdownItem key={n.id} n={n} />
						))}
					</div>
				) : (
					<div className='flex flex-col items-center gap-3 py-10 text-center text-muted-foreground'>
						<BellOff className='h-10 w-10 opacity-30' />
						<p className='text-sm'>Bạn chưa có thông báo nào</p>
					</div>
				)}

				{/* Footer */}
				<div className='border-t px-4 py-2.5 text-center'>
					<Link
						to={RouterRoutes.NOTIFICATIONS}
						className='text-xs font-medium text-primary hover:underline'
					>
						Xem tất cả thông báo
					</Link>
				</div>
			</DropdownMenuContent>
		</DropdownMenu>
	);
}
