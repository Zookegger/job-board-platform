import { ApplicationStatusBadge } from "@/components/shared/ApplicationTimeline";
import { DataTable, type DataTableActions } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { useEmployerApplications } from "@/hooks/useEmployerApplications";
import {
	APPLICATION_STATUS_LABELS,
	type CandidateApplicationListResponse,
	type CandidateApplicationParams,
} from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import { Eye, RefreshCw, User, UserCog } from "lucide-react";
import { useMemo, useState } from "react";
import { CandidateProfileModal } from "./CandidateProfileModal";
import { UpdateStatusDialog } from "./UpdateStatusDialog";

interface CandidateTableProps {
	jobId?: string;
}

function CandidateAvatar({ application }: { application: CandidateApplicationListResponse }) {
	return (
		<div className='flex size-10 shrink-0 items-center justify-center overflow-hidden rounded-full border bg-muted/50'>
			{application.candidateAvatarUrl ? (
				<img
					src={application.candidateAvatarUrl}
					alt={application.candidateName}
					className='h-full w-full object-cover'
				/>
			) : (
				<User className='size-4 text-muted-foreground/70' />
			)}
		</div>
	);
}

export default function CandidateTable({ jobId }: CandidateTableProps) {
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(10);
	const [statusFilter, setStatusFilter] = useState<string>("all");
	const [selected, setSelected] = useState<CandidateApplicationListResponse | null>(null);
	const [dialogOpen, setDialogOpen] = useState(false);
	const [cvTarget, setCvTarget] = useState<CandidateApplicationListResponse | null>(null);
	const [cvOpen, setCvOpen] = useState(false);

	const params: CandidateApplicationParams = useMemo(
		() => ({
			page,
			size: pageSize,
			jobId,
			status: statusFilter === "all" ? undefined : statusFilter,
		}),
		[page, pageSize, jobId, statusFilter],
	);

	const { data, isLoading, isError, error, isFetching, refetch } = useEmployerApplications(params);

	const applications = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;

	function openDialog(application: CandidateApplicationListResponse) {
		setSelected(application);
		setDialogOpen(true);
	}

	function openCvPreview(application: CandidateApplicationListResponse) {
		setCvTarget(application);
		setCvOpen(true);
	}

	const tableActions: DataTableActions<CandidateApplicationListResponse>[] = [
		{
			header: "Thao tác",
			items: [
				{
					label: "Xem chi tiết ứng viên",
					icon: Eye,
					variant: "ghost",
					onClick: (app) => openCvPreview(app),
				},
				{
					label: "Cập nhật trạng thái",
					icon: UserCog,
					variant: "ghost",
					onClick: (app) => openDialog(app),
				},
			],
		},
	];

	return (
		<div className='flex flex-col gap-4'>
			{/* Toolbar */}
			<div className='flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between'>
				<p className='text-sm text-muted-foreground'>{totalElements.toLocaleString("vi-VN")} ứng viên</p>
				<div className='flex items-center gap-2'>
					<div className='relative'>
						<select
							value={statusFilter}
							onChange={(e) => {
								setStatusFilter(e.target.value);
								setPage(0);
							}}
							className='h-9 appearance-none rounded-lg border border-input bg-background pl-3 pr-8 text-sm shadow-sm outline-none hover:bg-accent focus:border-primary focus:ring-2 focus:ring-primary/20'
						>
							<option value='all'>Tất cả trạng thái</option>
							{Object.entries(APPLICATION_STATUS_LABELS).map(([key, status]) => (
								<option
									key={key}
									value={key}
								>
									{status}
								</option>
							))}
						</select>
						<div className='pointer-events-none absolute inset-y-0 right-0 flex items-center px-2 text-muted-foreground'>
							<svg
								className='size-3.5'
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
						className='h-9 w-9'
						onClick={() => refetch()}
						disabled={isFetching}
						title='Làm mới'
					>
						<RefreshCw className={`size-4 ${isFetching ? "animate-spin text-primary" : ""}`} />
					</Button>
				</div>
			</div>

			{/* Table */}
			<div className='overflow-hidden rounded-xl border bg-card shadow-sm'>
				<DataTable<CandidateApplicationListResponse>
					columns={[
						{
							key: "candidate",
							header: "Ứng viên",
							className: "align-middle",
							render: (app) => (
								<div className='flex items-center gap-3 py-1'>
									<CandidateAvatar application={app} />
									<div className='flex flex-col min-w-0'>
										<p className='truncate text-sm font-semibold text-foreground'>
											{app.candidateName}
										</p>
										<p className='truncate text-xs text-muted-foreground'>{app.candidateEmail}</p>
									</div>
								</div>
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
					actions={tableActions}
					pageable={{
						page,
						pageSize,
						onPageChange: setPage,
						onPageSizeChange: (newSize) => {
							setPageSize(newSize);
							setPage(0);
						},
						isFetching,
					}}
				/>
			</div>

			<UpdateStatusDialog
				application={selected}
				open={dialogOpen}
				onOpenChange={setDialogOpen}
			/>

			<CandidateProfileModal
				candidate={cvTarget ?? undefined}
				resumeUrl={cvTarget?.resumeUrl ?? null}
				open={cvOpen}
				onClose={() => {
					setCvTarget(null);
					setCvOpen(false);
				}}
			/>
		</div>
	);
}
