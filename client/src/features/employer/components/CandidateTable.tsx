import { ApplicationStatusBadge } from "@/components/shared/ApplicationTimeline";
import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/shared/DataTable";
import { useEmployerApplications } from "@/hooks/useEmployerApplications";
import type { EmployerApplicationListResponse, EmployerApplicationParams } from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import { Building2, RefreshCw, UserCog } from "lucide-react";
import { useMemo, useState } from "react";
import { UpdateStatusDialog } from "./UpdateStatusDialog";

interface CandidateTableProps {
	jobId?: string;
}

function CandidateAvatar({ application }: { application: EmployerApplicationListResponse }) {
	return (
		<div className='flex size-10 shrink-0 items-center justify-center overflow-hidden rounded-full border bg-muted/50'>
			{application.candidateAvatarUrl ? (
				<img src={application.candidateAvatarUrl} alt={application.candidateName} className='h-full w-full object-cover' />
			) : (
				<Building2 className='size-4 text-muted-foreground/70' />
			)}
		</div>
	);
}

export default function CandidateTable({ jobId }: CandidateTableProps) {
	const [page, setPage] = useState(0);
	const [statusFilter, setStatusFilter] = useState<string>("all");
	const [selected, setSelected] = useState<EmployerApplicationListResponse | null>(null);
	const [dialogOpen, setDialogOpen] = useState(false);

	const params: EmployerApplicationParams = useMemo(
		() => ({
			page,
			jobId,
			status: statusFilter === "all" ? undefined : statusFilter,
		}),
		[page, jobId, statusFilter],
	);

	const { data, isLoading, isError, error, isFetching, refetch } = useEmployerApplications(params);

	const applications = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;

	function openDialog(application: EmployerApplicationListResponse) {
		setSelected(application);
		setDialogOpen(true);
	}

	return (
		<div className='flex flex-col gap-4'>
			{/* Toolbar */}
			<div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
				<p className='text-sm text-muted-foreground'>
					{totalElements.toLocaleString("vi-VN")} ứng viên
				</p>
				<div className='flex items-center gap-2'>
					<div className='relative'>
						<select
							value={statusFilter}
							onChange={(e) => { setStatusFilter(e.target.value); setPage(0); }}
							className='h-9 appearance-none rounded-lg border border-input bg-background pl-3 pr-8 text-sm shadow-sm outline-none hover:bg-accent focus:border-primary focus:ring-2 focus:ring-primary/20'
						>
							<option value='all'>Tất cả trạng thái</option>
							<option value='PENDING'>Chờ xử lý</option>
							<option value='REVIEWING'>Đang xem xét</option>
							<option value='INTERVIEW'>Phỏng vấn</option>
							<option value='HIRED'>Đã tuyển</option>
							<option value='REJECTED'>Từ chối</option>
						</select>
						<div className='pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-muted-foreground'>
							<svg className='size-3.5' fill='none' stroke='currentColor' viewBox='0 0 24 24'>
								<path strokeLinecap='round' strokeLinejoin='round' strokeWidth='2' d='M19 9l-7 7-7-7' />
							</svg>
						</div>
					</div>
					<Button variant='outline' size='icon' className='h-9 w-9' onClick={() => refetch()} disabled={isFetching} title='Làm mới'>
						<RefreshCw className={`size-4 ${isFetching ? "animate-spin text-primary" : ""}`} />
					</Button>
				</div>
			</div>

			{/* Table */}
			<div className='overflow-hidden rounded-xl border bg-card shadow-sm'>
				<DataTable<EmployerApplicationListResponse>
					columns={[
						{
							key: "candidate",
							header: "Ứng viên",
							className: "align-middle",
							render: (app) => (
								<div className='flex items-center gap-3 py-1'>
									<CandidateAvatar application={app} />
									<div className='flex flex-col min-w-0'>
										<p className='truncate text-sm font-semibold text-foreground'>{app.candidateName}</p>
										<p className='truncate text-xs text-muted-foreground'>{app.candidateEmail}</p>
									</div>
								</div>
							),
						},
						{
							key: "job",
							header: "Vị trí",
							className: "align-middle hidden md:table-cell",
							render: (app) => (
								<span className='text-sm text-muted-foreground'>{app.jobTitle}</span>
							),
						},
						{
							key: "appliedAt",
							header: "Ngày nộp",
							className: "align-middle whitespace-nowrap hidden sm:table-cell",
							render: (app) => (
								<span className='text-sm text-muted-foreground'>{formatDate(app.appliedAt)}</span>
							),
						},
						{
							key: "status",
							header: "Trạng thái",
							className: "align-middle",
							render: (app) => <ApplicationStatusBadge status={app.status} />,
						},
						{
							key: "actions",
							header: "Thao tác",
							className: "align-middle text-right",
							render: (app) => (
								<Button
									variant='ghost'
									size='sm'
									className='h-8 gap-1.5 px-3 hover:bg-primary/10 hover:text-primary'
									onClick={() => openDialog(app)}
								>
									<UserCog className='size-4' />
									<span className='hidden sm:inline'>Cập nhật</span>
								</Button>
							),
						},
					]}
					data={applications}
					isLoading={isLoading}
					isError={isError}
					error={error}
					onRetry={() => refetch()}
					emptyState={{
						icon: UserCog,
						title: "Chưa có ứng viên",
						subtitle: "Chưa có đơn ứng tuyển nào khớp với bộ lọc.",
					}}
					pageResponse={data}
					pageable={{
						page,
						pageSize: 10,
						onPageChange: setPage,
						onPageSizeChange: () => {},
						isFetching,
					}}
				/>
			</div>

			<UpdateStatusDialog
				application={selected}
				open={dialogOpen}
				onOpenChange={setDialogOpen}
			/>
		</div>
	);
}
