import { useAuth } from "@/hooks/useAuth";
import { useMediaQuery } from "@/hooks/useMediaQuery";
import RouterRoutes from "@/utils/RouterRoutes";
import type { LucideIcon } from "lucide-react";
import { EllipsisVertical, LogOut, Menu, UserIcon } from "lucide-react";
import { useEffect, useRef, useState } from "react";
import { NavLink, useNavigate } from "react-router-dom";
import UserAvatar from "../shared/UserAvatar";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "../ui/dropdown-menu";
import { Sheet, SheetContent } from "../ui/sheet";

interface NavItem {
	to: string;
	label: string;
	icon: LucideIcon;
}

interface DashboardSidebarProps {
	navItems: NavItem[];
	isOpen: boolean;
	onClose: () => void;
}

interface SidebarContentProps {
	navItems: NavItem[];
	isCollapsed: boolean;
	onToggle: () => void;
}

function SidebarContent({ navItems, isCollapsed, onToggle }: SidebarContentProps) {
	const { user, logout } = useAuth();
	const navigate = useNavigate();

	return (
		<>
			<div
				className={`flex h-16 items-center border-b px-6 ${isCollapsed ? "justify-center px-0" : "justify-between"}`}
			>
				{!isCollapsed && <span className='text-xl font-bold text-primary'>JobBoard</span>}
				<button
					onClick={onToggle}
					className='text-muted-foreground hover:text-foreground transition-colors cursor-pointer'
				>
					<Menu className='h-5 w-5' />
				</button>
			</div>

			<nav className='flex-1 space-y-1 p-4'>
				{navItems.map((item) => (
					<NavLink
						key={item.to}
						to={item.to}
						end
						className={({ isActive }) =>
							`flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium transition-colors ${
								isActive
									? "bg-primary/10 text-primary"
									: "text-muted-foreground hover:bg-accent hover:text-accent-foreground"
							} ${isCollapsed ? "justify-center px-0" : ""}`
						}
					>
						<item.icon className='h-4 w-4 shrink-0' />
						{!isCollapsed && <span>{item.label}</span>}
					</NavLink>
				))}
			</nav>

			<div className='border-t p-4'>
				<DropdownMenu>
					<div className={`flex items-center ${isCollapsed ? "justify-center" : "justify-between"}`}>
						{isCollapsed ? (
							<DropdownMenuTrigger className='rounded-full outline-none focus-visible:ring-2 focus-visible:ring-ring cursor-pointer'>
								<UserAvatar
									size='sm'
									fullName={user?.fullName || "User"}
									avatarUrl={user?.avatarUrl}
								/>
							</DropdownMenuTrigger>
						) : (
							<>
								<UserAvatar
									size='sm'
									fullName={user?.fullName || "User"}
									avatarUrl={user?.avatarUrl}
								/>
								<div className='truncate text-sm align-middle font-medium select-none'>
									{user?.fullName}
								</div>
								<DropdownMenuTrigger className='flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-accent rounded-lg px-1 py-2 cursor-pointer'>
									<EllipsisVertical className='h-4 w-4 shrink-0' />
								</DropdownMenuTrigger>
							</>
						)}

						<DropdownMenuContent
							align={isCollapsed ? "end" : "end"}
							side={isCollapsed ? "right" : "bottom"}
							sideOffset={isCollapsed ? 16 : 4}
							style={{ minWidth: 255 }}
						>
							<DropdownMenuItem onSelect={() => navigate(RouterRoutes.PROFILE)}>
								<button className='cursor-pointer flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground'>
									<UserIcon className='h-4 w-4' />
									Hồ sơ
								</button>
							</DropdownMenuItem>
							<DropdownMenuItem onClick={logout}>
								<button className='cursor-pointer flex w-full items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-muted-foreground transition-colors hover:bg-accent hover:text-accent-foreground'>
									<LogOut className='h-4 w-4' />
									Đăng xuất
								</button>
							</DropdownMenuItem>
						</DropdownMenuContent>
					</div>
				</DropdownMenu>
			</div>
		</>
	);
}

export function DashboardSidebar({ navItems, isOpen, onClose }: DashboardSidebarProps) {
	const isMobile = useMediaQuery("sm");
	const [isDesktopCollapsed, setIsDesktopCollapsed] = useState(false);
	const prevIsMobileRef = useRef(isMobile);

	useEffect(() => {
		// Only close if we just transitioned from mobile -> desktop
		if (prevIsMobileRef.current === true && isMobile === false) {
			onClose();
		}
		prevIsMobileRef.current = isMobile;
	}, [isMobile, onClose]);

	return (
		<>
			<aside
				className={`hidden lg:flex h-screen flex-col border-r bg-card transition-all duration-300 ${isDesktopCollapsed ? "w-[80px] min-w-[80px]" : "w-64 min-w-[200px]"}`}
			>
				<SidebarContent
					navItems={navItems}
					isCollapsed={isDesktopCollapsed}
					onToggle={() => setIsDesktopCollapsed((prev) => !prev)}
				/>
			</aside>

			<div className='lg:hidden'>
				<Sheet
					open={isOpen}
					onOpenChange={(open) => {
						if (!open) onClose();
					}}
				>
					<SheetContent
						side='left'
						className='max-w-dvw p-0 max-h-dvh'
					>
						<SidebarContent
							navItems={navItems}
							isCollapsed={false}
							onToggle={onClose}
						/>
					</SheetContent>
				</Sheet>
			</div>
		</>
	);
}
