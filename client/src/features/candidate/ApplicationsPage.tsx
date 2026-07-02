import { DataTable, type DataTableActions } from "@/components/shared/DataTable";
import { FilterToolbar } from "@/components/shared/FilterToolbar";
import { ApplicationStatusBadge } from "@/components/shared/ApplicationTimeline";
import { useMyApplications } from "@/hooks/useApplications";
import type { ApplicationListResponse, ApplicationStatus } from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import RouterRoutes from "@/utils/RouterRoutes";
import { Building2, ExternalLink, Eye, FileText, MapPin } from "lucide-react";
import { useCallback, useMemo } from "react";
import { Link, useSearchParams } from "react-router-dom";

const DEFAULT_PAGE_SIZE = 10;

function CompanyLogo({ application }: { application: ApplicationListResponse }) {
	return (
		<div className='flex size-12 shrink-0 items-center justify-center overflow-hidden rounded-xl border bg-muted/50 shadow-sm'>
			{application.companyLogoUrl ? (
				<img
					src={application.companyLogoUrl}
					alt={application.companyName}
					className='h-full w-full object-cover'
				/>
			) : (
				<Building2 className='size-5 text-muted-foreground/70' />
			)}
		</div>
	);
}

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

	const handleStatusFilterChange = useCallback((value: string) => {
		updateSearchParams({ status: value === "ALL" ? null : value, page: "0" });
	}, [updateSearchParams]);

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

	const tableActions = useMemo<DataTableActions<ApplicationListResponse>[]>(
		() => [
			{
				header: "Thao tác",
				items: [
					{
						label: "Chi tiết",
						icon: Eye,
						variant: "outline",
						onClick: (app) => {
							window.location.href = RouterRoutes.CANDIDATE_APPLICATION_DETAIL(app.id);
						},
					},
					{
						label: "Tin đăng",
						icon: ExternalLink,
						variant: "outline",
						onClick: (app) => {
							window.location.href = RouterRoutes.JOB_DETAIL(app.jobSlug);
						},
					},
				],
			},
		],
		[],
	);

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

			<div className='overflow-hidden rounded-xl border bg-card shadow-sm'>
				<DataTable<ApplicationListResponse>
					columns={[
						{
							key: "job",
							header: "Công việc",
							className: "align-middle",
							render: (application) => (
								<div className='flex items-center gap-4 py-2'>
									<CompanyLogo application={application} />
									<div className='flex min-w-0 flex-col'>
										<Link to={RouterRoutes.JOB_DETAIL(application.jobSlug)}>
											<p className='truncate text-base font-semibold text-foreground transition-colors hover:text-primary cursor-pointer'>
												{application.jobTitle}
											</p>
										</Link>
										<p className='truncate text-sm font-medium text-muted-foreground'>
											{application.companyName}
										</p>
										{application.jobLocation && (
											<div className='mt-1 flex items-center gap-1.5 text-xs text-muted-foreground/80'>
												<MapPin className='size-3.5 shrink-0' />
												<span className='truncate'>{application.jobLocation}</span>
											</div>
										)}
									</div>
								</div>
							),
						},
						{
							key: "appliedAt",
							header: "Ngày nộp",
							className: "align-middle whitespace-nowrap",
							render: (application) => (
								<span className='text-sm font-medium text-muted-foreground'>
									{formatDate(application.appliedAt)}
								</span>
							),
						},
						{
							key: "status",
							header: "Trạng thái",
							className: "align-middle",
							render: (application) => <ApplicationStatusBadge status={application.status} />,
						},
					]}
					actions={tableActions}
					data={applications}
					isLoading={isLoading}
					isError={isError}
					error={error}
					onRetry={() => refetch()}
					emptyState={{
						icon: FileText,
						title: "Chưa có dữ liệu",
						subtitle:
							statusParam === null
								? "Khám phá việc làm phù hợp và bắt đầu ứng tuyển ngay."
								: "Không tìm thấy hồ sơ nào khớp với bộ lọc hiện tại.",
					}}
					pageResponse={data}
					pageable={{
						page,
						pageSize,
						totalPages,
						totalElements,
						onPageChange: (newPage) => updateSearchParams({ page: String(newPage) }),
						onPageSizeChange: (newSize) => {
							updateSearchParams({ size: String(newSize), page: "0" });
						},
						isFetching,
						label: "đơn ứng tuyển",
					}}
					minWidth='min-w-[800px]'
				/>
			</div>
		</div>
	);
}
