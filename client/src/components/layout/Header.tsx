import { useAuth } from "@/hooks/useAuth";
import { UserRole } from "@/types/auth";
import RouterRoutes from "@/utils/RouterRoutes";
import { useNavigate } from "react-router-dom";
import UserAvatar from "../shared/UserAvatar";
import { Button } from "../ui/button";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "../ui/dropdown-menu";



export function Header() {
	const { isAuthenticated, user, logout } = useAuth();
	const navigate = useNavigate();

	const candidateItems = (
		<>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.PROFILE)}>Hồ sơ</DropdownMenuItem>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.CANDIDATE_APPLICATIONS)}>
				Đơn ứng tuyển
			</DropdownMenuItem>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.CANDIDATE_SAVED_JOBS)}>
				Việc đã lưu
			</DropdownMenuItem>
		</>
	);

	const employerItems = (
		<>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.PROFILE)}>Hồ sơ</DropdownMenuItem>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.EMPLOYER_DASHBOARD)}>
				Bảng điều khiển
			</DropdownMenuItem>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.EMPLOYER_JOBS)}>Việc làm của tôi</DropdownMenuItem>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.EMPLOYER_COMPANY)}>Công ty</DropdownMenuItem>
		</>
	);

	const adminItems = (
		<>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.ADMIN_DASHBOARD)}>Bảng điều khiển</DropdownMenuItem>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.ADMIN_USERS)}>Người dùng</DropdownMenuItem>
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.ADMIN_COMPANIES)}>Công ty</DropdownMenuItem>
		</>
	);

	return (
		<header className='border-b bg-background'>
			<nav className='mx-auto flex h-16 max-w-7xl items-center justify-between px-4'>
				<div className='flex items-center gap-6'>
					<Button
						onClick={() => navigate(RouterRoutes.HOME)}
						className='text-xl tracking-tight font-bold text-primary hover:text-primary-hover'
					>
						JobBoard
					</Button>

					<Button
						onClick={() => navigate(RouterRoutes.JOBS)}
						className='text-sm font-medium text-muted-foreground hover:text-foreground'
					>
						Việc làm
					</Button>
				</div>

				{!isAuthenticated ? (
					<div className='flex items-center gap-3'>
						<Button
							onClick={() => navigate(RouterRoutes.LOGIN)}
							className='text-sm font-medium text-muted-foreground hover:text-foreground'
						>
							Đăng nhập
						</Button>
						<Button
							onClick={() => navigate(RouterRoutes.REGISTER)}
							variant={"primary"}
						>
							Đăng ký
						</Button>
					</div>
				) : (
					<DropdownMenu>
						<DropdownMenuTrigger className='flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground'>
							<UserAvatar fullName={user?.fullName ?? ""} avatarUrl={user?.avatarUrl} />
							{user?.fullName}
						</DropdownMenuTrigger>
						<DropdownMenuContent align='end'>
							{user?.role === UserRole.CANDIDATE && candidateItems}
							{user?.role === UserRole.EMPLOYER && employerItems}
							{user?.role === UserRole.ADMIN && adminItems}
							<DropdownMenuSeparator />
							<DropdownMenuItem onSelect={logout}>Đăng xuất</DropdownMenuItem>
						</DropdownMenuContent>
					</DropdownMenu>
				)}
			</nav>
		</header>
	);
}
