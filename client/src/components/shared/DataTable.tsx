import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";
import type { PageResponse } from "@/types/pagination";
import getErrorMessage from "@/utils/getErrorMessage";
import { ChevronLeft, ChevronRight, MoreHorizontal, RefreshCw } from "lucide-react";
import { useMemo, type ReactNode } from "react";
import { DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger } from "../ui/dropdown-menu";

export interface Column<T> {
	key: string;
	header: string;
	render: (item: T) => ReactNode;
	className?: string;
}

export interface EmptyStateConfig {
	icon: React.ElementType;
	title: string;
	subtitle: string;
}

export interface PaginationConfig {
	page: number;
	pageSize: number;
	totalPages: number;
	totalElements: number;
	isFetching: boolean;
	onPageChange: (page: number) => void;
	onPageSizeChange: (size: number) => void;
	label?: string;
}

export interface PageableConfig {
	page: number;
	pageSize: number;
	totalPages?: number;
	totalElements?: number;
	onPageChange: (page: number) => void;
	onPageSizeChange: (size: number) => void;
	isFetching: boolean;
	label?: string;
}

export interface DataTableProps<T> {
	columns: Column<T>[];
	data: T[];
	isLoading: boolean;
	isError: boolean;
	error?: Error | null;
	onRetry: () => void;
	emptyState: EmptyStateConfig;

	/** Spring-native: pass the full PageResponse + callbacks */
	pageResponse?: PageResponse<T>;
	pageable?: PageableConfig;

	/** Legacy: manual pagination config (kept for backward compat) */
	pagination?: PaginationConfig;

	actions?: DataTableActions<T>[];
	minWidth?: string;
	skeletonRows?: number;

	rowTitle?: string | undefined;
	onRowClick?: (item: T) => void | undefined;
}

export interface DataTableActions<T> {
	header?: string;
	items: {
		label: string;
		icon: React.ElementType;
		onClick: (item: T) => void;
		disabled?: (item: T) => boolean;
		show?: (item: T) => boolean;
		variant?: "default" | "destructive" | "outline" | "ghost" | "link" | "secondary" | "primary";
	}[];
}

function TableSkeleton({ columns }: { columns: number }) {
	return (
		<>
			{Array.from({ length: 5 }).map((_, rowIndex) => (
				<tr
					key={rowIndex}
					className='border-b'
				>
					{Array.from({ length: columns }).map((_, colIndex) => (
						<td
							key={colIndex}
							className='px-4 py-4'
						>
							<Skeleton className='h-5 w-full max-w-32' />
						</td>
					))}
				</tr>
			))}
		</>
	);
}

function ErrorRow({ colSpan, error, onRetry }: { colSpan: number; error?: Error | null; onRetry: () => void }) {
	return (
		<tr>
			<td
				colSpan={colSpan}
				className='px-4 py-12 text-center'
			>
				<p className='font-medium text-destructive'>{getErrorMessage(error, "Không thể tải dữ liệu")}</p>
				<Button
					variant='outline'
					className='mt-3'
					onClick={onRetry}
				>
					<RefreshCw /> Thử lại
				</Button>
			</td>
		</tr>
	);
}

function EmptyRow({
	colSpan,
	icon: Icon,
	title,
	subtitle,
}: {
	colSpan: number;
	icon: React.ElementType;
	title: string;
	subtitle: string;
}) {
	return (
		<tr>
			<td
				colSpan={colSpan}
				className='px-4 py-14 text-center'
			>
				<div className='mx-auto flex size-12 items-center justify-center rounded-lg bg-muted'>
					<Icon className='size-6 text-muted-foreground' />
				</div>
				<p className='mt-3 font-medium'>{title}</p>
				<p className='mt-1 text-sm text-muted-foreground'>{subtitle}</p>
			</td>
		</tr>
	);
}

function PaginationFooter({
	page,
	pageSize,
	totalPages,
	totalElements,
	isFetching,
	onPageChange,
	onPageSizeChange,
	label,
}: PaginationConfig) {
	const currentPageLabel = totalPages > 0 ? page + 1 : 0;

	return (
		<div className='flex flex-col items-center justify-between gap-4 border-t px-4 py-3 sm:flex-row'>
			{/* Trái: Thông tin tổng số & Dropdown số lượng */}
			<div className='flex items-center gap-4 sm:gap-6 lg:gap-8'>
				<div className='text-sm text-muted-foreground'>
					Tổng số: <span className='font-medium text-foreground'>{totalElements}</span> {label || "bản ghi"}
				</div>

				<div className='flex items-center gap-2 text-sm text-muted-foreground'>
					<span>Hiển thị</span>
					<select
						value={pageSize} // FIXED: Don't hardcode this to 10! Pass your state variable here.
						onChange={(e) => onPageSizeChange(Number(e.target.value))}
						className='h-8 w-16 rounded-md border border-input bg-background px-2 text-sm outline-none transition focus:border-ring focus:ring-1 focus:ring-ring'
					>
						<option value={5}>5</option>
						<option value={10}>10</option>
						<option value={20}>20</option>
						<option value={50}>50</option>
						<option value={100}>100</option>
					</select>
					<span>/ trang</span>
				</div>
			</div>

			{/* Phải: Trạng thái trang & Nút điều hướng */}
			<div className='flex items-center gap-4 sm:gap-6'>
				<div className='text-sm text-muted-foreground'>
					Trang <span className='font-medium text-foreground'>{currentPageLabel}</span> / {totalPages}
				</div>

				<div className='flex items-center gap-2'>
					<Button
						variant='outline'
						size='sm'
						className='h-8 w-24'
						disabled={page === 0 || isFetching}
						onClick={() => onPageChange(Math.max(page - 1, 0))}
					>
						<ChevronLeft className='mr-1 h-4 w-4' /> Trước
					</Button>
					<Button
						variant='outline'
						size='sm'
						className='h-8 w-24'
						disabled={totalPages === 0 || page >= totalPages - 1 || isFetching}
						onClick={() => onPageChange(page + 1)}
					>
						Sau <ChevronRight className='ml-1 h-4 w-4' />
					</Button>
				</div>
			</div>
		</div>
	);
}

function ActionColumn<T>({ actions, item }: { actions: DataTableActions<T>; item: T }) {
	const visibleItems = actions.items.filter((action) => !action.show || action.show(item));
	if (visibleItems.length === 0) return null;

	return (
		<>
			<div className='flex flex-wrap gap-2 md:hidden'>
				<DropdownMenu>
					<DropdownMenuTrigger asChild>
						<Button
							variant='outline'
							size='sm'
						>
							<MoreHorizontal />
						</Button>
					</DropdownMenuTrigger>
					<DropdownMenuContent align='end' className="min-w-40">
						{visibleItems.map((act, i) => (
							<DropdownMenuItem
								key={i}
								onClick={() => act.onClick(item)}
								disabled={act.disabled?.(item)}
							>
								{act.icon && <act.icon className='mr-2 size-4' />}
								{act.label}
							</DropdownMenuItem>
						))}
					</DropdownMenuContent>
				</DropdownMenu>
			</div>

			<div className='hidden md:block'>
				{visibleItems.map((act, i) => (
					<Button
						key={i}
						variant={act.variant ?? "outline"}
						size='sm'
						disabled={act.disabled?.(item)}
						onClick={() => act.onClick(item)}
						title={act.label}
					>
						{act.icon && <act.icon className='size-4' />}
					</Button>
				))}
			</div>
		</>
	);
}

export function DataTable<T>({
	columns,
	data,
	isLoading,
	isError,
	error,
	onRetry,
	emptyState,
	pageResponse,
	pageable,
	pagination,
	actions,
	minWidth = "min-w-[640px]",
	skeletonRows = 5,
	onRowClick = undefined,
	rowTitle,
}: DataTableProps<T>) {
	const resolvedPagination: PaginationConfig | undefined = useMemo(() => {
		if (pageResponse && pageable) {
			return {
				page: pageable.page,
				pageSize: pageable.pageSize,
				totalPages: pageResponse.totalPages ?? pageable.totalPages ?? 0,
				totalElements: pageResponse.totalElements ?? pageable.totalElements ?? 0,
				isFetching: pageable.isFetching,
				onPageChange: pageable.onPageChange,
				onPageSizeChange: pageable.onPageSizeChange,
				label: pageable.label,
			};
		}
		return pagination;
	}, [pageResponse, pageable, pagination]);

	const columnsWithActions = useMemo(() => {
		if (!actions || actions.length === 0) return columns;
		return [
			...columns,
			{
				key: "actions",
				header: "Hành động",
				className: "w-[1%] whitespace-nowrap text-right",
				render: (item: T) => (
					<ActionColumn
						actions={actions[0]}
						item={item}
					/>
				),
			},
		];
	}, [columns, actions]);

	return (
		<div className='overflow-hidden rounded-lg border bg-card'>
			<div className='overflow-x-auto'>
				<table className={cn("w-full border-collapse text-left text-sm", minWidth)}>
					<thead className='border-b bg-muted/60 text-xs uppercase text-muted-foreground'>
						<tr>
							{columnsWithActions.map((col) => (
								<th
									key={col.key}
									className={cn("px-4 py-3 font-medium", col.className)}
								>
									{col.header}
								</th>
							))}
						</tr>
					</thead>
					<tbody>
						{isLoading ? (
							<TableSkeleton columns={columnsWithActions.length || skeletonRows} />
						) : isError ? (
							<ErrorRow
								colSpan={columnsWithActions.length}
								error={error}
								onRetry={onRetry}
							/>
						) : data.length === 0 ? (
							<EmptyRow
								colSpan={columnsWithActions.length}
								icon={emptyState.icon}
								title={emptyState.title}
								subtitle={emptyState.subtitle}
							/>
						) : (
							data.map((item, index) => (
								<tr
									key={index}
									className={cn(
										"border-b last:border-0 hover:bg-muted/30 transition-colors",
										typeof onRowClick === "function" && "cursor-pointer",
									)}
									onClick={() => onRowClick?.(item)}
									title={rowTitle}
								>
									{columnsWithActions.map((col) => (
										<td
											key={col.key}
											className={cn("px-4 py-4", col.className)}
											onClick={(e) => {
												if (col.key === "actions") {
													e.stopPropagation();
												}
											}}
										>
											{col.render(item)}
										</td>
									))}
								</tr>
							))
						)}
					</tbody>
				</table>
			</div>
			{resolvedPagination && <PaginationFooter {...resolvedPagination} />}
		</div>
	);
}
