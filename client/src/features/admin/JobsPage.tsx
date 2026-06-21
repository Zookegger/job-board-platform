import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Separator } from "@/components/ui/separator";
import { useApproveJob, usePendingJobs, useRejectJob } from "@/hooks/useAdminJobs";
import type { AdminPendingJobResponse } from "@/types/job";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";
import { formatSalary } from "@/utils/StringUtil";
import { Briefcase, Building2, CheckCircle2, Eye, MapPin, RefreshCw, XCircle } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";

const DEFAULT_PAGE_SIZE = 10;

const EMPLOYMENT_TYPE_LABELS: Record<string, string> = {
	FULL_TIME: "Toàn thời gian",
	PART_TIME: "Bán thời gian",
	CONTRACT: "Hợp đồng",
	INTERNSHIP: "Thực tập",
};

const EXPERIENCE_LEVEL_LABELS: Record<string, string> = {
	INTERN: "Thực tập",
	JUNIOR: "Junior",
	MID: "Mid-level",
	SENIOR: "Senior",
	LEAD: "Lead",
};

export default function AdminJobsPage() {
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);

	const [detailDialogOpen, setDetailDialogOpen] = useState(false);
	const [selectedJobToView, setSelectedJobToView] = useState<AdminPendingJobResponse | null>(null);

	const [approveDialog, setApproveDialog] = useState<{
		open: boolean;
		job: AdminPendingJobResponse | null;
	}>({ open: false, job: null });

	const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
	const [selectedJobToReject, setSelectedJobToReject] = useState<AdminPendingJobResponse | null>(null);
	const [rejectReason, setRejectReason] = useState("");

	const { data, isError, isFetching, isLoading, refetch, error } = usePendingJobs(page, pageSize);
	const approveJob = useApproveJob();
	const rejectJob = useRejectJob();

	const jobs = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	const actionPending = approveJob.isPending || rejectJob.isPending;

	const handleApprove = (job: AdminPendingJobResponse) => {
		setApproveDialog({ open: true, job });
	};

	const openDetailDialog = (job: AdminPendingJobResponse) => {
		setSelectedJobToView(job);
		setDetailDialogOpen(true);
	};

	const confirmApprove = () => {
		if (!approveDialog.job) return;
		approveJob.mutate(approveDialog.job.id, {
			onSuccess: () => {
				toast.success(`Đã duyệt tin "${approveDialog.job!.title}"`);
				setApproveDialog({ open: false, job: null });
			},
			onError: (err) => toast.error(getErrorMessage(err, "Không thể duyệt tin")),
		});
	};

	const openRejectDialog = (job: AdminPendingJobResponse) => {
		setSelectedJobToReject(job);
		setRejectReason("");
		setRejectDialogOpen(true);
	};

	const handleReject = () => {
		if (!selectedJobToReject) return;
		if (!rejectReason.trim()) {
			toast.error("Vui lòng nhập lý do từ chối");
			return;
		}
		rejectJob.mutate(
			{ jobId: selectedJobToReject.id, reason: rejectReason.trim() },
			{
				onSuccess: () => {
					toast.success("Đã từ chối tin tuyển dụng");
					setRejectDialogOpen(false);
				},
				onError: (err) => toast.error(getErrorMessage(err, "Không thể từ chối tin")),
			},
		);
	};

	return (
		<div className='mx-auto flex w-full max-w-7xl flex-col gap-5'>
			<div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
				<div>
					<h1 className='text-2xl font-semibold text-foreground'>Tin tuyển dụng chờ phê duyệt</h1>
					<p className='mt-1 text-sm text-muted-foreground'>
						{data?.totalElements.toLocaleString("vi-VN")} tin đang cần ADMIN xem xét
					</p>
				</div>
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

			<DataTable
				columns={[
					{
						key: "job",
						header: "Tin tuyển dụng",
						className: "align-top",
						render: (job) => (
							<div className='flex gap-3'>
								<div className='flex size-11 shrink-0 items-center justify-center overflow-hidden rounded-lg border bg-muted'>
									{job.companyLogoUrl ? (
										<img
											src={job.companyLogoUrl}
											alt={job.companyName ?? ""}
											className='h-full w-full object-cover'
										/>
									) : (
										<Briefcase className='size-5 text-muted-foreground' />
									)}
								</div>
								<div className='min-w-0'>
									<p className='font-medium text-foreground'>{job.title}</p>
									<div className='mt-1 flex items-center gap-1 text-xs text-muted-foreground'>
										<Building2 className='size-3.5' />
										<span className='line-clamp-1'>{job.companyName ?? "Chưa rõ"}</span>
									</div>
									{job.location && (
										<p className='mt-1 text-xs text-muted-foreground'>{job.location}</p>
									)}
								</div>
							</div>
						),
					},
					{
						key: "type",
						header: "Loại hình",
						className: "align-top",
						render: (job) => (
							<>
								<Badge
									variant='outline'
									className='text-xs'
								>
									{EMPLOYMENT_TYPE_LABELS[job.employmentType] ?? job.employmentType}
								</Badge>
								<p className='mt-1 text-xs text-muted-foreground'>
									{EXPERIENCE_LEVEL_LABELS[job.experienceLevel] ?? job.experienceLevel}
								</p>
							</>
						),
					},
					{
						key: "salary",
						header: "Mức lương",
						className: "align-top text-sm",
						render: (job) => formatSalary(job.salaryMin, job.salaryMax, job.currency),
					},
					{
						key: "createdAt",
						header: "Ngày gửi",
						className: "align-top text-sm text-muted-foreground",
						render: (job) => formatDate(job.createdAt),
					},
					{
						key: "actions",
						header: "Xử lý",
						className: "align-top",
						render: (job) => (
							<div className='flex flex-wrap gap-2'>
								<Button
									variant='outline'
									size='sm'
									onClick={() => openDetailDialog(job)}
								>
									<Eye /> Chi tiết
								</Button>
								<Button
									variant='success'
									size='sm'
									disabled={actionPending}
									onClick={() => handleApprove(job)}
								>
									<CheckCircle2 /> Duyệt
								</Button>
								<Button
									variant='destructive'
									size='sm'
									disabled={actionPending}
									onClick={() => openRejectDialog(job)}
								>
									<XCircle /> Từ chối
								</Button>
							</div>
						),
					},
				]}
				data={jobs}
				isLoading={isLoading}
				isError={isError}
				error={error}
				onRetry={() => refetch()}
				emptyState={{
					icon: Briefcase,
					title: "Không có tin tuyển dụng chờ duyệt",
					subtitle: "Tất cả tin tuyển dụng đã được xử lý.",
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
					label: "tin tuyển dụng",
				}}
				minWidth='min-w-[900px]'
			/>

			<BaseDialog
				isOpen={approveDialog.open}
				onClose={() => setApproveDialog({ open: false, job: null })}
				title='Xác nhận duyệt tin'
				description={`Bạn có chắc muốn duyệt tin "${approveDialog.job?.title}"?`}
				footer={
					<>
						<Button
							variant='success'
							onClick={confirmApprove}
							disabled={approveJob.isPending}
						>
							{approveJob.isPending ? "Đang duyệt..." : "Xác nhận duyệt"}
						</Button>
						<Button
							variant='outline'
							onClick={() => setApproveDialog({ open: false, job: null })}
							disabled={approveJob.isPending}
						>
							Hủy
						</Button>
					</>
				}
			/>

			<Dialog
				open={rejectDialogOpen}
				onOpenChange={(open) => {
					if (!open) {
						setSelectedJobToReject(null);
						setRejectReason("");
					}
					setRejectDialogOpen(open);
				}}
			>
				<DialogContent className='max-w-xl'>
					<DialogHeader>
						<DialogTitle>Từ chối tin tuyển dụng</DialogTitle>
						<p className='text-sm text-muted-foreground'>
							Nhập lý do từ chối cho tin "{selectedJobToReject?.title ?? ""}".
						</p>
					</DialogHeader>
					<div className='space-y-4 px-4'>
						<label className='block text-sm font-medium text-foreground'>Lý do từ chối</label>
						<textarea
							value={rejectReason}
							onChange={(event) => setRejectReason(event.target.value)}
							rows={6}
							placeholder='Nhập lý do từ chối'
							className='w-full rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
						/>
					</div>
					<DialogFooter>
						<Button
							variant='outline'
							onClick={() => setRejectDialogOpen(false)}
							disabled={rejectJob.isPending}
						>
							Hủy
						</Button>
						<Button
							variant='destructive'
							onClick={handleReject}
							disabled={rejectJob.isPending || !rejectReason.trim()}
						>
							{rejectJob.isPending ? "Đang từ chối..." : "Xác nhận từ chối"}
						</Button>
					</DialogFooter>
				</DialogContent>
			</Dialog>

			{/* Job Detail Dialog */}
			<Dialog
				open={detailDialogOpen}
				onOpenChange={(open) => {
					if (!open) setSelectedJobToView(null);
					setDetailDialogOpen(open);
				}}
			>
				<DialogContent className='w-full sm:max-w-2xl p-0 flex flex-col'>
					<DialogHeader className='px-6 pt-6 pb-4 border-b'>
						<DialogTitle className='text-lg'>{selectedJobToView?.title}</DialogTitle>
						{selectedJobToView?.companyName && (
							<div className='flex items-center gap-1.5 text-sm text-muted-foreground mt-1'>
								<Building2 className='size-4' />
								<span>{selectedJobToView.companyName}</span>
							</div>
						)}
					</DialogHeader>

					<div className='flex-1 overflow-y-auto px-6 py-4'>
						{selectedJobToView && (
							<div className='space-y-5'>
								{/* Meta badges */}
								<div className='flex flex-wrap gap-2'>
									<Badge variant='outline'>
										{EMPLOYMENT_TYPE_LABELS[selectedJobToView.employmentType] ??
											selectedJobToView.employmentType}
									</Badge>
									<Badge variant='outline'>
										{EXPERIENCE_LEVEL_LABELS[selectedJobToView.experienceLevel] ??
											selectedJobToView.experienceLevel}
									</Badge>
									{selectedJobToView.categoryName && (
										<Badge variant='secondary'>{selectedJobToView.categoryName}</Badge>
									)}
								</div>

								{/* Location & Salary */}
								<div className='grid grid-cols-2 gap-4 text-sm'>
									<div>
										<p className='text-xs font-medium text-muted-foreground uppercase tracking-wide mb-1'>
											Địa điểm
										</p>
										<div className='flex items-center gap-1.5'>
											<MapPin className='size-4 text-muted-foreground' />
											<span>{selectedJobToView.location ?? "Chưa rõ"}</span>
										</div>
										<span className='text-xs text-muted-foreground'>
											{selectedJobToView.locationTypes}
										</span>
									</div>
									<div>
										<p className='text-xs font-medium text-muted-foreground uppercase tracking-wide mb-1'>
											Mức lương
										</p>
										<span>
											{formatSalary(
												selectedJobToView.salaryMin,
												selectedJobToView.salaryMax,
												selectedJobToView.currency,
											)}
										</span>
									</div>
								</div>

								<div className='grid grid-cols-2 gap-4 text-sm'>
									<div>
										<p className='text-xs font-medium text-muted-foreground uppercase tracking-wide mb-1'>
											Số lượng tuyển
										</p>
										<span>{selectedJobToView.numberOfOpenings} người</span>
									</div>
									<div>
										<p className='text-xs font-medium text-muted-foreground uppercase tracking-wide mb-1'>
											Ngày gửi
										</p>
										<span>{formatDate(selectedJobToView.createdAt)}</span>
									</div>
								</div>

								<Separator />

								{selectedJobToView.description && (
									<div>
										<p className='text-sm font-semibold mb-2'>Mô tả công việc</p>
										<p className='text-sm text-muted-foreground whitespace-pre-wrap'>
											{selectedJobToView.description}
										</p>
									</div>
								)}

								{selectedJobToView.requirements && (
									<div>
										<p className='text-sm font-semibold mb-2'>Yêu cầu ứng viên</p>
										<p className='text-sm text-muted-foreground whitespace-pre-wrap'>
											{selectedJobToView.requirements}
										</p>
									</div>
								)}

								{selectedJobToView.benefits && (
									<div>
										<p className='text-sm font-semibold mb-2'>Quyền lợi</p>
										<p className='text-sm text-muted-foreground whitespace-pre-wrap'>
											{selectedJobToView.benefits}
										</p>
									</div>
								)}
							</div>
						)}
					</div>

					<DialogFooter className='flex flex-row justify-end px-6 py-4 border-t gap-2'>
						<Button
							variant='destructive'
							disabled={actionPending}
							onClick={() => {
								setDetailDialogOpen(false);
								if (selectedJobToView) openRejectDialog(selectedJobToView);
							}}
						>
							<XCircle /> Từ chối
						</Button>
						<Button
							variant='success'
							disabled={actionPending}
							onClick={() => {
								setDetailDialogOpen(false);
								if (selectedJobToView) handleApprove(selectedJobToView);
							}}
						>
							<CheckCircle2 /> Duyệt
						</Button>
					</DialogFooter>
				</DialogContent>
			</Dialog>
		</div>
	);
}
