import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable, type DataTableActions } from "@/components/shared/DataTable";
import { FilterToolbar } from "@/components/shared/FilterToolbar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Sheet, SheetContent, SheetDescription, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { useDebounce } from "@/hooks/useDebounce";
import { useDeleteEmployerJob, useEmployerJobs, useSubmitForReview } from "@/hooks/useEmployerJobs";
import type { JobListResponse, JobStatus } from "@/types/job";
import { EMPLOYMENT_TYPE_LABELS, EXPERIENCE_LEVEL_LABELS, JOB_STATUS_LABELS, LOCATION_TYPES_LABELS } from "@/types/job";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";
import { formatSalary } from "@/utils/StringUtil";
import { Briefcase, Eye, Plus, SendHorizonal, Trash2 } from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
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
	const [searchParams, setSearchParams] = useSearchParams();

	const page = parseInt(searchParams.get("page") || "0", 10);
	const pageSize = parseInt(searchParams.get("size") || String(DEFAULT_PAGE_SIZE), 10);
	const statusParam = searchParams.get("status");
	const keywordParam = searchParams.get("keyword") || null;

	const [searchTerm, setSearchTerm] = useState(keywordParam || "");
	const debouncedSearch = useDebounce(searchTerm.trim(), 400);

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

	const handleStatusFilterChange = useCallback((filter: JobStatus | "ALL") => {
		updateSearchParams({ status: filter === "ALL" ? null : filter, page: "0" });
	}, [updateSearchParams]);

	const handleResetFilters = useCallback(() => {
		setSearchTerm("");
		updateSearchParams({ keyword: null, status: null, page: null });
	}, [updateSearchParams]);

	useEffect(() => {
		if (debouncedSearch !== (keywordParam || "")) {
			updateSearchParams({ keyword: debouncedSearch || null, page: "0" });
		}
	}, [debouncedSearch, keywordParam, updateSearchParams]);

	const hasActiveFilters = keywordParam !== null || statusParam !== null;

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			keyword: debouncedSearch || undefined,
			status: statusParam as JobStatus | undefined,
		}),
		[page, pageSize, debouncedSearch, statusParam],
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
					icon: SendHorizonal,
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
			</div>

				<FilterToolbar
					searchValue={searchTerm}
					onSearchChange={setSearchTerm}
					searchPlaceholder='Tìm theo tiêu đề việc làm...'
					resetDisabled={!hasActiveFilters}
					onReset={handleResetFilters}
					onRefetch={() => refetch()}
					isFetching={isFetching}
					selects={[
						{
							key: "status-filter",
							value: statusParam || "ALL",
							onValueChange: (val) => handleStatusFilterChange(val as JobStatus | "ALL"),
							placeholder: "Tất cả trạng thái",
							options: [
								{ value: "ALL", label: "Tất cả trạng thái" },
								{ value: "DRAFT", label: "Bản nháp" },
								{ value: "PENDING_APPROVAL", label: "Chờ duyệt" },
								{ value: "ACTIVE", label: "Đã đăng" },
								{ value: "EXPIRED", label: "Hết hạn" },
								{ value: "REJECTED", label: "Bị từ chối" },
							],
						},
					]}
				/>

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
						statusParam !== null || debouncedSearch
							? "Thay đổi bộ lọc hoặc tìm kiếm để xem kết quả khác."
							: "Bắt đầu bằng cách tạo tin tuyển dụng mới.",
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
