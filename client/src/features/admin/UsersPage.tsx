import { DataTable, type Column, type DataTableActions } from "@/components/shared/DataTable";
import { FilterToolbar } from "@/components/shared/FilterToolbar";
import { Badge } from "@/components/ui/badge";
import { Card, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";
import { useAdminUsers } from "@/hooks/useAdminUsers";
import { useDebounce } from "@/hooks/useDebounce";
import { cn } from "@/lib/utils";
import type { AdminUserListResponse, AdminUsersQueryParams } from "@/types/admin";
import { UserRole } from "@/types/auth";
import { formatDate } from "@/utils/DateUtils";
import { Eye, ShieldBan, ShieldCheck, UserCog, Users } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";

const STATUS_OPTIONS: { value: string; label: string }[] = [
	{ value: "ALL", label: "Tất cả trạng thái" },
	{ value: "true", label: "Đang hoạt động" },
	{ value: "false", label: "Đã khóa" },
];

const ROLE_OPTIONS: { value: string; label: string }[] = [
	{ value: "ALL", label: "Tất cả vai trò" },
	{ value: UserRole.ADMIN, label: "Quản trị viên" },
	{ value: UserRole.EMPLOYER, label: "Nhà tuyển dụng" },
	{ value: UserRole.CANDIDATE, label: "Người tìm việc" },
];

const DEFAULT_PAGE = 0;

function RoleBadge({ role }: { role: UserRole }) {
	const styles: Record<UserRole, string> = {
		ADMIN: "border-purple-500/40 bg-purple-500/10 text-purple-700",
		EMPLOYER: "border-blue-500/40 bg-blue-500/10 text-blue-700",
		CANDIDATE: "border-emerald-500/40 bg-emerald-500/10 text-emerald-700",
	};

	return (
		<Badge
			variant='outline'
			className={cn("", styles[role])}
		>
			{role === UserRole.ADMIN
				? "Quản trị viên"
				: role === UserRole.EMPLOYER
					? "Nhà tuyển dụng"
					: "Người tìm việc"}
		</Badge>
	);
}

function StatusBadge({ isActive }: { isActive: boolean }) {
	return (
		<Badge
			variant='outline'
			className='border-emerald-500/40 bg-emerald-500/10 text-emerald-700'
		>
			{isActive ? "Đang hoạt động" : "Đã khóa"}
		</Badge>
	);
}

export default function AdminUsersPage() {
	const [searchParams, setSearchParams] = useSearchParams();

	const page = parseInt(searchParams.get("page") || String(DEFAULT_PAGE), 10);
	const pageSize = parseInt(searchParams.get("size") || "10", 10);
	const roleParam = searchParams.get("role") as UserRole | null;
	const isActiveParam = searchParams.get("isActive") as "true" | "false" | null;
	const keywordParam = searchParams.get("keyword") || null;

	const [localSearchParams, setLocalSearchParams] = useState(keywordParam || "");

	const debouncedSearch = useDebounce(localSearchParams, 400);

	const updateSearchParams = useCallback(
		(updates: AdminUsersQueryParams) => {
			const nextParams = new URLSearchParams(searchParams);

			if (updates.keyword !== undefined) {
				if (updates.keyword !== null) {
					nextParams.set("keyword", updates.keyword);
				} else {
					nextParams.delete("keyword");
				}
			}

			if (updates.page !== undefined) {
				nextParams.set("page", String(updates.page));
			}

			if (updates.size !== undefined) {
				nextParams.set("size", String(updates.size));
			}

			if (updates.role !== undefined) {
				if (updates.role !== null) {
					nextParams.set("role", updates.role);
				} else {
					nextParams.delete("role");
				}
			}

			if (updates.isActive !== undefined) {
				if (updates.isActive !== null) {
					nextParams.set("isActive", updates.isActive ? "true" : "false");
				} else {
					nextParams.delete("isActive");
				}
			}

			setSearchParams(nextParams);
		},
		[searchParams, setSearchParams],
	);

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			role: roleParam,
			isActive: isActiveParam === null ? null : isActiveParam === "true" ? true : false,
			keyword: keywordParam,
			sortBy: "createdAt",
			direction: "desc" as const,
		}),
		[page, pageSize, roleParam, isActiveParam, keywordParam],
	);

	const { data, isLoading, isFetching, isError, error, refetch } = useAdminUsers(queryParams);

	const users = data?.content ?? [];
	
	const columns = useMemo<Column<AdminUserListResponse>[]>(
		() => [
			{
				key: "avatar",
				header: "",
				render: (user) => (
					<div className='flex items-center justify-center'>
						<div className='h-10 w-10 overflow-hidden rounded-full bg-muted'>
							{user.avatarUrl ? (
								<img
									src={user.avatarUrl}
									alt={user.fullName || user.email}
								/>
							) : (
								<span className='text-muted-foreground'>N/A</span>
							)}
						</div>
					</div>
				),
			},
			{
				key: "user",
				header: "Họ tên",
				render: (user) => <span className='font-medium'>{user.fullName || "Chưa có tên hiển thị"}</span>,
				className: "min-w-[180px] whitespace-nowrap text-ellipsis overflow-hidden",
			},
			{
				key: "phone",
				header: "Số điện thoại",
				render: (user) => <span className='font-medium'>{user.phone}</span>,
				className: "min-w-[140px]",
			},
			{
				key: "email",
				header: "Email",
				render: (user) => <span className='whitespace-nowrap text-muted-foreground'>{user.email}</span>,
				className: "whitespace-nowrap",
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
				render: (user) => <StatusBadge isActive={user.isActive} />,
				className: "whitespace-nowrap",
			},
			{
				key: "createdAt",
				header: "Ngày tạo",
				render: (user) => (
					<span className='whitespace-nowrap text-muted-foreground'>{formatDate(user.createdAt)}</span>
				),
				className: "whitespace-nowrap",
			},
			{
				key: "updatedAt",
				header: "Lần cuối cập nhật",
				render: (user) => (
					<span className='whitespace-nowrap text-muted-foreground'>{formatDate(user.updatedAt)}</span>
				),
				className: "whitespace-nowrap",
			},
		],
		[],
	);

	const actions = useMemo<DataTableActions<AdminUserListResponse>[]>(
		() => [
			{
				header: "Thao tác",
				items: [
					{
						label: "Xem chi tiết",
						icon: Eye,
						variant: "outline",
						onClick: (user) => {
							// Hook this up to your detail modal or route
							console.log("Xem chi tiết user:", user.id);
						},
					},
					{
						label: "Khóa tài khoản",
						icon: ShieldBan,
						variant: "destructive",
						show: (user) => user.isActive === true,
						// disabled: () => actionPending,
						onClick: (user) => {
							// Hook this up to your confirmation dialog
							console.log("Khóa user:", user.id);
						},
					},
					{
						label: "Mở khóa",
						icon: ShieldCheck,
						variant: "outline", // Or "primary" if you want it to pop
						show: (user) => user.isActive === false,
						// disabled: () => actionPending,
						onClick: (user) => {
							// Hook this up to your confirmation dialog
							console.log("Mở khóa user:", user.id);
						},
					},
				],
			},
		],
		[], // dependency array: add actionPending or dialog state variables here later
	);

	const handleRoleChange = useCallback(
		(nextRole: UserRole | null) => {
			updateSearchParams({
				role: nextRole,
				page: DEFAULT_PAGE,
			});
		},
		[updateSearchParams],
	);

	const handleStatusChange = useCallback(
		(nextStatus: boolean | null) => {
			updateSearchParams({
				isActive: nextStatus,
				page: DEFAULT_PAGE,
			});
		},
		[updateSearchParams],
	);

	const handlePageChange = useCallback(
		(nextPage: number) => {
			updateSearchParams({ page: nextPage });
		},
		[updateSearchParams],
	);

	const handlePageSizeChange = useCallback(
		(nextSize: number) => {
			updateSearchParams({
				size: nextSize,
			});
		},
		[updateSearchParams],
	);

	const handleResetFilters = useCallback(() => {
		updateSearchParams({
			page: DEFAULT_PAGE,
			role: null,
			isActive: null,
		});
	}, [updateSearchParams]);

	useEffect(() => {
		if (debouncedSearch !== (keywordParam || "")) {
			updateSearchParams({ keyword: debouncedSearch || null, page: DEFAULT_PAGE });
		}
	}, [debouncedSearch, keywordParam, updateSearchParams]);

	const hasActiveFilters = Boolean(roleParam || isActiveParam || keywordParam);

	return (
		<div className='space-y-6'>
			<Card className='border-none bg-linear-to-r from-primary/10 via-background to-background shadow-sm'>
				<CardHeader className='lg:flex-row lg:items-center lg:justify-between'>
					<div className='flex gap-4 items-center'>
						<div className='flex size-12 items-center justify-center rounded-xl bg-primary/10 text-primary'>
							<Users className='size-6' />
						</div>

						<div>
							<CardTitle className='text-2xl font-semibold'>Quản lý tài khoản</CardTitle>
							<CardDescription className='mt-1 max-w-2xl'>
								Xem danh sách tài khoản trong hệ thống, lọc theo vai trò và trạng thái hoạt động.
							</CardDescription>
						</div>
					</div>
				</CardHeader>
			</Card>

			<FilterToolbar
				searchValue={localSearchParams}
				onSearchChange={setLocalSearchParams}
				searchPlaceholder='Tìm kiếm tài khoản theo ID, tên, email, số điện thoại...'
				resetDisabled={!hasActiveFilters}
				onReset={handleResetFilters}
				onRefetch={() => refetch()}
				isFetching={isFetching}
				selects={[
					{
						key: "role-filter",
						value: roleParam ?? "ALL",
						onValueChange: (val) => handleRoleChange(val === "ALL" ? null : val as UserRole),
						placeholder: "Lọc vai trò",
						options: ROLE_OPTIONS,
					},
					{
						key: "status-filter",
						value: isActiveParam ?? "ALL",
						onValueChange: (val) =>
							handleStatusChange(val === "ALL" ? null : val === "true"),
						placeholder: "Lọc trạng thái",
						options: STATUS_OPTIONS,
					},
				]}
			/>
			<DataTable
				columns={columns}
				actions={actions}
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
		</div>
	);
}
