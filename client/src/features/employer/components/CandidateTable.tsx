import { ApplicationStatusBadge } from "@/components/shared/ApplicationTimeline";
import { DataTable, type DataTableActions } from "@/components/shared/DataTable";
import { FilterToolbar } from "@/components/shared/FilterToolbar";
import { useEmployerApplications } from "@/hooks/useEmployerApplications";
import {
	APPLICATION_STATUS_LABELS,
	type CandidateApplicationListResponse,
	type CandidateApplicationParams,
} from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import { Eye, User, UserCog } from "lucide-react";
import { useCallback, useMemo, useState } from "react";
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

	const handleStatusFilterChange = useCallback((value: string) => {
		setStatusFilter(value);
		setPage(0);
	}, []);

	const handleResetFilters = useCallback(() => {
		setStatusFilter("all");
		setPage(0);
	}, []);

	const hasActiveFilters = statusFilter !== "all";

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
			</div>

			<FilterToolbar
				resetDisabled={!hasActiveFilters}
				onReset={handleResetFilters}
				onRefetch={() => refetch()}
				isFetching={isFetching}
				selects={[
					{
						key: "status-filter",
						value: statusFilter,
						onValueChange: handleStatusFilterChange,
						placeholder: "Tất cả trạng thái",
						options: [
							{ value: "all", label: "Tất cả trạng thái" },
							...Object.entries(APPLICATION_STATUS_LABELS).map(([key, label]) => ({
								value: key,
								label,
							})),
						],
					},
				]}
			/>

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
