import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable, type DataTableActions } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { useDeleteEmployerJob, useEmployerJobs, useSubmitForReview } from "@/hooks/useEmployerJobs";
import type { JobListResponse, JobStatus } from "@/types/job";
import { EMPLOYMENT_TYPE_LABELS, EXPERIENCE_LEVEL_LABELS, JOB_STATUS_LABELS, LOCATION_TYPES_LABELS } from "@/types/job";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";
import { formatSalary } from "@/utils/StringUtil";
import { Briefcase, Eye, Plus, RefreshCw, Search, SendHorizonal, SendHorizontal, Trash2 } from "lucide-react";
import { useDeferredValue, useMemo, useState } from "react";
import { toast } from "sonner";
import CandidateTable from "./components/CandidateTable";
import JobFormDialog from "./components/JobFormDialog";

const DEFAULT_PAGE_SIZE = 10;

function getStatusBadge(status: JobStatus) {
	const label = JOB_STATUS_LABELS[status];
	switch (status) {
		case "DRAFT":
			return <Badge variant='secondary'>{label}</Badge>;
		case "PENDING_APPROVAL":
			return (
				<Badge
					variant='outline'
					className='border-warning/40 bg-warning/20 text-warning-foreground'
				>
					{label}
				</Badge>
			);
		case "ACTIVE":
			return (
				<Badge
					variant='outline'
					className='border-success/40 bg-success/20 text-success'
				>
					{label}
				</Badge>
			);
		case "EXPIRED":
			return <Badge variant='outline'>{label}</Badge>;
		case "REJECTED":
			return <Badge variant='destructive'>{label}</Badge>;
		default:
			return <Badge variant='outline'>{label}</Badge>;
	}
}

export default function EmployerJobsPage() {
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
	const [searchTerm, setSearchTerm] = useState("");
	const [statusFilter, setStatusFilter] = useState<JobStatus | "ALL">("ALL");
	const deferredSearch = useDeferredValue(searchTerm.trim());

	const [formDialogOpen, setFormDialogOpen] = useState(false);
	const [formDialogMode, setFormDialogMode] = useState<"create" | "detail" | "edit">("create");
	const [selectedJobId, setSelectedJobId] = useState<string | undefined>(undefined);

	const [candidateSheetJob, setCandidateSheetJob] = useState<JobListResponse | null>(null);

	const [submitDialog, setSubmitDialog] = useState<{
		open: boolean;
		job: JobListResponse | null;
	}>({ open: false, job: null });

	const [deleteDialog, setDeleteDialog] = useState<{
		open: boolean;
		job: JobListResponse | null;
	}>({ open: false, job: null });

	function handleSearchChange(keyword: string) {
		setSearchTerm(keyword);
		setPage(0);
	}

	function handleStatusFilterChange(filter: JobStatus | "ALL") {
		setStatusFilter(filter);
		setPage(0);
	}

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			keyword: deferredSearch || undefined,
			status: statusFilter !== "ALL" ? statusFilter : undefined,
		}),
		[page, pageSize, deferredSearch, statusFilter],
	);

	const { data, isError, isFetching, isLoading, refetch, error } = useEmployerJobs(queryParams);
	const submitForReview = useSubmitForReview(submitDialog.job?.id ?? "");
	const deleteJob = useDeleteEmployerJob(deleteDialog.job?.id ?? "");

	const jobs = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	const actionPending = submitForReview.isPending || deleteJob.isPending;

	const openSubmitDialog = (job: JobListResponse) => {
		setSubmitDialog({ open: true, job });
	};

	const confirmSubmit = () => {
		if (!submitDialog.job) return;

		submitForReview.mutate(undefined, {
			onSuccess: () => {
				toast.success(`Đã gửi duyệt tin "${submitDialog.job!.title}"`);
				setSubmitDialog({ open: false, job: null });
			},
			onError: (mutationError) =>
				toast.error(getErrorMessage(mutationError, "Không thể gửi duyệt tin tuyển dụng")),
		});
	};

	const openDeleteDialog = (job: JobListResponse) => {
		setDeleteDialog({ open: true, job });
	};

	const tableActions: DataTableActions<JobListResponse>[] = [
		{
			header: "Thao tác",
			items: [
				{
					label: "Xem chi tiết",
					icon: Eye,
					variant: "outline",
					onClick: (job) => {
						setFormDialogMode("detail");
						setSelectedJobId(job.id);
						setFormDialogOpen(true);
					},
				},
				{
					label: "Gửi duyệt",
					icon: SendHorizontal,
					variant: "primary", // Maps directly to your mobile variant
					show: (job) => job.status === "DRAFT",
					disabled: () => actionPending,
					onClick: (job) => openSubmitDialog(job),
				},
				{
					label: "Xóa",
					icon: Trash2,
					variant: "destructive",
					disabled: () => actionPending,
					onClick: (job) => openDeleteDialog(job),
				},
			],
		},
	];

	const confirmDelete = () => {
		if (!deleteDialog.job) return;

		deleteJob.mutate(undefined, {
			onSuccess: () => {
				toast.success(`Đã xóa tin "${deleteDialog.job!.title}"`);
				setDeleteDialog({ open: false, job: null });
			},
			onError: (mutationError) => toast.error(getErrorMessage(mutationError, "Không thể xóa tin tuyển dụng")),
		});
	};

	return (
		<div className='mx-auto flex w-full max-w-7xl flex-col gap-5'>
			<div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
				<div>
					<h1 className='text-2xl font-semibold text-foreground'>Việc làm của tôi</h1>
					<p className='mt-1 text-sm text-muted-foreground'>
						{totalElements.toLocaleString("vi-VN")} tin tuyển dụng
					</p>
				</div>
				<div className='flex gap-2'>
					<Button
						variant='outline'
						onClick={() => {
							setFormDialogMode("create");
							setSelectedJobId(undefined);
							setFormDialogOpen(true);
						}}
					>
						<Plus /> Tạo tin
					</Button>
					<Button
						variant='outline'
						onClick={() => refetch()}
						disabled={isFetching}
						className='w-fit'
					>
						<RefreshCw className={isFetching ? "animate-spin" : ""} />
						Làm mới
					</Button>
				</div>
			</div>

			<div className='rounded-lg border bg-card p-4'>
				<div className='grid gap-3 md:grid-cols-[minmax(260px,1fr)_180px]'>
					<Input
						value={searchTerm}
						onChange={(event) => handleSearchChange(event.target.value)}
						placeholder='Tìm theo tiêu đề việc làm...'
						startIcon={<Search className='size-4' />}
						className='h-10 bg-background'
					/>
					<select
						value={statusFilter}
						onChange={(event) => handleStatusFilterChange(event.target.value as JobStatus | "ALL")}
						className='h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
					>
						<option value='ALL'>Tất cả trạng thái</option>
						<option value='DRAFT'>Bản nháp</option>
						<option value='PENDING_APPROVAL'>Chờ duyệt</option>
						<option value='ACTIVE'>Đã đăng</option>
						<option value='EXPIRED'>Hết hạn</option>
						<option value='REJECTED'>Bị từ chối</option>
					</select>
				</div>
			</div>

			<DataTable
				columns={[
					{
						key: "title",
						header: "Tiêu đề",
						className: "align-top",
						render: (job) => (
							<div className='min-w-0'>
								<button
									onClick={() => {
										setFormDialogMode("detail");
										setSelectedJobId(job.id);
										setFormDialogOpen(true);
									}}
									className='text-left font-medium text-foreground hover:text-primary hover:underline'
								>
									{job.title}
								</button>
								<div className='mt-2 flex flex-wrap gap-1.5'>
									<Badge variant='secondary'>{EMPLOYMENT_TYPE_LABELS[job.employmentType]}</Badge>
									<Badge variant='outline'>{EXPERIENCE_LEVEL_LABELS[job.experienceLevel]}</Badge>
								</div>
							</div>
						),
					},
					{
						key: "status",
						header: "Trạng thái",
						className: "align-top",
						render: (job) => getStatusBadge(job.status),
					},
					{
						key: "salary",
						header: "Mức lương",
						className: "align-top text-sm text-muted-foreground",
						render: (job) => formatSalary(job.salaryMin, job.salaryMax, job.currency ?? "VND"),
					},
					{
						key: "locationTypes",
						header: "Hình thức",
						className: "align-top text-sm text-muted-foreground",
						render: (job) => LOCATION_TYPES_LABELS[job.locationTypes],
					},
					{
						key: "createdAt",
						header: "Ngày tạo",
						className: "align-top text-sm text-muted-foreground",
						render: (job) => formatDate(job.createdAt),
					},
				]}
				data={jobs}
				isLoading={isLoading}
				isError={isError}
				error={error}
				onRetry={() => refetch()}
				emptyState={{
					icon: Briefcase,
					title: "Chưa có tin tuyển dụng nào",
					subtitle:
						statusFilter !== "ALL" || deferredSearch
							? "Thay đổi bộ lọc hoặc tìm kiếm để xem kết quả khác."
							: "Bắt đầu bằng cách tạo tin tuyển dụng mới.",
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
					label: "tin",
				}}
				minWidth='min-w-[900px]'
				actions={tableActions}
				onRowClick={(job) => {
					setCandidateSheetJob(job);
				}}
				rowTitle="Bấm để xem danh sách ứng tuyển"
			/>

			<BaseDialog
				isOpen={submitDialog.open}
				onClose={() => setSubmitDialog({ open: false, job: null })}
				title='Xác nhận gửi duyệt'
				description={`Bạn có chắc muốn gửi duyệt tin tuyển dụng "${submitDialog.job?.title ?? ""}"?`}
				footer={
					<div className='flex gap-3'>
						<Button
							variant='outline'
							onClick={() => setSubmitDialog({ open: false, job: null })}
						>
							Hủy
						</Button>
						<Button
							variant='primary'
							onClick={confirmSubmit}
							disabled={actionPending}
						>
							<SendHorizonal /> Xác nhận gửi
						</Button>
					</div>
				}
			/>

			<BaseDialog
				isOpen={deleteDialog.open}
				onClose={() => setDeleteDialog({ open: false, job: null })}
				title='Xóa tin tuyển dụng'
				description={`Bạn có chắc muốn xóa tin tuyển dụng "${deleteDialog.job?.title ?? ""}"? Hành động này không thể hoàn tác.`}
				footer={
					<div className='flex gap-3'>
						<Button
							variant='outline'
							onClick={() => setDeleteDialog({ open: false, job: null })}
						>
							Hủy
						</Button>
						<Button
							variant='destructive'
							onClick={confirmDelete}
							disabled={actionPending}
						>
							<Trash2 /> Xác nhận xóa
						</Button>
					</div>
				}
			/>
			{formDialogOpen && (
				<JobFormDialog
					key={selectedJobId ?? "create"}
					open={formDialogOpen}
					onOpenChange={(open) => {
						setFormDialogOpen(open);
						if (!open) {
							setTimeout(() => setSelectedJobId(undefined), 200);
						}
					}}
					mode={formDialogMode}
					jobId={selectedJobId}
					onCreated={(id) => {
						setSelectedJobId(id);
						setFormDialogMode("detail");
					}}
					onEditJob={() => setFormDialogMode("edit")}
					onCancelEdit={() => setFormDialogMode("detail")}
				/>
			)}

			<Sheet
				open={!!candidateSheetJob}
				onOpenChange={(open) => {
					if (!open) setCandidateSheetJob(null);
				}}
			>
				<SheetContent
					side='right'
					className='w-dvw! sm:max-w-3xl! p-0'
				>
					<SheetHeader className='px-6 pt-6 pb-2'>
						<SheetTitle className='text-lg'>Ứng viên — {candidateSheetJob?.title ?? ""}</SheetTitle>
						<SheetDescription>Danh sách ứng viên đã nộp đơn cho tin tuyển dụng này</SheetDescription>
					</SheetHeader>
					<div className='flex-1 overflow-y-auto px-6 pb-6'>
						{candidateSheetJob && <CandidateTable jobId={candidateSheetJob.id} />}
					</div>
				</SheetContent>
			</Sheet>
		</div>
	);
}
