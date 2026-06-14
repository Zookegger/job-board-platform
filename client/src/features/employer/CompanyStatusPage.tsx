import { CheckCircle2, Clock, RefreshCw, XCircle } from "lucide-react";

function formatDate(value: string | Date | null | undefined, withTime = false): string {
	if (!value) return "—";
	const date = typeof value === "string" ? new Date(value) : value;
	if (isNaN(date.getTime())) return "—";
	if (withTime) {
		return new Intl.DateTimeFormat("vi-VN", {
			day: "2-digit", month: "2-digit", year: "numeric",
			hour: "2-digit", minute: "2-digit", second: "2-digit", hour12: false,
		}).format(date);
	}
	return new Intl.DateTimeFormat("vi-VN", {
		day: "2-digit", month: "2-digit", year: "numeric",
	}).format(date);
}

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type { ApprovalLogResponse } from "@/api/companyStatus";
import { useCompanyApprovalHistory, useCompanyStatus } from "@/hooks/useCompanyStatus";
import getErrorMessage from "@/utils/getErrorMessage";

// ── StatusBadge ──────────────────────────────────────────────────────────────

type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED" | "SUSPENDED";

const STATUS_CONFIG: Record<
	ApprovalStatus,
	{ label: string; className: string; icon: React.ReactNode }
> = {
	PENDING: {
		label: "Đang chờ duyệt",
		className: "bg-amber-100 text-amber-800 border-amber-300",
		icon: <Clock className='h-3 w-3' />,
	},
	APPROVED: {
		label: "Đã được duyệt",
		className: "bg-green-100 text-green-800 border-green-300",
		icon: <CheckCircle2 className='h-3 w-3' />,
	},
	REJECTED: {
		label: "Bị từ chối",
		className: "bg-red-100 text-red-800 border-red-300",
		icon: <XCircle className='h-3 w-3' />,
	},
	SUSPENDED: {
		label: "Đã bị đình chỉ",
		className: "bg-gray-100 text-gray-700 border-gray-300",
		icon: <XCircle className='h-3 w-3' />,
	},
};

function StatusBadge({ status }: { status: string }) {
	const cfg = STATUS_CONFIG[status as ApprovalStatus] ?? {
		label: status,
		className: "bg-gray-100 text-gray-700",
		icon: null,
	};
	return (
		<Badge variant='outline' className={`inline-flex items-center gap-1 px-3 py-1 text-sm font-medium ${cfg.className}`}>
			{cfg.icon}
			{cfg.label}
		</Badge>
	);
}

// ── ApprovalTimeline ─────────────────────────────────────────────────────────

function ApprovalTimeline({ logs }: { logs: ApprovalLogResponse[] }) {
	if (logs.length === 0) {
		return <p className='py-6 text-center text-sm text-muted-foreground'>Chưa có lịch sử phê duyệt.</p>;
	}

	return (
		<ol className='relative border-l border-gray-200 pl-6'>
			{logs.map((log, i) => (
				<li key={i} className='mb-6 ml-2'>
					<span className='absolute -left-1.5 mt-1.5 h-3 w-3 rounded-full border border-white bg-gray-400' />
					<div className='flex flex-wrap items-center gap-2'>
						<StatusBadge status={log.newStatus} />
						{log.oldStatus && (
							<span className='text-xs text-muted-foreground'>
								← {STATUS_CONFIG[log.oldStatus as ApprovalStatus]?.label ?? log.oldStatus}
							</span>
						)}
					</div>
					{log.note && <p className='mt-1 text-sm text-gray-600'>{log.note}</p>}
					<time className='mt-1 block text-xs text-muted-foreground'>
					{formatDate(log.createdAt, true)}
					</time>
				</li>
			))}
		</ol>
	);
}

// ── CompanyStatusPage ─────────────────────────────────────────────────────────

export default function CompanyStatusPage() {
	const { data, isLoading, isError, error, refetch, dataUpdatedAt } = useCompanyStatus();
	const { data: history = [], isLoading: historyLoading, refetch: refetchHistory } = useCompanyApprovalHistory();

	const handleRefresh = () => {
		refetch();
		refetchHistory();
	};

	const lastUpdated = dataUpdatedAt ? new Date(dataUpdatedAt) : null;

	// ── Loading ──────────────────────────────────────────────────────────────
	if (isLoading) {
		return (
			<div className='mx-auto max-w-2xl space-y-4 p-6'>
				<Skeleton className='h-7 w-48' />
				<Skeleton className='h-5 w-32' />
				<Skeleton className='h-24 w-full' />
				<Skeleton className='h-16 w-full' />
			</div>
		);
	}

	// ── Error ────────────────────────────────────────────────────────────────
	if (isError) {
		return (
			<div className='mx-auto max-w-2xl p-6'>
				<div className='rounded-lg border border-red-200 bg-red-50 p-4 text-red-700'>
					<p className='font-medium'>Không thể tải dữ liệu</p>
					<p className='mt-1 text-sm'>{getErrorMessage(error)}</p>
					<Button variant='outline' size='sm' className='mt-3' onClick={handleRefresh}>
						Thử lại
					</Button>
				</div>
			</div>
		);
	}

	if (!data) return null;

	const alertContent: Record<ApprovalStatus, React.ReactNode> = {
		PENDING: (
			<p className='text-sm text-amber-800'>
				Hồ sơ đang được xét duyệt, thường mất <strong>1–3 ngày làm việc</strong>.
			</p>
		),
		APPROVED: (
			<p className='text-sm text-green-800'>
				Công ty đã được phê duyệt. Bạn có thể <strong>đăng tuyển ngay</strong>.
			</p>
		),
		REJECTED: (
			<p className='text-sm text-red-800'>
				Hồ sơ bị từ chối.{" "}
				{data.reviewNote && (
					<>
						Lý do: <strong>{data.reviewNote}</strong>.{" "}
					</>
				)}
				Vui lòng cập nhật và gửi lại.
			</p>
		),
		SUSPENDED: (
			<p className='text-sm text-gray-700'>
				Tài khoản công ty đã bị đình chỉ. Vui lòng liên hệ quản trị viên để biết thêm chi tiết.
			</p>
		),
	};

	const alertBg: Record<ApprovalStatus, string> = {
		PENDING: "bg-amber-50 border-amber-200",
		APPROVED: "bg-green-50 border-green-200",
		REJECTED: "bg-red-50 border-red-200",
		SUSPENDED: "bg-gray-50 border-gray-200",
	};

	const status = data.approvalStatus as ApprovalStatus;

	return (
		<div className='mx-auto max-w-2xl space-y-6 p-6'>
			{/* Header */}
			<div className='flex items-start justify-between'>
				<div>
					<h1 className='text-2xl font-bold'>{data.name}</h1>
					{data.taxCode && <p className='mt-1 text-sm text-muted-foreground'>MST: {data.taxCode}</p>}
				</div>
				<Button
					variant='outline'
					size='sm'
					onClick={handleRefresh}
					disabled={isLoading || historyLoading}
					className='gap-1.5'
				>
					<RefreshCw className='h-4 w-4' />
					Làm mới
				</Button>
			</div>

			<Tabs defaultValue='status'>
				<TabsList>
					<TabsTrigger value='status'>Trạng thái</TabsTrigger>
					<TabsTrigger value='history'>Lịch sử duyệt</TabsTrigger>
				</TabsList>

				{/* ── Tab: Trạng thái ── */}
				<TabsContent value='status' className='space-y-4'>
					<Card>
						<CardHeader>
							<CardTitle className='text-base'>Trạng thái phê duyệt</CardTitle>
						</CardHeader>
						<CardContent className='space-y-4'>
							<StatusBadge status={status} />

							<div className={`rounded-lg border p-3 ${alertBg[status]}`}>
								{alertContent[status]}
							</div>

							{/* Grid dates */}
							<div className='grid grid-cols-2 gap-4 text-sm'>
								<div>
									<p className='text-muted-foreground'>Ngày gửi hồ sơ</p>
									<p className='font-medium'>{formatDate(data.submittedAt)}</p>
								</div>
								<div>
									<p className='text-muted-foreground'>Ngày duyệt</p>
									<p className='font-medium'>{formatDate(data.reviewedAt)}</p>
								</div>
							</div>
						</CardContent>
					</Card>

					{lastUpdated && (
						<p className='text-right text-xs text-muted-foreground'>
							Cập nhật lần cuối: {formatDate(lastUpdated, true)}
						</p>
					)}
				</TabsContent>

				{/* ── Tab: Lịch sử duyệt ── */}
				<TabsContent value='history'>
					<Card>
						<CardHeader>
							<CardTitle className='text-base'>Lịch sử phê duyệt</CardTitle>
						</CardHeader>
						<CardContent>
							{historyLoading ? (
								<div className='space-y-3'>
									<Skeleton className='h-10 w-full' />
									<Skeleton className='h-10 w-full' />
								</div>
							) : (
								<ApprovalTimeline logs={history} />
							)}
						</CardContent>
					</Card>
				</TabsContent>
			</Tabs>
		</div>
	);
}
