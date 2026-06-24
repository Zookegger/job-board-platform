import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useDismissReport, useReports, useResolveReport, useReviewReport } from "@/hooks/useAdminReports";
import type { ReportResponse, ReportStatus } from "@/types/report";
import getErrorMessage from "@/utils/getErrorMessage";
import { CheckCircle2, Flag, RotateCcw, XCircle } from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { toast } from "sonner";

const DEFAULT_PAGE_SIZE = 10;

const STATUS_FILTER_OPTIONS: { value: string; label: string }[] = [
	{ value: "", label: "Tất cả trạng thái" },
	{ value: "PENDING", label: "Chờ xử lý" },
	{ value: "REVIEWED", label: "Đã duyệt" },
	{ value: "DISMISSED", label: "Bác bỏ" },
	{ value: "RESOLVED", label: "Đã giải quyết" },
] as const;

function formatDate(value: string | null) {
	if (!value) return "Chưa có";
	return new Intl.DateTimeFormat("vi-VN", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}

function StatusBadge({ status }: { status: ReportStatus }) {
	const styles: Record<string, string> = {
		PENDING: "border-warning/70 bg-warning/20 text-warning ring-1 ring-warning/20 font-semibold shadow-sm",
		REVIEWED: "border-blue-500/70 bg-blue-50 text-blue-700 ring-1 ring-blue-500/20 font-semibold shadow-sm",
		DISMISSED: "border-muted-foreground/40 bg-muted/80 text-muted-foreground ring-1 ring-muted-foreground/10 font-medium shadow-sm",
		RESOLVED: "border-success/70 bg-success/20 text-success ring-1 ring-success/20 font-semibold shadow-sm",
	};
	const labels: Record<string, string> = {
		PENDING: "Chờ xử lý",
		REVIEWED: "Đã duyệt",
		DISMISSED: "Bác bỏ",
		RESOLVED: "Đã giải quyết",
	};
	return (
		<Badge
			variant='outline'
			className={styles[status]}
		>
			{labels[status] || status}
		</Badge>
	);
}

function fallbackText(value: string | null | undefined, fallback = "Chưa cập nhật") {
	return value?.trim() || fallback;
}

const REASON_LABELS: Record<string, string> = {
	SPAM: "Spam",
	SCAM: "Lừa đảo",
	INAPPROPRIATE: "Nội dung không phù hợp",
	OTHER: "Khác",
};

export default function AdminReportsPage() {
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
	const [statusFilter, setStatusFilter] = useState("");

	const [actionDialog, setActionDialog] = useState<{
		open: boolean;
		report: ReportResponse | null;
		action: "review" | "dismiss" | "resolve" | null;
	}>({ open: false, report: null, action: null });
	const [actionNotes, setActionNotes] = useState("");

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			status: (statusFilter || undefined) as ReportStatus | undefined,
		}),
		[page, pageSize, statusFilter],
	);

	const { data, isError, isFetching, isLoading, refetch, error } = useReports(queryParams);

	const reviewReport = useReviewReport();
	const dismissReport = useDismissReport();
	const resolveReport = useResolveReport();

	const reports = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	const actionPending = reviewReport.isPending || dismissReport.isPending || resolveReport.isPending;

	const openActionDialog = useCallback((report: ReportResponse, action: "review" | "dismiss" | "resolve") => {
		setActionDialog({ open: true, report, action });
		setActionNotes("");
	}, []);

	const confirmAction = () => {
		const report = actionDialog.report;
		if (!report) return;

		const action = actionDialog.action as "review" | "dismiss" | "resolve";

		if (action === "dismiss" && !actionNotes.trim()) {
			toast.error("Vui lòng nhập ghi chú khi bỏ qua báo cáo");
			return;
		}

		const mutation = action === "review" ? reviewReport : action === "dismiss" ? dismissReport : resolveReport;

		const actionLabels: Record<string, string> = {
			review: "duyệt",
			dismiss: "bỏ qua",
			resolve: "giải quyết",
		};

		mutation.mutate(
			{ id: report.id, reviewNotes: actionNotes.trim() || undefined },
			{
				onSuccess: () => {
					toast.success(`Đã ${actionLabels[action]} báo cáo`);
					setActionDialog({ open: false, report: null, action: null });
				},
				onError: (mutationError) =>
					toast.error(getErrorMessage(mutationError, `Không thể ${actionLabels[action]} báo cáo`)),
			},
		);
	};

	const actionLabels: Record<string, { title: string; description: string }> = {
		review: {
			title: "Duyệt báo cáo",
			description: "Xác nhận bạn đã xem xét báo cáo này.",
		},
		dismiss: {
			title: "Bác bỏ báo cáo",
			description: "Nhập ghi chú lý do bỏ qua.",
		},
		resolve: {
			title: "Giải quyết báo cáo",
			description: "Xác nhận báo cáo đã được giải quyết.",
		},
	};
	const actionBtnLabels: Record<string, string> = {
		review: "Xác nhận duyệt",
		dismiss: "Xác nhận bỏ qua",
		resolve: "Xác nhận giải quyết",
	};
	const actionBtnVariants: Record<string, "success" | "destructive" | "default"> = {
		review: "success",
		dismiss: "destructive",
		resolve: "default",
	};

	const columns = useMemo(
		() => [
			{
				key: "target",
				header: "Đối tượng",
				className: "align-top",
				render: (r: ReportResponse) => (
					<div className='min-w-0'>
						{r.jobTitle ? (
							<>
								<p className='text-xs text-muted-foreground'>Tin tuyển dụng</p>
								<p className='font-medium text-foreground'>{r.jobTitle}</p>
							</>
						) : r.companyName ? (
							<>
								<p className='text-xs text-muted-foreground'>Công ty</p>
								<p className='font-medium text-foreground'>{r.companyName}</p>
							</>
						) : (
							<span className='text-muted-foreground'>Không xác định</span>
						)}
					</div>
				),
			},
			{
				key: "reportedBy",
				header: "Người báo cáo",
				className: "align-top",
				render: (r: ReportResponse) => (
					<span className='text-sm text-foreground'>{fallbackText(r.reportedByName)}</span>
				),
			},
			{
				key: "reason",
				header: "Lý do",
				className: "align-top",
				render: (r: ReportResponse) => (
					<div>
						<Badge
							variant='outline'
							className='border-muted-foreground/30 text-muted-foreground'
						>
							{REASON_LABELS[r.reason] || r.reason}
						</Badge>
						{r.details && (
							<p className='mt-2 max-w-64 whitespace-pre-wrap text-xs text-muted-foreground'>
								{r.details}
							</p>
						)}
					</div>
				),
			},
			{
				key: "reviewNotes",
				header: "Ghi chú",
				className: "align-top",
				render: (r: ReportResponse) =>
					r.reviewNotes ? (
						<p className='max-w-48 whitespace-pre-wrap text-xs text-muted-foreground'>{r.reviewNotes}</p>
					) : (
						<span className='text-xs text-muted-foreground'>—</span>
					),
			},
			{
				key: "status",
				header: "Trạng thái",
				className: "align-top",
				render: (r: ReportResponse) => <StatusBadge status={r.status} />,
			},
			{
				key: "createdAt",
				header: "Ngày tạo",
				className: "align-top text-sm text-muted-foreground",
				render: (r: ReportResponse) => formatDate(r.createdAt),
			},
			{
				key: "actions",
				header: "Thao tác",
				className: "align-top",
				render: (r: ReportResponse) => (
					<div className='flex flex-wrap gap-2'>
						{r.status === "PENDING" && (
							<>
								<Button
									variant='success'
									size='sm'
									disabled={actionPending}
									onClick={() => openActionDialog(r, "review")}
								>
									<CheckCircle2 /> Duyệt
								</Button>
								<Button
									variant='destructive'
									size='sm'
									disabled={actionPending}
									onClick={() => openActionDialog(r, "dismiss")}
								>
									<XCircle /> Bác bỏ
								</Button>
							</>
						)}
						{r.status === "REVIEWED" && (
							<>
								<Button
									variant='default'
									size='sm'
									disabled={actionPending}
									onClick={() => openActionDialog(r, "resolve")}
								>
									<CheckCircle2 /> Giải quyết
								</Button>
								<Button
									variant='destructive'
									size='sm'
									disabled={actionPending}
									onClick={() => openActionDialog(r, "dismiss")}
								>
									<XCircle /> Bác bỏ
								</Button>
							</>
						)}
						{r.status !== "PENDING" && r.status !== "REVIEWED" && (
							<span className='text-xs text-muted-foreground'>—</span>
						)}
					</div>
				),
			},
		],
		[actionPending, openActionDialog],
	);

	return (
		<div className='mx-auto flex w-full max-w-7xl flex-col gap-5'>
			<div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
				<div>
					<h1 className='text-2xl font-semibold text-foreground'>Quản lý báo cáo</h1>
					<p className='mt-1 text-sm text-muted-foreground'>
						Tổng số {totalElements.toLocaleString("vi-VN")} báo cáo
					</p>
				</div>
				<Button
					variant='outline'
					onClick={() => refetch()}
					disabled={isFetching}
					className='w-fit'
				>
					<RotateCcw className={isFetching ? "animate-spin" : ""} />
					Làm mới
				</Button>
			</div>

			<div className='rounded-lg border bg-card p-4'>
				<div className='grid gap-3 md:grid-cols-[minmax(260px,1fr)]'>
					<select
						value={statusFilter}
						onChange={(event) => {
							setStatusFilter(event.target.value);
							setPage(0);
						}}
						className='h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
					>
						{STATUS_FILTER_OPTIONS.map((opt) => (
							<option
								key={opt.value}
								value={opt.value}
							>
								{opt.label}
							</option>
						))}
					</select>
				</div>
			</div>

			<DataTable
				columns={columns}
				data={reports}
				isLoading={isLoading}
				isError={isError}
				error={error}
				onRetry={() => refetch()}
				emptyState={{
					icon: Flag,
					title: "Không có báo cáo",
					subtitle: "Thay đổi bộ lọc để xem kết quả khác.",
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
					label: "báo cáo",
				}}
				minWidth='min-w-[1040px]'
			/>

			<BaseDialog
				isOpen={actionDialog.open}
				onClose={() => setActionDialog({ open: false, report: null, action: null })}
				title={actionDialog.action ? actionLabels[actionDialog.action].title : ""}
				description={actionDialog.action ? actionLabels[actionDialog.action].description : ""}
				children={
					actionDialog.action === "dismiss" ? (
						<div className='px-4'>
							<textarea
								value={actionNotes}
								onChange={(event) => setActionNotes(event.target.value)}
								rows={6}
								placeholder='Nhập ghi chú lý do bỏ qua'
								className='w-full resize-none rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
							/>
						</div>
					) : null
				}
				footer={
					<div className='flex justify-end gap-3'>
						<Button
							variant='outline'
							onClick={() => setActionDialog({ open: false, report: null, action: null })}
						>
							Hủy
						</Button>
						<Button
							variant={actionDialog.action ? actionBtnVariants[actionDialog.action] : "default"}
							onClick={confirmAction}
							disabled={actionPending}
						>
							{actionDialog.action ? actionBtnLabels[actionDialog.action] : "Xác nhận"}
						</Button>
					</div>
				}
			/>
		</div>
	);
}
