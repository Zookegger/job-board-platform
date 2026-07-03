import { employerDashboardApi } from "@/api/employerDashboard";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useCompanyStatus } from "@/hooks/useCompanyStatus";
import { useEmployerApplications } from "@/hooks/useEmployerApplications";
import { APPLICATION_STATUS_LABELS, type CandidateApplicationListResponse } from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import RouterRoutes from "@/utils/RouterRoutes";
import { useQuery } from "@tanstack/react-query";
import {
	ArrowRight,
	Bell,
	Briefcase,
	Building2,
	Clock,
	Eye,
	FileText,
	Loader2,
	Plus,
	RefreshCcw,
	UserCheck,
	Users,
	XCircle,
} from "lucide-react";
import type { ComponentType } from "react";
import { useState } from "react";
import { Link } from "react-router-dom";
import { CandidateProfileModal } from "./components/CandidateProfileModal";

type MetricCardItem = {
	label: string;
	value: number;
	description: string;
	icon: ComponentType<{ className?: string }>;
	iconClassName: string;
	cardClassName: string;
};

type MetricGroup = {
	title: string;
	description: string;
	metrics: MetricCardItem[];
};

type QuickAction = {
	title: string;
	description: string;
	href: string;
	icon: ComponentType<{ className?: string }>;
	badge?: string;
};

const appIconClassName = "bg-blue-500/10 text-blue-600";
const appCardClassName = "border-blue-100 bg-blue-50/40";

function formatNumber(value: number) {
	return new Intl.NumberFormat("vi-VN").format(value);
}

function getApplicationStatusBadge(status: string) {
	switch (status) {
		case "PENDING":
			return <Badge variant='secondary'>{APPLICATION_STATUS_LABELS[status]}</Badge>;
		case "REVIEWING":
			return (
				<Badge
					variant='outline'
					className='border-blue-400/40 bg-blue-100/60 text-blue-700'
				>
					{APPLICATION_STATUS_LABELS[status]}
				</Badge>
			);
		case "INTERVIEW":
			return (
				<Badge
					variant='outline'
					className='border-amber-400/40 bg-amber-100/60 text-amber-700'
				>
					{APPLICATION_STATUS_LABELS[status]}
				</Badge>
			);
		case "HIRED":
			return (
				<Badge
					variant='outline'
					className='border-success/40 bg-success/20 text-success'
				>
					{APPLICATION_STATUS_LABELS[status]}
				</Badge>
			);
		case "REJECTED":
			return <Badge variant='destructive'>{APPLICATION_STATUS_LABELS[status]}</Badge>;
		case "WITHDRAWN":
			return <Badge variant='outline'>{APPLICATION_STATUS_LABELS[status]}</Badge>;
		default:
			return <Badge variant='secondary'>{status}</Badge>;
	}
}

function MetricCard({ metric }: { metric: MetricCardItem }) {
	const Icon = metric.icon;

	return (
		<Card
			className={`overflow-hidden shadow-sm transition-all hover:-translate-y-0.5 hover:shadow-md ${metric.cardClassName}`}
		>
			<CardContent className='p-5'>
				<div className='flex items-start justify-between gap-4'>
					<div>
						<p className='text-sm font-medium text-muted-foreground'>{metric.label}</p>
						<div className='mt-3 text-3xl font-bold text-foreground'>{formatNumber(metric.value)}</div>
					</div>
					<div className={`rounded-2xl p-3 ${metric.iconClassName}`}>
						<Icon className='size-5' />
					</div>
				</div>
				<p className='mt-4 text-sm text-muted-foreground'>{metric.description}</p>
			</CardContent>
		</Card>
	);
}

function MetricGroupSection({ group }: { group: MetricGroup }) {
	return (
		<section className='space-y-3'>
			<div>
				<h2 className='text-sm font-bold uppercase tracking-wide text-foreground'>{group.title}</h2>
				<p className='mt-1 text-sm text-muted-foreground'>{group.description}</p>
			</div>
			<div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
				{group.metrics.map((metric) => (
					<MetricCard
						key={metric.label}
						metric={metric}
					/>
				))}
			</div>
		</section>
	);
}

function QuickActionCard({ action }: { action: QuickAction }) {
	const Icon = action.icon;

	return (
		<Link to={action.href}>
			<Card className='h-full border-border/70 shadow-sm transition-all hover:-translate-y-0.5 hover:border-primary/40 hover:shadow-md'>
				<CardContent className='flex items-center gap-4 p-4'>
					<div className='rounded-2xl bg-muted p-3 text-muted-foreground'>
						<Icon className='size-5' />
					</div>
					<div className='min-w-0 flex-1'>
						<div className='flex flex-wrap items-center gap-2'>
							<p className='font-semibold text-foreground'>{action.title}</p>
							{action.badge ? (
								<span className='rounded-full bg-destructive/10 px-2 py-0.5 text-xs font-semibold text-destructive'>
									{action.badge}
								</span>
							) : null}
						</div>
						<p className='mt-1 truncate text-sm text-muted-foreground'>{action.description}</p>
					</div>
					<ArrowRight className='size-4 shrink-0 text-muted-foreground' />
				</CardContent>
			</Card>
		</Link>
	);
}

function DashboardSkeleton() {
	return (
		<div className='space-y-8'>
			<Card className='border-border/70 shadow-sm'>
				<CardContent className='p-6'>
					<Skeleton className='h-5 w-36' />
					<Skeleton className='mt-4 h-9 w-72' />
					<Skeleton className='mt-3 h-4 w-full max-w-xl' />
				</CardContent>
			</Card>
			<div className='grid gap-4 lg:grid-cols-3'>
				<Skeleton className='h-28 rounded-xl' />
				<Skeleton className='h-28 rounded-xl' />
				<Skeleton className='h-28 rounded-xl' />
			</div>
			{[1, 2].map((count, index) => (
				<section
					key={index}
					className='space-y-3'
				>
					<Skeleton className='h-4 w-36' />
					<div className='grid gap-4 md:grid-cols-2 xl:grid-cols-4'>
						{Array.from({ length: count === 0 ? 3 : 4 }).map((_, cardIndex) => (
							<Skeleton
								key={cardIndex}
								className='h-40 rounded-xl'
							/>
						))}
					</div>
				</section>
			))}
		</div>
	);
}

function CompanyStatusBanner({ status, reviewNote }: { status: string; reviewNote: string | null }) {
	if (status === "APPROVED") return null;

	const config: Record<
		string,
		{ icon: ComponentType<{ className?: string }>; title: string; message: string; className: string }
	> = {
		PENDING: {
			icon: Clock,
			title: "Công ty đang chờ duyệt",
			message:
				"Công ty của bạn đang được admin xem xét. Bạn có thể tạo tin tuyển dụng nhưng sẽ không được đăng cho đến khi công ty được duyệt.",
			className: "border-warning/40 bg-warning/5",
		},
		REJECTED: {
			icon: XCircle,
			title: "Công ty bị từ chối",
			message: `Công ty của bạn đã bị từ chối duyệt.${reviewNote ? ` Lý do: ${reviewNote}` : ""} Vui lòng cập nhật thông tin công ty và gửi yêu cầu duyệt lại.`,
			className: "border-destructive/40 bg-destructive/5",
		},
		SUSPENDED: {
			icon: XCircle,
			title: "Công ty đã bị tạm ngưng",
			message: `Công ty của bạn đã bị tạm ngưng.${reviewNote ? ` Lý do: ${reviewNote}` : ""} Mọi tin tuyển dụng đang hoạt động sẽ không còn hiển thị.`,
			className: "border-destructive/40 bg-destructive/5",
		},
	};

	const c = config[status];
	if (!c) return null;
	const Icon = c.icon;

	return (
		<Card className={`${c.className} shadow-sm`}>
			<CardContent className='flex items-start gap-4 p-5'>
				<Icon className='mt-0.5 size-5 shrink-0' />
				<div>
					<p className='font-semibold'>{c.title}</p>
					<p className='mt-1 text-sm text-muted-foreground'>{c.message}</p>
				</div>
			</CardContent>
		</Card>
	);
}

function RecentApplicationsSection({
	applications,
	isLoading,
}: {
	applications: CandidateApplicationListResponse[] | undefined;
	isLoading: boolean;
}) {
	const [selectedCandidate, setSelectedCandidate] = useState<CandidateApplicationListResponse | null>(null);

	return (
		<>
			<section className='space-y-3'>
				<div className='flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between'>
					<div>
						<h2 className='text-sm font-bold uppercase tracking-wide text-foreground'>
							Hồ sơ ứng tuyển gần đây
						</h2>
						<p className='mt-1 text-sm text-muted-foreground'>
							5 hồ sơ mới nhất từ các tin tuyển dụng của bạn.
						</p>
					</div>
					<Link to={RouterRoutes.EMPLOYER_JOBS}>
						<Button
							variant='outline'
							size='sm'
							className='gap-2'
						>
							Xem tất cả <ArrowRight className='size-3' />
						</Button>
					</Link>
				</div>

				{isLoading ? (
					<div className='flex items-center justify-center py-12'>
						<Loader2 className='size-6 animate-spin text-muted-foreground' />
					</div>
				) : !applications || applications.length === 0 ? (
					<Card className='border-border/70 shadow-sm'>
						<CardContent className='flex flex-col items-center gap-3 py-12'>
							<FileText className='size-12 text-muted-foreground/40' />
							<p className='text-sm text-muted-foreground'>Chưa có hồ sơ ứng tuyển nào.</p>
							<p className='text-xs text-muted-foreground/60'>
								Khi ứng viên nộp đơn, hồ sơ sẽ xuất hiện ở đây.
							</p>
						</CardContent>
					</Card>
				) : (
					<div className='overflow-hidden rounded-xl border shadow-sm'>
						<table className='w-full'>
							<thead>
								<tr className='border-b bg-muted/50'>
									<th className='px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
										Ứng viên
									</th>
									<th className='px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
										Tin tuyển dụng
									</th>
									<th className='px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
										Trạng thái
									</th>
									<th className='px-4 py-3 text-left text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
										Ngày nộp
									</th>
									<th className='px-4 py-3 text-right text-xs font-semibold uppercase tracking-wider text-muted-foreground'>
										Chi tiết
									</th>
								</tr>
							</thead>
							<tbody className='divide-y'>
								{applications.map((app) => (
									<tr
										key={app.id}
										className='bg-card transition-colors hover:bg-muted/30'
									>
										<td className='px-4 py-3'>
											<div className='flex items-center gap-3'>
												<div className='flex size-8 items-center justify-center rounded-full bg-primary/10 text-xs font-semibold text-primary'>
													{app.candidateName?.charAt(0)?.toUpperCase() || "?"}
												</div>
												<div>
													<p className='text-sm font-medium'>
														{app.candidateName || "Ẩn danh"}
													</p>
													<p className='text-xs text-muted-foreground'>
														{app.candidateEmail}
													</p>
												</div>
											</div>
										</td>
										<td className='px-4 py-3 text-sm'>{app.jobTitle}</td>
										<td className='px-4 py-3'>{getApplicationStatusBadge(app.status)}</td>
										<td className='px-4 py-3 text-sm text-muted-foreground'>
											{formatDate(app.appliedAt)}
										</td>
										<td className='px-4 py-3 text-right'>
											<Button
												variant='ghost'
												size='sm'
												onClick={() => setSelectedCandidate(app)}
											>
												<Eye className='size-4' />
											</Button>
										</td>
									</tr>
								))}
							</tbody>
						</table>
					</div>
				)}

				{selectedCandidate && (
					<CandidateProfileModal
						candidate={selectedCandidate}
						resumeUrl={selectedCandidate.resumeUrl}
						open={!!selectedCandidate}
						onClose={() => setSelectedCandidate(null)}
					/>
				)}
			</section>
		</>
	);
}

export default function EmployerDashboardPage() {
	const {
		data: stats,
		isLoading,
		isError,
		isFetching,
		refetch,
	} = useQuery({
		queryKey: ["employer", "dashboard", "stats"],
		queryFn: employerDashboardApi.getEmployerDashboardStats,
		retry: false,
	});

	const { data: companyStatus, isLoading: companyStatusLoading } = useCompanyStatus();

	const { data: recentApplicationsPage, isLoading: recentLoading } = useEmployerApplications({ page: 0, size: 5 });

	const hasCompany = !!companyStatus;

	const metricGroups: MetricGroup[] = [
		{
			title: "Tổng quan ứng tuyển",
			description: "Phân bổ hồ sơ ứng tuyển theo trạng thái xử lý.",
			metrics: [
				{
					label: "Chờ xử lý",
					value: stats?.pendingApplications ?? 0,
					description: "Hồ sơ đang chờ nhà tuyển dụng xem xét",
					icon: Clock,
					iconClassName: appIconClassName,
					cardClassName: appCardClassName,
				},
				{
					label: "Đang xem xét",
					value: stats?.reviewingApplications ?? 0,
					description: "Hồ sơ đang được đánh giá",
					icon: Eye,
					iconClassName: appIconClassName,
					cardClassName: appCardClassName,
				},
				{
					label: "Phỏng vấn",
					value: stats?.interviewApplications ?? 0,
					description: "Ứng viên đang trong quá trình phỏng vấn",
					icon: Users,
					iconClassName: appIconClassName,
					cardClassName: appCardClassName,
				},
				{
					label: "Đã tuyển",
					value: stats?.hiredApplications ?? 0,
					description: "Ứng viên đã được tuyển dụng thành công",
					icon: UserCheck,
					iconClassName: appIconClassName,
					cardClassName: appCardClassName,
				},
			],
		},
	];

	const quickActions: QuickAction[] = [
		{
			title: "Tạo tin tuyển dụng",
			description: "Đăng tin mới để tìm kiếm ứng viên",
			href: RouterRoutes.EMPLOYER_JOBS,
			icon: Plus,
		},
		{
			title: "Việc làm của tôi",
			description: "Quản lý và theo dõi tin tuyển dụng",
			href: RouterRoutes.EMPLOYER_JOBS,
			icon: Briefcase,
			badge: (stats?.pendingApprovalJobs ?? 0) > 0 ? `${stats?.pendingApprovalJobs} chờ duyệt` : undefined,
		},
		{
			title: "Công ty",
			description: "Cập nhật thông tin doanh nghiệp",
			href: RouterRoutes.EMPLOYER_COMPANY,
			icon: Building2,
		},
		{
			title: "Thông báo",
			description: "Xem các thông báo và cập nhật",
			href: RouterRoutes.NOTIFICATIONS,
			icon: Bell,
		},
	];

	if (isLoading) {
		return <DashboardSkeleton />;
	}

	return (
		<div className='space-y-8'>
			<Card className='overflow-hidden border-primary/10 bg-gradient-to-br from-primary/10 via-background to-muted/40 shadow-sm'>
				<CardContent className='p-6'>
					<div className='flex flex-col gap-6 lg:flex-row lg:items-center lg:justify-between'>
						<div className='max-w-2xl'>
							<div className='inline-flex items-center gap-2 rounded-full bg-primary/10 px-3 py-1 text-sm font-medium text-primary'>
								<Briefcase className='size-4' />
								{companyStatusLoading ? "..." : companyStatus?.name || "Nhà tuyển dụng"}
							</div>

							<h1 className='mt-4 text-3xl font-bold tracking-tight text-foreground'>
								Bảng điều khiển nhà tuyển dụng
							</h1>

							<p className='mt-2 text-sm leading-6 text-muted-foreground'>
								Theo dõi nhanh tình hình tin tuyển dụng, hồ sơ ứng tuyển và các hoạt động gần đây.
							</p>
						</div>

						<div className='grid gap-3 sm:grid-cols-2 lg:min-w-96'>
							<div className='rounded-2xl border bg-background/80 p-4 shadow-sm'>
								<p className='text-sm text-muted-foreground'>Tổng tin tuyển dụng</p>
								<p className='mt-2 text-3xl font-bold text-foreground'>
									{formatNumber(
										(stats?.activeJobs ?? 0) +
											(stats?.draftJobs ?? 0) +
											(stats?.pendingApprovalJobs ?? 0) +
											(stats?.expiredJobs ?? 0) +
											(stats?.rejectedJobs ?? 0),
									)}
								</p>
								<p className='mt-1 text-sm text-muted-foreground'>Đã tạo trên hệ thống</p>
							</div>

							<div className='rounded-2xl border bg-background/80 p-4 shadow-sm'>
								<p className='text-sm text-muted-foreground'>Tổng ứng tuyển</p>
								<p className='mt-2 text-3xl font-bold text-foreground'>
									{formatNumber(stats?.totalApplications ?? 0)}
								</p>
								<p className='mt-1 text-sm text-muted-foreground'>Lượt ứng tuyển đã nhận</p>
							</div>
						</div>
					</div>
				</CardContent>
			</Card>

			{!companyStatusLoading && !hasCompany && (
				<Card className='border-amber-400/40 bg-amber-50/60 shadow-sm'>
					<CardContent className='p-5'>
						<p className='font-semibold text-amber-800'>Chưa có thông tin công ty</p>
						<p className='mt-1 text-sm text-amber-700/80'>
							Vui lòng thiết lập thông tin công ty để bắt đầu đăng tin tuyển dụng.
						</p>
						<Link to={RouterRoutes.EMPLOYER_COMPANY}>
							<Button
								variant='outline'
								size='sm'
								className='mt-3'
							>
								<Building2 className='size-4' />
								Thiết lập công ty
							</Button>
						</Link>
					</CardContent>
				</Card>
			)}

			{companyStatus && (
				<CompanyStatusBanner
					status={companyStatus.approvalStatus}
					reviewNote={companyStatus.reviewNote}
				/>
			)}

			{isError ? (
				<Card className='border-destructive/40 bg-destructive/5 shadow-sm'>
					<CardContent className='flex flex-col gap-4 py-6 text-sm text-destructive sm:flex-row sm:items-center sm:justify-between'>
						<span>Không thể tải dữ liệu thống kê dashboard. Vui lòng thử lại sau.</span>
						<Button
							variant='outline'
							size='sm'
							onClick={() => void refetch()}
							disabled={isFetching}
							className='w-fit'
						>
							<RefreshCcw className='size-4' />
							Thử lại
						</Button>
					</CardContent>
				</Card>
			) : (
				<>
					<div className='grid gap-4 lg:grid-cols-3'>
						<Card className='border-emerald-100 bg-emerald-50/60 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-emerald-700'>Đang hoạt động</p>
								<p className='mt-2 text-3xl font-bold text-emerald-900'>
									{formatNumber(stats?.activeJobs ?? 0)}
								</p>
								<p className='mt-1 text-sm text-emerald-700/80'>
									Tin tuyển dụng đang hiển thị công khai
								</p>
							</CardContent>
						</Card>

						<Card className='border-amber-100 bg-amber-50/60 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-amber-700'>Chờ duyệt</p>
								<p className='mt-2 text-3xl font-bold text-amber-900'>
									{formatNumber(stats?.pendingApprovalJobs ?? 0)}
								</p>
								<p className='mt-1 text-sm text-amber-700/80'>Đang chờ admin kiểm duyệt</p>
							</CardContent>
						</Card>

						<Card className='border-blue-100 bg-blue-50/60 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-blue-700'>Mới trong tuần</p>
								<p className='mt-2 text-3xl font-bold text-blue-900'>
									{formatNumber(stats?.newApplicationsThisWeek ?? 0)}
								</p>
								<p className='mt-1 text-sm text-blue-700/80'>Hồ sơ ứng tuyển trong 7 ngày qua</p>
							</CardContent>
						</Card>
					</div>

					<div className='space-y-7'>
						{metricGroups.map((group) => (
							<MetricGroupSection
								key={group.title}
								group={group}
							/>
						))}
					</div>

					<RecentApplicationsSection
						applications={recentApplicationsPage?.content}
						isLoading={recentLoading}
					/>

					<section className='space-y-3'>
						<div className='flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between'>
							<div>
								<h2 className='text-sm font-bold uppercase tracking-wide text-foreground'>
									Thao tác nhanh
								</h2>
								<p className='mt-1 text-sm text-muted-foreground'>
									Đi nhanh đến các màn hình thường dùng.
								</p>
							</div>
						</div>

						<div className='grid gap-4 sm:grid-cols-2 xl:grid-cols-4'>
							{quickActions.map((action) => (
								<QuickActionCard
									key={action.href}
									action={action}
								/>
							))}
						</div>
					</section>
				</>
			)}
		</div>
	);
}
