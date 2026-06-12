import { useAuth } from "@/hooks/useAuth";
import RouterRoutes from "@/utils/RouterRoutes";
import type { LucideIcon } from "lucide-react";
import { EllipsisVertical, LogOut, UserIcon } from "lucide-react";
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

function SidebarContent({ navItems }: { navItems: NavItem[] }) {
	const { user, logout } = useAuth();
	const navigate = useNavigate();

	return (
		<>
			<div className='flex h-16 items-center border-b px-6'>
				<span className='text-xl font-bold text-primary'>JobBoard</span>
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
							}`
						}
					>
						<item.icon className='h-4 w-4' />
						{item.label}
					</NavLink>
				))}
			</nav>

			<div className='border-t p-4'>
				<DropdownMenu>
					<div className='flex items-center justify-between'>
						<UserAvatar
							size='sm'
							fullName={user?.fullName || "User"}
							avatarUrl={user?.avatarUrl}
						/>
						<div className='truncate text-sm align-middle font-medium select-none'>{user?.fullName}</div>
						<DropdownMenuTrigger className='flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground hover:bg-accent rounded-lg px-1 py-2'>
							<EllipsisVertical className='h-4 w-4' />
						</DropdownMenuTrigger>
						<DropdownMenuContent
							align='end'
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
	return (
		<>
			<aside className='hidden lg:flex h-screen w-64 flex-col border-r bg-card'>
				<SidebarContent navItems={navItems} />
			</aside>

			<div className='lg:hidden'>
				<Sheet open={isOpen} onOpenChange={(open) => { if (!open) onClose(); }}>
					<SheetContent side='left' className='w-64 p-0'>
						<SidebarContent navItems={navItems} />
					</SheetContent>
				</Sheet>
			</div>
		</>
	);
}
