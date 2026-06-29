import { DataTable, type Column } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import { useAdminUsers } from "@/hooks/useAdminUsers";
import type {
	AdminUserListResponse,
	AdminUserRole,
	AdminUserStatus,
} from "@/types/admin";
import {
	RefreshCcw,
	ShieldCheck,
	UserCheck,
	UserCog,
	Users,
	UserX,
} from "lucide-react";
import { useMemo } from "react";
import { useSearchParams } from "react-router-dom";

const DEFAULT_PAGE = 0;
const DEFAULT_PAGE_SIZE = 10;

const ROLE_OPTIONS = [
	{ value: "ALL", label: "Tất cả vai trò" },
	{ value: "ADMIN", label: "Admin" },
	{ value: "EMPLOYER", label: "Nhà tuyển dụng" },
	{ value: "CANDIDATE", label: "Ứng viên" },
] as const;

const STATUS_OPTIONS = [
	{ value: "ALL", label: "Tất cả trạng thái" },
	{ value: "ACTIVE", label: "Đang hoạt động" },
	{ value: "INACTIVE", label: "Đã khóa" },
] as const;

type RoleFilter = (typeof ROLE_OPTIONS)[number]["value"];
type StatusFilter = (typeof STATUS_OPTIONS)[number]["value"];

function parsePositiveNumber(value: string | null, fallback: number) {
	const numberValue = Number(value);

	if (!Number.isInteger(numberValue) || numberValue < 0) {
		return fallback;
	}

	return numberValue;
}

function parsePageSize(value: string | null) {
	const size = parsePositiveNumber(value, DEFAULT_PAGE_SIZE);
	const allowedSizes = [5, 10, 20, 50, 100];

	return allowedSizes.includes(size) ? size : DEFAULT_PAGE_SIZE;
}

function parseRole(value: string | null): RoleFilter {
	if (ROLE_OPTIONS.some((option) => option.value === value)) {
		return value as RoleFilter;
	}

	return "ALL";
}

function parseStatus(value: string | null): StatusFilter {
	if (STATUS_OPTIONS.some((option) => option.value === value)) {
		return value as StatusFilter;
	}

	return "ALL";
}

function formatDateTime(value: string | null | undefined) {
	if (!value) return "Chưa có";

	return new Intl.DateTimeFormat("vi-VN", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}

function getRoleLabel(role: AdminUserRole) {
	const labels: Record<AdminUserRole, string> = {
		ADMIN: "Admin",
		EMPLOYER: "Nhà tuyển dụng",
		CANDIDATE: "Ứng viên",
	};

	return labels[role] ?? role;
}

function RoleBadge({ role }: { role: AdminUserRole }) {
	const styles: Record<AdminUserRole, string> = {
		ADMIN: "border-purple-500/40 bg-purple-500/10 text-purple-700",
		EMPLOYER: "border-blue-500/40 bg-blue-500/10 text-blue-700",
		CANDIDATE: "border-emerald-500/40 bg-emerald-500/10 text-emerald-700",
	};

	return (
		<Badge
			variant='outline'
			className={styles[role]}
		>
			{getRoleLabel(role)}
		</Badge>
	);
}

function StatusBadge({ status }: { status: AdminUserStatus }) {
	if (status === "ACTIVE") {
		return (
			<Badge
				variant='outline'
				className='border-emerald-500/40 bg-emerald-500/10 text-emerald-700'
			>
				Đang hoạt động
			</Badge>
		);
	}

	return (
		<Badge
			variant='outline'
			className='border-destructive/40 bg-destructive/10 text-destructive'
		>
			Đã khóa
		</Badge>
	);
}

function UserIdentity({ user }: { user: AdminUserListResponse }) {
	return (
		<div className='flex min-w-0 items-center gap-3'>
			<div className='flex size-10 shrink-0 items-center justify-center rounded-full bg-primary/10 text-sm font-semibold text-primary'>
				{user.fullName?.charAt(0)?.toUpperCase() || user.email.charAt(0).toUpperCase()}
			</div>

			<div className='min-w-0'>
				<p className='truncate font-medium text-foreground'>
					{user.fullName || "Chưa cập nhật"}
				</p>
				<p className='truncate text-sm text-muted-foreground'>{user.email}</p>
			</div>
		</div>
	);
}

export default function AdminUsersPage() {
	const [searchParams, setSearchParams] = useSearchParams();

	const page = parsePositiveNumber(searchParams.get("page"), DEFAULT_PAGE);
	const pageSize = parsePageSize(searchParams.get("size"));
	const role = parseRole(searchParams.get("role"));
	const status = parseStatus(searchParams.get("status"));

	function updateSearchParams(updates: {
		page?: number;
		size?: number;
		role?: RoleFilter;
		status?: StatusFilter;
	}) {
		const nextParams = new URLSearchParams(searchParams);

		if (updates.page !== undefined) {
			nextParams.set("page", String(updates.page));
		}

		if (updates.size !== undefined) {
			nextParams.set("size", String(updates.size));
		}

		if (updates.role !== undefined) {
			if (updates.role === "ALL") {
				nextParams.delete("role");
			} else {
				nextParams.set("role", updates.role);
			}
		}

		if (updates.status !== undefined) {
			if (updates.status === "ALL") {
				nextParams.delete("status");
			} else {
				nextParams.set("status", updates.status);
			}
		}

		setSearchParams(nextParams);
	}

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			role: role === "ALL" ? undefined : role,
			status: status === "ALL" ? undefined : status,
			sortBy: "createdAt",
			direction: "desc" as const,
		}),
		[page, pageSize, role, status],
	);

	const {
		data,
		isLoading,
		isFetching,
		isError,
		error,
		refetch,
	} = useAdminUsers(queryParams);

	const users = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const activeFiltersCount = [role !== "ALL", status !== "ALL"].filter(Boolean).length;

	const columns = useMemo<Column<AdminUserListResponse>[]>(
		() => [
			{
				key: "user",
				header: "Tài khoản",
				render: (user) => <UserIdentity user={user} />,
				className: "min-w-[260px]",
			},
			{
				key: "role",
				header: "Vai trò",
				render: (user) => <RoleBadge role={user.role} />,
				className: "whitespace-nowrap",
			},
			{
				key: "status",
				header: "Trạng thái",
				render: (user) => <StatusBadge status={user.status} />,
				className: "whitespace-nowrap",
			},
			{
				key: "createdAt",
				header: "Ngày tạo",
				render: (user) => (
					<span className='whitespace-nowrap text-muted-foreground'>
						{formatDateTime(user.createdAt)}
					</span>
				),
				className: "whitespace-nowrap",
			},
		],
		[],
	);

	function handleRoleChange(nextRole: string) {
		updateSearchParams({
			role: nextRole as RoleFilter,
			page: DEFAULT_PAGE,
		});
	}

	function handleStatusChange(nextStatus: string) {
		updateSearchParams({
			status: nextStatus as StatusFilter,
			page: DEFAULT_PAGE,
		});
	}

	function handlePageChange(nextPage: number) {
		updateSearchParams({ page: nextPage });
	}

	function handlePageSizeChange(nextSize: number) {
		updateSearchParams({
			size: nextSize,
			page: DEFAULT_PAGE,
		});
	}

	function handleResetFilters() {
		updateSearchParams({
			role: "ALL",
			status: "ALL",
			page: DEFAULT_PAGE,
		});
	}

	return (
		<div className='space-y-6'>
			<Card className='border-none bg-gradient-to-r from-primary/10 via-background to-background shadow-sm'>
				<CardHeader className='gap-3'>
					<div className='flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'>
						<div>
							<div className='mb-3 flex size-12 items-center justify-center rounded-xl bg-primary/10 text-primary'>
								<Users className='size-6' />
							</div>

							<CardTitle className='text-2xl font-semibold'>
								Quản lý tài khoản
							</CardTitle>
							<CardDescription className='mt-1 max-w-2xl'>
								Xem danh sách tài khoản trong hệ thống, lọc theo vai trò và trạng thái hoạt động.
							</CardDescription>
						</div>

						<Button
							variant='outline'
							onClick={() => refetch()}
							disabled={isFetching}
						>
							<RefreshCcw className={isFetching ? "animate-spin" : ""} />
							Tải lại
						</Button>
					</div>
				</CardHeader>
			</Card>

			<div className='grid gap-4 md:grid-cols-3'>
				<Card>
					<CardContent className='flex items-center gap-4 pt-6'>
						<div className='flex size-11 items-center justify-center rounded-lg bg-primary/10 text-primary'>
							<Users className='size-5' />
						</div>
						<div>
							<p className='text-sm text-muted-foreground'>Tổng tài khoản</p>
							<p className='text-2xl font-semibold'>{totalElements}</p>
						</div>
					</CardContent>
				</Card>

				<Card>
					<CardContent className='flex items-center gap-4 pt-6'>
						<div className='flex size-11 items-center justify-center rounded-lg bg-emerald-500/10 text-emerald-700'>
							<UserCheck className='size-5' />
						</div>
						<div>
							<p className='text-sm text-muted-foreground'>Bộ lọc vai trò</p>
							<p className='text-lg font-semibold'>
								{ROLE_OPTIONS.find((option) => option.value === role)?.label}
							</p>
						</div>
					</CardContent>
				</Card>

				<Card>
					<CardContent className='flex items-center gap-4 pt-6'>
						<div className='flex size-11 items-center justify-center rounded-lg bg-amber-500/10 text-amber-700'>
							{status === "INACTIVE" ? (
								<UserX className='size-5' />
							) : (
								<ShieldCheck className='size-5' />
							)}
						</div>
						<div>
							<p className='text-sm text-muted-foreground'>Bộ lọc trạng thái</p>
							<p className='text-lg font-semibold'>
								{STATUS_OPTIONS.find((option) => option.value === status)?.label}
							</p>
						</div>
					</CardContent>
				</Card>
			</div>

			<Card>
				<CardHeader>
					<div className='flex flex-col gap-4 lg:flex-row lg:items-center lg:justify-between'>
						<div>
							<CardTitle>Danh sách tài khoản</CardTitle>
							<CardDescription>
								Dữ liệu tự tải lại khi thay đổi bộ lọc hoặc chuyển trang.
							</CardDescription>
						</div>

						<div className='flex flex-col gap-3 sm:flex-row sm:items-center'>
							<Select
								value={role}
								onValueChange={handleRoleChange}
							>
								<SelectTrigger className='w-full sm:w-48'>
									<SelectValue placeholder='Lọc vai trò' />
								</SelectTrigger>
								<SelectContent>
									{ROLE_OPTIONS.map((option) => (
										<SelectItem
											key={option.value}
											value={option.value}
										>
											{option.label}
										</SelectItem>
									))}
								</SelectContent>
							</Select>

							<Select
								value={status}
								onValueChange={handleStatusChange}
							>
								<SelectTrigger className='w-full sm:w-48'>
									<SelectValue placeholder='Lọc trạng thái' />
								</SelectTrigger>
								<SelectContent>
									{STATUS_OPTIONS.map((option) => (
										<SelectItem
											key={option.value}
											value={option.value}
										>
											{option.label}
										</SelectItem>
									))}
								</SelectContent>
							</Select>

							<Button
								variant='outline'
								onClick={handleResetFilters}
								disabled={activeFiltersCount === 0}
							>
								Xóa lọc
							</Button>
						</div>
					</div>
				</CardHeader>

				<CardContent>
					<DataTable
						columns={columns}
						data={users}
						isLoading={isLoading}
						isError={isError}
						error={error}
						onRetry={refetch}
						pageResponse={data}
						pageable={{
							page,
							pageSize,
							isFetching,
							onPageChange: handlePageChange,
							onPageSizeChange: handlePageSizeChange,
							label: "tài khoản",
						}}
						emptyState={{
							icon: UserCog,
							title: "Không có tài khoản phù hợp",
							subtitle: "Thử đổi bộ lọc vai trò hoặc trạng thái để xem thêm dữ liệu.",
						}}
						minWidth='min-w-[760px]'
					/>
				</CardContent>
			</Card>
		</div>
	);
}