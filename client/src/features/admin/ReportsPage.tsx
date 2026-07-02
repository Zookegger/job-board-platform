import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable, type DataTableActions } from "@/components/shared/DataTable";
import { FilterToolbar } from "@/components/shared/FilterToolbar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { useDismissReport, useReports, useResolveReport, useReviewReport } from "@/hooks/useAdminReports";
import { useToast } from "@/providers/ToastProvider";
import { ReportReason, ReportStatus, type ReportResponse } from "@/types/report";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";
import {
	AlertCircle,
	AlertTriangle,
	Ban,
	Briefcase,
	Building2,
	CheckCircle,
	CheckCircle2,
	Clock,
	Flag,
	HelpCircle,
	Info,
	ShieldAlert,
	XCircle,
} from "lucide-react";
import { useCallback, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";

const DEFAULT_PAGE_SIZE = 10;

const STATUS_FILTER_OPTIONS = [
	{ value: "ALL", label: "Tất cả trạng thái" },
	{ value: ReportStatus.PENDING, label: "Chờ xử lý" },
	{ value: ReportStatus.REVIEWED, label: "Đã xem xét" },
	{ value: ReportStatus.DISMISSED, label: "Bác bỏ" },
	{ value: ReportStatus.RESOLVED, label: "Đã giải quyết" },
] as const;

function StatusBadge({ status }: { status: ReportStatus }) {
	const styles: Record<string, string> = {
		PENDING: "border-warning/70 bg-warning/10 text-warning ring-1 ring-warning/20 shadow-sm",
		REVIEWED: "border-blue-500/70 bg-blue-50 text-blue-700 ring-1 ring-blue-500/20 shadow-sm",
		DISMISSED:
			"border-muted-foreground/40 bg-muted/50 text-muted-foreground ring-1 ring-muted-foreground/10 shadow-sm",
		RESOLVED: "border-success/70 bg-success/10 text-success ring-1 ring-success/20 shadow-sm",
	};
	const labels: Record<string, string> = {
		PENDING: "Chờ xử lý",
		REVIEWED: "Đã xem xét",
		DISMISSED: "Bác bỏ",
		RESOLVED: "Đã giải quyết",
	};
	const icons: Record<string, React.ElementType> = {
		PENDING: Clock,
		REVIEWED: CheckCircle2,
		DISMISSED: Ban,
		RESOLVED: CheckCircle,
	};

	const Icon = icons[status];

	return (
		<Badge
			variant='outline'
			className={`font-medium ${styles[status]}`}
		>
			{Icon && <Icon className='mr-1.5 h-3.5 w-3.5' />}
			{labels[status] || status}
		</Badge>
	);
}

function ReasonBadge({ reason }: { reason: string }) {
	const config = REASON_CONFIG[reason] || {
		label: reason,
		icon: AlertCircle,
		colorClass: "text-muted-foreground",
	};
	const Icon = config.icon;
	const badgeStyles: Record<string, string> = {
		SPAM: "border-orange-500/70 bg-orange-50 text-orange-700 ring-1 ring-orange-500/20",
		SCAM: "border-destructive/70 bg-destructive/10 text-destructive ring-1 ring-destructive/20",
		INAPPROPRIATE: "border-amber-500/70 bg-amber-50 text-amber-700 ring-1 ring-amber-500/20",
		OTHER: "border-muted-foreground/40 bg-muted/50 text-muted-foreground ring-1 ring-muted-foreground/10",
	};
	return (
		<Badge
			variant='outline'
			className={`font-medium ${badgeStyles[reason] ?? badgeStyles.OTHER}`}
		>
			<Icon className={`mr-1.5 h-3.5 w-3.5 ${config.colorClass}`} />
			{config.label}
		</Badge>
	);
}

const REASON_FILTER_OPTIONS = [
	{ value: "ALL", label: "Tất cả lý do" },
	{ value: ReportReason.SPAM, label: "Spam" },
	{ value: ReportReason.SCAM, label: "Lừa đảo" },
	{ value: ReportReason.INAPPROPRIATE, label: "Không phù hợp" },
	{ value: ReportReason.OTHER, label: "Khác" },
];

function fallbackText(value: string | null | undefined, fallback = "Chưa cập nhật") {
	return value?.trim() || fallback;
}

const REASON_CONFIG: Record<string, { label: string; icon: React.ElementType; colorClass: string }> = {
	SPAM: { label: "Spam", icon: ShieldAlert, colorClass: "text-orange-500" },
	SCAM: { label: "Lừa đảo", icon: AlertTriangle, colorClass: "text-destructive" },
	INAPPROPRIATE: { label: "Không phù hợp", icon: AlertCircle, colorClass: "text-amber-500" },
	OTHER: { label: "Khác", icon: Info, colorClass: "text-muted-foreground" },
};

export default function AdminReportsPage() {
	const [searchParams, setSearchParams] = useSearchParams();

	const toast = useToast();

	const page = parseInt(searchParams.get("page") || "0", 10);
	const pageSize = parseInt(searchParams.get("size") || String(DEFAULT_PAGE_SIZE), 10);
	const statusParam = searchParams.get("status");
	const reasonParam = searchParams.get("reason");

	const [actionDialog, setActionDialog] = useState<{
		open: boolean;
		report: ReportResponse | null;
		action: "review" | "dismiss" | "resolve" | null;
	}>({ open: false, report: null, action: null });
	const [actionNotes, setActionNotes] = useState("");

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

	const handleReasonFilterChange = useCallback(
		(value: string) => updateSearchParams({ reason: value === "ALL" ? null : value, page: "0" }),
		[updateSearchParams],
	);

const handleResetFilters = useCallback(() => {
	updateSearchParams({ status: null, reason: null, page: null });
}, [updateSearchParams]);

const hasActiveFilters = statusParam !== null || reasonParam !== null;

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			status: statusParam as ReportStatus | undefined,
			reason: reasonParam as ReportReason | undefined,
		}),
		[page, pageSize, statusParam, reasonParam],
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
		review: { title: "Duyệt báo cáo", description: "Xác nhận bạn đã xem xét báo cáo này." },
		dismiss: { title: "Bác bỏ báo cáo", description: "Nhập ghi chú lý do bỏ qua." },
		resolve: { title: "Giải quyết báo cáo", description: "Xác nhận báo cáo đã được giải quyết." },
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

	const tableActions = useMemo<DataTableActions<ReportResponse>[]>(
		() => [
			{
				header: "Thao tác",
				items: [
					{
						label: "Duyệt",
						icon: CheckCircle2,
						variant: "success",
						show: (r) => r.status === "PENDING",
						disabled: () => actionPending,
						onClick: (r) => openActionDialog(r, "review"),
					},
					{
						label: "Giải quyết",
						icon: CheckCircle2,
						variant: "default",
						show: (r) => r.status === "REVIEWED",
						disabled: () => actionPending,
						onClick: (r) => openActionDialog(r, "resolve"),
					},
					{
						label: "Bác bỏ",
						icon: XCircle,
						variant: "destructive",
						show: (r) => r.status === "PENDING" || r.status === "REVIEWED",
						disabled: () => actionPending,
						onClick: (r) => openActionDialog(r, "dismiss"),
					},
				],
			},
		],
		[actionPending, openActionDialog],
	);

	const columns = useMemo(
		() => [
			{
				key: "target",
				header: "Đối tượng",
				className: "align-top min-w-[220px]",
				render: (r: ReportResponse) => (
					<div className='flex items-start gap-3 min-w-0'>
						<div className='mt-0.5 flex h-8 w-8 shrink-0 items-center justify-center rounded-md border bg-muted/30 text-muted-foreground'>
							{r.jobTitle ? (
								<Briefcase className='h-4 w-4' />
							) : r.companyName ? (
								<Building2 className='h-4 w-4' />
							) : (
								<HelpCircle className='h-4 w-4' />
							)}
						</div>
						<div className='min-w-0'>
							<p className='text-[10px] font-semibold uppercase text-muted-foreground'>
								{r.jobTitle ? "Tin tuyển dụng" : r.companyName ? "Công ty" : "Không xác định"}
							</p>
							<p
								className='truncate font-medium text-foreground'
								title={r.jobTitle || r.companyName || ""}
							>
								{fallbackText(r.jobTitle || r.companyName, "—")}
							</p>
						</div>
					</div>
				),
			},
			{
				key: "reason",
				header: "Lý do",
				className: "align-top min-w-[160px]",
				render: (r: ReportResponse) => <ReasonBadge reason={r.reason} />,
			},
			{
				key: "details",
				header: "Chi tiết báo cáo",
				className: "align-top min-w-[200px] max-w-[240px]",
				render: (r: ReportResponse) =>
					r.details ? (
						<p
							className='line-clamp-2 text-xs text-muted-foreground leading-relaxed'
							title={r.details}
						>
							{r.details}
						</p>
					) : (
						<span className='text-muted-foreground/50'>—</span>
					),
			},
			{
				key: "reportedBy",
				header: "Người báo cáo",
				className: "align-top min-w-[140px]",
				render: (r: ReportResponse) => (
					<span className='text-sm font-medium text-foreground'>{fallbackText(r.reportedByName)}</span>
				),
			},
			{
				key: "reviewNotes",
				header: "Ghi chú xử lý",
				className: "align-top min-w-[180px] max-w-[200px]",
				render: (r: ReportResponse) =>
					r.reviewNotes ? (
						<p
							className='line-clamp-2 text-xs text-muted-foreground leading-relaxed'
							title={r.reviewNotes}
						>
							{r.reviewNotes}
						</p>
					) : (
						<span className='text-muted-foreground/50'>—</span>
					),
			},
			{
				key: "status",
				header: "Trạng thái",
				className: "align-top min-w-[140px]",
				render: (r: ReportResponse) => <StatusBadge status={r.status} />,
			},
			{
				key: "createdAt",
				header: "Ngày tạo",
				className: "align-top min-w-[120px] text-sm text-muted-foreground whitespace-nowrap",
				render: (r: ReportResponse) => formatDate(r.createdAt),
			},
		],
		[],
	);

	return (
		<div className='mx-auto flex w-full flex-col gap-6 pb-10'>
			<div className='flex flex-col gap-3 md:flex-row md:items-end md:justify-between'>
				<div>
					<h1 className='text-2xl font-bold tracking-tight text-foreground'>Quản lý báo cáo</h1>
					<p className='mt-1 text-sm text-muted-foreground'>
						Hệ thống ghi nhận tổng cộng{" "}
						<span className='font-semibold text-foreground'>{totalElements.toLocaleString("vi-VN")}</span>{" "}
						báo cáo cần xử lý.
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
						options: STATUS_FILTER_OPTIONS.map((opt) => ({
							value: opt.value,
							label: opt.label,
						})),
					},
					{
						key: "reason-filter",
						value: reasonParam || "ALL",
						onValueChange: handleReasonFilterChange,
						placeholder: "Tất cả lý do",
						options: REASON_FILTER_OPTIONS,
					},
				]}
			/>

			<div className='rounded-lg border bg-card text-card-foreground shadow-sm'>
				<DataTable
					columns={columns}
					actions={tableActions}
					data={reports}
					isLoading={isLoading}
					isError={isError}
					error={error}
					onRetry={() => refetch()}
					emptyState={{
						icon: Flag,
						title: "Không tìm thấy báo cáo nào",
						subtitle: "Hiện tại không có báo cáo nào khớp với bộ lọc của bạn.",
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
						label: "báo cáo",
					}}
					minWidth='min-w-[1040px]'
				/>
			</div>

			<BaseDialog
				isOpen={actionDialog.open}
				onClose={() => setActionDialog({ open: false, report: null, action: null })}
				title={actionDialog.action ? actionLabels[actionDialog.action].title : ""}
				description={actionDialog.action ? actionLabels[actionDialog.action].description : ""}
				children={
					actionDialog.action === "dismiss" ? (
						<div className='pt-2'>
							<textarea
								value={actionNotes}
								onChange={(event) => setActionNotes(event.target.value)}
								rows={4}
								placeholder='Nhập lý do chi tiết vì sao báo cáo này bị bỏ qua...'
								className='w-full resize-none rounded-md border border-input bg-background px-3 py-2 text-sm text-foreground shadow-sm placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-1 focus-visible:ring-ring disabled:cursor-not-allowed disabled:opacity-50'
							/>
						</div>
					) : null
				}
				footer={
					<div className='flex w-full justify-end gap-2 sm:w-auto'>
						<Button
							variant='outline'
							onClick={() => setActionDialog({ open: false, report: null, action: null })}
						>
							Hủy thao tác
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
