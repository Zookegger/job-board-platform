import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAdminDashboardStats } from "@/hooks/useAdminDashboard";
import {
	ArrowRight,
	Briefcase,
	Building2,
	CheckCircle2,
	Code2,
	FileText,
	Flag,
	RefreshCcw,
	ShieldCheck,
	Sparkles,
	Users,
} from "lucide-react";
import type { ComponentType } from "react";
import { Link } from "react-router-dom";

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

const userIconClassName = "bg-blue-500/10 text-blue-600";
const companyIconClassName = "bg-amber-500/10 text-amber-600";
const jobIconClassName = "bg-emerald-500/10 text-emerald-600";

const userCardClassName = "border-blue-100 bg-blue-50/40";
const companyCardClassName = "border-amber-100 bg-amber-50/40";
const jobCardClassName = "border-emerald-100 bg-emerald-50/40";

function formatNumber(value: number) {
	return new Intl.NumberFormat("vi-VN").format(value);
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
						<p className='text-sm font-medium text-muted-foreground'>
							{metric.label}
						</p>

						<div className='mt-3 text-3xl font-bold text-foreground'>
							{formatNumber(metric.value)}
						</div>
					</div>

					<div className={`rounded-2xl p-3 ${metric.iconClassName}`}>
						<Icon className='size-5' />
					</div>
				</div>

				<p className='mt-4 text-sm text-muted-foreground'>
					{metric.description}
				</p>
			</CardContent>
		</Card>
	);
}

function MetricGroupSection({ group }: { group: MetricGroup }) {
	return (
		<section className='space-y-3'>
			<div>
				<h2 className='text-sm font-bold uppercase tracking-wide text-foreground'>
					{group.title}
				</h2>
				<p className='mt-1 text-sm text-muted-foreground'>
					{group.description}
				</p>
			</div>

			<div className='grid gap-4 md:grid-cols-2 xl:grid-cols-3'>
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

						<p className='mt-1 truncate text-sm text-muted-foreground'>
							{action.description}
						</p>
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

			{[2, 2, 3].map((count, index) => (
				<section
					key={index}
					className='space-y-3'
				>
					<Skeleton className='h-4 w-36' />
					<div className='grid gap-4 md:grid-cols-2 xl:grid-cols-3'>
						{Array.from({ length: count }).map((_, cardIndex) => (
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

export default function AdminDashboardPage() {
	const {
		data: stats,
		isLoading,
		isError,
		isFetching,
		refetch,
	} = useAdminDashboardStats();

	const totalPending =
		(stats?.pendingCompanies ?? 0) + (stats?.pendingJobs ?? 0);

	const metricGroups: MetricGroup[] = [
		{
			title: "Người dùng",
			description: "Theo dõi quy mô tài khoản và lượng người dùng mới.",
			metrics: [
				{
					label: "Tổng người dùng",
					value: stats?.totalUsers ?? 0,
					description: "Tất cả tài khoản trên hệ thống",
					icon: Users,
					iconClassName: userIconClassName,
					cardClassName: userCardClassName,
				},
				{
					label: "Người dùng mới",
					value: stats?.newUsers ?? 0,
					description: "Trong 7 ngày gần nhất",
					icon: Sparkles,
					iconClassName: userIconClassName,
					cardClassName: userCardClassName,
				},
			],
		},
		{
			title: "Công ty",
			description: "Quản lý doanh nghiệp đăng ký và các hồ sơ cần duyệt.",
			metrics: [
				{
					label: "Tổng công ty",
					value: stats?.totalCompanies ?? 0,
					description: "Công ty đã đăng ký trên nền tảng",
					icon: Building2,
					iconClassName: companyIconClassName,
					cardClassName: companyCardClassName,
				},
				{
					label: "Công ty chờ duyệt",
					value: stats?.pendingCompanies ?? 0,
					description: "Công ty đang chờ admin xử lý",
					icon: ShieldCheck,
					iconClassName: companyIconClassName,
					cardClassName: companyCardClassName,
				},
			],
		},
		{
			title: "Việc làm & Ứng tuyển",
			description: "Theo dõi tin tuyển dụng đã duyệt, tin chờ duyệt và hồ sơ ứng tuyển.",
			metrics: [
				{
					label: "Tin đã duyệt",
					value: stats?.totalJobs ?? 0,
					description: "Tin tuyển dụng đang hiển thị công khai",
					icon: CheckCircle2,
					iconClassName: jobIconClassName,
					cardClassName: jobCardClassName,
				},
				{
					label: "Tin chờ duyệt",
					value: stats?.pendingJobs ?? 0,
					description: "Tin tuyển dụng đang chờ admin duyệt",
					icon: Briefcase,
					iconClassName: jobIconClassName,
					cardClassName: jobCardClassName,
				},
				{
					label: "Tổng hồ sơ ứng tuyển",
					value: stats?.totalApplications ?? 0,
					description: "Tổng số lượt ứng tuyển của ứng viên",
					icon: FileText,
					iconClassName: jobIconClassName,
					cardClassName: jobCardClassName,
				},
			],
		},
	];

	const quickActions: QuickAction[] = [
		{
			title: "Duyệt công ty",
			description: "Xem và xử lý công ty đăng ký",
			href: "/admin/companies",
			icon: Building2,
			badge:
				(stats?.pendingCompanies ?? 0) > 0
					? `${stats?.pendingCompanies} chờ duyệt`
					: undefined,
		},
		{
			title: "Duyệt việc làm",
			description: "Kiểm duyệt tin tuyển dụng",
			href: "/admin/jobs",
			icon: Briefcase,
			badge:
				(stats?.pendingJobs ?? 0) > 0
					? `${stats?.pendingJobs} chờ duyệt`
					: undefined,
		},
		{
			title: "Báo cáo",
			description: "Theo dõi báo cáo vi phạm",
			href: "/admin/reports",
			icon: Flag,
		},
		{
			title: "Kỹ năng",
			description: "Quản lý danh sách kỹ năng",
			href: "/admin/skills",
			icon: Code2,
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
								<ShieldCheck className='size-4' />
								Admin Dashboard
							</div>

							<h1 className='mt-4 text-3xl font-bold tracking-tight text-foreground'>
								Bảng điều khiển quản trị
							</h1>

							<p className='mt-2 text-sm leading-6 text-muted-foreground'>
								Theo dõi nhanh tình hình người dùng, công ty, tin tuyển dụng và
								các mục đang chờ xử lý trên nền tảng.
							</p>
						</div>

						<div className='grid gap-3 sm:grid-cols-2 lg:min-w-96'>
							<div className='rounded-2xl border bg-background/80 p-4 shadow-sm'>
								<p className='text-sm text-muted-foreground'>Cần xử lý</p>
								<p className='mt-2 text-3xl font-bold text-foreground'>
									{formatNumber(totalPending)}
								</p>
								<p className='mt-1 text-sm text-muted-foreground'>
									Công ty và tin tuyển dụng chờ duyệt
								</p>
							</div>

							<div className='rounded-2xl border bg-background/80 p-4 shadow-sm'>
								<p className='text-sm text-muted-foreground'>Tổng ứng tuyển</p>
								<p className='mt-2 text-3xl font-bold text-foreground'>
									{formatNumber(stats?.totalApplications ?? 0)}
								</p>
								<p className='mt-1 text-sm text-muted-foreground'>
									Lượt ứng tuyển trên toàn hệ thống
								</p>
							</div>
						</div>
					</div>
				</CardContent>
			</Card>

			{isError ? (
				<Card className='border-destructive/40 bg-destructive/5 shadow-sm'>
					<CardContent className='flex flex-col gap-4 py-6 text-sm text-destructive sm:flex-row sm:items-center sm:justify-between'>
						<span>
							Không thể tải dữ liệu thống kê dashboard. Vui lòng thử lại sau.
						</span>

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
						<Card className='border-blue-100 bg-blue-50/50 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-blue-700'>Người dùng mới</p>
								<p className='mt-2 text-3xl font-bold text-blue-900'>
									{formatNumber(stats?.newUsers ?? 0)}
								</p>
								<p className='mt-1 text-sm text-blue-700/80'>
									Tăng trưởng trong 7 ngày gần nhất
								</p>
							</CardContent>
						</Card>

						<Card className='border-amber-100 bg-amber-50/60 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-amber-700'>
									Công ty chờ duyệt
								</p>
								<p className='mt-2 text-3xl font-bold text-amber-900'>
									{formatNumber(stats?.pendingCompanies ?? 0)}
								</p>
								<p className='mt-1 text-sm text-amber-700/80'>
									Cần kiểm tra hồ sơ doanh nghiệp
								</p>
							</CardContent>
						</Card>

						<Card className='border-emerald-100 bg-emerald-50/60 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-emerald-700'>
									Tin tuyển dụng chờ duyệt
								</p>
								<p className='mt-2 text-3xl font-bold text-emerald-900'>
									{formatNumber(stats?.pendingJobs ?? 0)}
								</p>
								<p className='mt-1 text-sm text-emerald-700/80'>
									Cần kiểm duyệt trước khi hiển thị
								</p>
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

					<section className='space-y-3'>
						<div className='flex flex-col gap-1 sm:flex-row sm:items-end sm:justify-between'>
							<div>
								<h2 className='text-sm font-bold uppercase tracking-wide text-foreground'>
									Thao tác nhanh
								</h2>
								<p className='mt-1 text-sm text-muted-foreground'>
									Đi nhanh đến các màn hình quản trị thường dùng.
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