import { useAuth } from "@/hooks/useAuth";
import { UserRole, type UserResponse } from "@/types/auth";
import RouterRoutes from "@/utils/RouterRoutes";
import { Menu, X } from "lucide-react";
import { useState } from "react";
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
import { Sheet, SheetContent, SheetTrigger } from "../ui/sheet";

function MobileNav({ isAuthenticated, user, logout }: { isAuthenticated: boolean; user: UserResponse | null; logout: () => void }) {
	const navigate = useNavigate();

	return (
		<div className='flex flex-col gap-4 p-4'>
			<Button
				onClick={() => navigate(RouterRoutes.HOME)}
				className='text-xl tracking-tight font-bold text-primary hover:text-primary-hover justify-start'
			>
				JobBoard
			</Button>
			<div className='flex flex-col gap-1'>
				<Button
					onClick={() => navigate(RouterRoutes.JOBS)}
					variant='ghost'
					className='justify-start text-sm font-medium text-muted-foreground hover:text-foreground'
				>
					Việc làm
				</Button>
				<Button
					onClick={() => navigate(RouterRoutes.COMPANIES)}
					variant='ghost'
					className='justify-start text-sm font-medium text-muted-foreground hover:text-foreground'
				>
					Công ty
				</Button>
			</div>
			<div className='border-t pt-4'>
				{!isAuthenticated ? (
					<div className='flex flex-col gap-2'>
						<Button
							onClick={() => navigate(RouterRoutes.LOGIN)}
							variant='outline'
							className='w-full'
						>
							Đăng nhập
						</Button>
						<Button
							onClick={() => navigate(RouterRoutes.REGISTER)}
							variant='primary'
							className='w-full'
						>
							Đăng ký
						</Button>
					</div>
				) : (
					<div className='flex flex-col gap-1'>
						<div className='flex items-center gap-3 px-3 py-2'>
							<UserAvatar fullName={user?.fullName ?? ""} avatarUrl={user?.avatarUrl} />
							<span className='text-sm font-medium'>{user?.fullName}</span>
						</div>
						{user?.role === UserRole.CANDIDATE && (
							<>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.PROFILE)}>Hồ sơ</Button>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.CANDIDATE_APPLICATIONS)}>Đơn ứng tuyển</Button>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.CANDIDATE_SAVED_JOBS)}>Việc đã lưu</Button>
							</>
						)}
						{user?.role === UserRole.EMPLOYER && (
							<>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.PROFILE)}>Hồ sơ</Button>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.EMPLOYER_DASHBOARD)}>Bảng điều khiển</Button>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.EMPLOYER_JOBS)}>Việc làm của tôi</Button>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.EMPLOYER_COMPANY)}>Công ty</Button>
							</>
						)}
						{user?.role === UserRole.ADMIN && (
							<>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.ADMIN_DASHBOARD)}>Bảng điều khiển</Button>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.ADMIN_USERS)}>Người dùng</Button>
								<Button variant='ghost' className='justify-start' onClick={() => navigate(RouterRoutes.ADMIN_COMPANIES)}>Công ty</Button>
							</>
						)}
						<div className='border-t mt-2 pt-2'>
							<Button variant='ghost' className='justify-start w-full text-destructive' onClick={logout}>Đăng xuất</Button>
						</div>
					</div>
				)}
			</div>
		</div>
	);
}

export function Header() {
	const { isAuthenticated, user, logout } = useAuth();
	const navigate = useNavigate();
	const [open, setOpen] = useState(false);

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
			<DropdownMenuItem onSelect={() => navigate(RouterRoutes.ADMIN_SKILLS)}>Kỹ năng</DropdownMenuItem>
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
						className='hidden sm:inline text-sm font-medium text-muted-foreground hover:text-foreground'
					>
						Việc làm
					</Button>
					<Button
						onClick={() => navigate(RouterRoutes.COMPANIES)}
						variant='ghost'
						className='hidden sm:inline text-sm font-medium text-muted-foreground hover:text-foreground'
					>
						Công ty
					</Button>
				</div>

				<div className='flex items-center gap-3'>
					<div className='hidden lg:flex items-center gap-3'>
						{!isAuthenticated ? (
							<>
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
							</>
						) : (
							<DropdownMenu>
								<DropdownMenuTrigger className='flex items-center gap-2 text-sm font-medium text-muted-foreground hover:text-foreground'>
									<UserAvatar
										fullName={user?.fullName ?? ""}
										avatarUrl={user?.avatarUrl}
									/>
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
					</div>

					<Sheet
						open={open}
						onOpenChange={setOpen}
					>
						<SheetTrigger
							asChild
							className='lg:hidden'
						>
							<Button
								variant='ghost'
								size='icon'
							>
								{open ? <X className='h-5 w-5' /> : <Menu className='h-5 w-5' />}
							</Button>
						</SheetTrigger>
						<SheetContent
							side='right'
							className='w-70 sm:w-[320px]'
						>
							<MobileNav
								isAuthenticated={!!isAuthenticated}
								user={user}
								logout={logout}
							/>
						</SheetContent>
					</Sheet>
				</div>
			</nav>
		</header>
	);
}
