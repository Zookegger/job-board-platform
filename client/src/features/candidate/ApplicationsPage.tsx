import { DataTable } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { ApplicationStatusBadge } from "@/components/shared/ApplicationTimeline";
import { useMyApplications } from "@/hooks/useApplications";
import type { ApplicationListResponse, ApplicationStatus } from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import RouterRoutes from "@/utils/RouterRoutes";
import { Building2, ExternalLink, Eye, FileText, MapPin, RefreshCw } from "lucide-react";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";

const DEFAULT_PAGE_SIZE = 10;

type StatusFilter = "all" | ApplicationStatus;

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
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
	const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			status: statusFilter === "all" ? undefined : statusFilter,
		}),
		[page, pageSize, statusFilter],
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

				<div className='flex flex-col gap-3 sm:flex-row sm:items-center'>
					<div className='relative'>
						<select
							value={statusFilter}
							onChange={(event) => {
								setStatusFilter(event.target.value as StatusFilter);
								setPage(0);
							}}
							className='h-10 w-full appearance-none rounded-lg border border-input bg-background pl-4 pr-10 text-sm font-medium shadow-sm outline-none transition-all hover:bg-accent hover:text-accent-foreground focus:border-primary focus:ring-2 focus:ring-primary/20 sm:w-[180px]'
						>
							<option value='all'>Tất cả trạng thái</option>
							<option value='PENDING'>Chờ xử lý</option>
							<option value='REVIEWING'>Đang xem xét</option>
							<option value='INTERVIEW'>Phỏng vấn</option>
							<option value='HIRED'>Đã tuyển</option>
							<option value='REJECTED'>Từ chối</option>
							<option value='WITHDRAWN'>Đã rút đơn</option>
						</select>
						<div className='pointer-events-none absolute inset-y-0 right-0 flex items-center px-3 text-muted-foreground'>
							<svg
								className='size-4'
								fill='none'
								stroke='currentColor'
								viewBox='0 0 24 24'
							>
								<path
									strokeLinecap='round'
									strokeLinejoin='round'
									strokeWidth='2'
									d='M19 9l-7 7-7-7'
								/>
							</svg>
						</div>
					</div>

					<Button
						variant='outline'
						size='icon'
						onClick={() => refetch()}
						disabled={isFetching}
						className='h-10 w-10 shrink-0 rounded-lg shadow-sm'
						title='Làm mới dữ liệu'
					>
						<RefreshCw className={`size-4 ${isFetching ? "animate-spin text-primary" : ""}`} />
					</Button>
				</div>
			</div>

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
						{
							key: "actions",
							header: "Thao tác",
							className: "align-middle text-right",
							render: (application) => (
								<div className='flex items-center justify-end gap-2'>
									<Link to={RouterRoutes.CANDIDATE_APPLICATION_DETAIL(application.id)}>
										<Button
											variant='ghost'
											size='sm'
											className='h-8 gap-2 px-3 hover:bg-primary/10 hover:text-primary'
										>
											<Eye className='size-4' />
											<span className='hidden sm:inline'>Chi tiết</span>
										</Button>
									</Link>
									<Link to={RouterRoutes.JOB_DETAIL(application.jobSlug)}>
										<Button
											variant='ghost'
											size='sm'
											className='h-8 gap-2 px-3 text-muted-foreground hover:bg-secondary hover:text-secondary-foreground'
										>
											<ExternalLink className='size-4' />
											<span className='hidden sm:inline'>Tin đăng</span>
										</Button>
									</Link>
								</div>
							),
						},
					]}
					data={applications}
					isLoading={isLoading}
					isError={isError}
					error={error}
					onRetry={() => refetch()}
					emptyState={{
						icon: FileText,
						title: "Chưa có dữ liệu",
						subtitle:
							statusFilter === "all"
								? "Khám phá việc làm phù hợp và bắt đầu ứng tuyển ngay."
								: "Không tìm thấy hồ sơ nào khớp với bộ lọc hiện tại.",
					}}
					pageResponse={data}
					pageable={{
						page,
						pageSize,
						totalPages,
						totalElements,
						onPageChange: setPage,
						onPageSizeChange: (newSize) => {
							setPageSize(newSize);
							setPage(0);
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
