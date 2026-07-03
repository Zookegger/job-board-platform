import { ApplicationCard } from "@/components/shared/ApplicationCard";
import { FilterToolbar } from "@/components/shared/FilterToolbar";
import { PaginationBar } from "@/components/shared/PaginationBar";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { useMyApplications } from "@/hooks/useApplications";
import type { ApplicationStatus } from "@/types/application";
import getErrorMessage from "@/utils/getErrorMessage";
import RouterRoutes from "@/utils/RouterRoutes";
import { AlertTriangle, FileText, RefreshCw } from "lucide-react";
import { useCallback, useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";

const DEFAULT_PAGE_SIZE = 10;

export default function CandidateApplicationsPage() {
	const [searchParams, setSearchParams] = useSearchParams();

	const page = parseInt(searchParams.get("page") || "0", 10);
	const pageSize = parseInt(searchParams.get("size") || String(DEFAULT_PAGE_SIZE), 10);
	const statusParam = searchParams.get("status");

	const updateSearchParams = useCallback(
		(updates: Record<string, string | null>) => {
			const nextParams = new URLSearchParams(searchParams);
			for (const [key, value] of Object.entries(updates)) {
				if (value !== null) nextParams.set(key, value);
				else nextParams.delete(key);
			}
			setSearchParams(nextParams);
		},
		[searchParams, setSearchParams],
	);

	const handleStatusFilterChange = useCallback(
		(value: string) => {
			updateSearchParams({ status: value === "ALL" ? null : value, page: "0" });
		},
		[updateSearchParams],
	);

	const handleResetFilters = useCallback(() => {
		updateSearchParams({ status: null, page: null });
	}, [updateSearchParams]);

	const hasActiveFilters = statusParam !== null;

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			status: statusParam as ApplicationStatus | undefined,
		}),
		[page, pageSize, statusParam],
	);

	const { data, isError, isFetching, isLoading, refetch, error } = useMyApplications(queryParams);

	const applications = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	return (
		<div className='mx-auto flex w-full max-w-7xl flex-col gap-6 p-4 md:p-6'>
			<div className='flex flex-col gap-4 md:flex-row md:items-end md:justify-between'>
				<div className='flex flex-col gap-1'>
					<h1 className='text-3xl font-bold tracking-tight text-foreground'>Đơn ứng tuyển của tôi</h1>
					<p className='text-sm font-medium text-muted-foreground'>
						{totalElements.toLocaleString("vi-VN")} hồ sơ đã nộp
					</p>
				</div>
			</div>

			<div>
				<FilterToolbar
					resetDisabled={!hasActiveFilters}
					onReset={handleResetFilters}
					onRefetch={() => refetch()}
					isFetching={isFetching}
					selects={[
						{
							key: "status-filter",
							value: statusParam || "ALL",
							onValueChange: handleStatusFilterChange,
							placeholder: "Tất cả trạng thái",
							options: [
								{ value: "ALL", label: "Tất cả trạng thái" },
								{ value: "PENDING", label: "Chờ xử lý" },
								{ value: "REVIEWING", label: "Đang xem xét" },
								{ value: "INTERVIEW", label: "Phỏng vấn" },
								{ value: "HIRED", label: "Đã tuyển" },
								{ value: "REJECTED", label: "Từ chối" },
								{ value: "WITHDRAWN", label: "Đã rút đơn" },
							],
						},
					]}
				/>

				<div className='overflow-hidden rounded-xl'>
					{isLoading ? (
						<div className='grid grid-cols-1 gap-4 p-4 sm:grid-cols-2 xl:grid-cols-3'>
							{Array.from({ length: 6 }).map((_, i) => (
								<div
									key={i}
									className='flex flex-col gap-3 rounded-xl border bg-card p-4 shadow-sm'
								>
									<div className='flex items-start gap-3'>
										<Skeleton className='size-12 shrink-0 rounded-xl' />
										<div className='flex flex-1 flex-col gap-2'>
											<Skeleton className='h-5 w-3/4' />
											<Skeleton className='h-4 w-1/2' />
										</div>
										<Skeleton className='h-6 w-20 rounded-full' />
									</div>
									<Skeleton className='h-4 w-1/3' />
									<div className='flex gap-2 border-t pt-3'>
										<Skeleton className='h-8 w-24 rounded-md' />
										<Skeleton className='h-8 w-24 rounded-md' />
									</div>
								</div>
							))}
						</div>
					) : isError ? (
						<div className='flex flex-col items-center justify-center gap-3 px-4 py-14 text-center'>
							<div className='mx-auto flex size-12 items-center justify-center rounded-lg bg-muted'>
								<AlertTriangle className='size-6 text-destructive' />
							</div>
							<p className='font-medium text-destructive'>
								{getErrorMessage(error, "Không thể tải dữ liệu")}
							</p>
							<Button
								variant='outline'
								className='mt-1'
								onClick={() => refetch()}
							>
								<RefreshCw className='mr-2 size-4' /> Thử lại
							</Button>
						</div>
					) : applications.length === 0 ? (
						<div className='flex flex-col items-center justify-center gap-2 px-4 py-14 text-center'>
							<div className='mx-auto flex size-12 items-center justify-center rounded-lg bg-muted'>
								<FileText className='size-6 text-muted-foreground' />
							</div>
							<p className='font-medium'>Chưa có dữ liệu</p>
							<p className='text-sm text-muted-foreground'>
								{statusParam === null
									? "Khám phá việc làm phù hợp và bắt đầu ứng tuyển ngay."
									: "Không tìm thấy hồ sơ nào khớp với bộ lọc hiện tại."}
							</p>
							{statusParam === null && (
								<Button
									variant='default'
									className='mt-2'
									asChild
								>
									<Link to={RouterRoutes.JOBS}>Khám phá việc làm</Link>
								</Button>
							)}
						</div>
					) : (
						<>
							<div className='flex items-center justify-end px-4 pt-4'>
								<div className='flex items-center gap-2 text-sm text-muted-foreground'>
									<span>Hiển thị</span>
									<select
										value={pageSize}
										onChange={(e) => {
											updateSearchParams({ size: String(e.target.value), page: "0" });
										}}
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
							<div className='grid grid-cols-1 gap-4 px-4 pb-4 sm:grid-cols-2 xl:grid-cols-3'>
								{applications.map((app) => (
									<ApplicationCard
										key={app.id}
										application={app}
									/>
								))}
							</div>
						</>
					)}
				</div>
			</div>
			{!isLoading && !isError && totalPages > 0 && (
				<PaginationBar
					page={page}
					totalPages={totalPages}
					isFetching={isFetching}
					onPageChange={(newPage) => updateSearchParams({ page: String(newPage) })}
				/>
			)}
		</div>
	);
}
