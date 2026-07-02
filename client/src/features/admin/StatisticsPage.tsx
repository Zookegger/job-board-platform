import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { useAdminApplicationChartStats } from "@/hooks/useAdminDashboard";
import { BarChart3, PieChart as PieChartIcon, RefreshCcw } from "lucide-react";
import { useState } from "react";
import {
	Area,
	AreaChart,
	CartesianGrid,
	Cell,
	Pie,
	PieChart,
	ResponsiveContainer,
	Tooltip,
	XAxis,
	YAxis,
} from "recharts";

import type { DailyApplicationPoint, StatusDistributionPoint } from "@/types/admin";

type StatisticsRange = 7 | 30;

function formatNumber(value: number) {
	return new Intl.NumberFormat("vi-VN").format(value);
}

function formatShortDate(date: string) {
	return new Intl.DateTimeFormat("vi-VN", {
		day: "2-digit",
		month: "2-digit",
	}).format(new Date(date));
}

function ChartCardSkeleton() {
	return (
		<Card className='shadow-sm'>
			<CardHeader>
				<Skeleton className='h-5 w-48' />
				<Skeleton className='h-4 w-72' />
			</CardHeader>

			<CardContent>
				<Skeleton className='h-80 w-full rounded-xl' />
			</CardContent>
		</Card>
	);
}

const statusMeta: Record<
	string,
	{
		label: string;
		colorClassName: string;
		bgClassName: string;
		hexColor: string;
	}
> = {
	PENDING: {
		label: "Chờ xử lý",
		colorClassName: "text-amber-500",
		bgClassName: "bg-amber-500",
		hexColor: "#F59E0B",
	},
	REVIEWING: {
		label: "Đang xem xét",
		colorClassName: "text-blue-500",
		bgClassName: "bg-blue-500",
		hexColor: "#3B82F6",
	},
	INTERVIEW: {
		label: "Phỏng vấn",
		colorClassName: "text-violet-500",
		bgClassName: "bg-violet-500",
		hexColor: "#8B5CF6",
	},
	HIRED: {
		label: "Đã tuyển",
		colorClassName: "text-emerald-500",
		bgClassName: "bg-emerald-500",
		hexColor: "#10B981",
	},
	REJECTED: {
		label: "Từ chối",
		colorClassName: "text-red-500",
		bgClassName: "bg-red-500",
		hexColor: "#EF4444",
	},
};

function getStatusMeta(status: string) {
	return (
		statusMeta[status] ?? {
			label: status,
			colorClassName: "text-slate-500",
			bgClassName: "bg-slate-500",
			hexColor: "#64748B",
		}
	);
}

function ApplicationsLineChart({ data }: { data: DailyApplicationPoint[] }) {
	if (!data.length) {
		return (
			<div className='flex h-80 items-center justify-center rounded-xl border border-dashed bg-muted/30 text-sm text-muted-foreground'>
				Chưa có dữ liệu ứng tuyển trong khoảng thời gian này.
			</div>
		);
	}

	return (
		<div className='h-80 rounded-xl border bg-background px-4 py-5'>
			<ResponsiveContainer
				width='100%'
				height='100%'
			>
				<AreaChart
					data={data}
					margin={{ top: 8, right: 8, left: -8, bottom: 0 }}
				>
					<defs>
						<linearGradient
							id='areaGradient'
							x1='0'
							y1='0'
							x2='0'
							y2='1'
						>
							<stop
								offset='0%'
								stopColor='#2563eb'
								stopOpacity={0.18}
							/>
							<stop
								offset='100%'
								stopColor='#2563eb'
								stopOpacity={0.02}
							/>
						</linearGradient>
					</defs>

					<CartesianGrid
						strokeDasharray='4 6'
						stroke='currentColor'
						className='text-muted'
						vertical={false}
					/>

					<XAxis
						dataKey='date'
						tickFormatter={formatShortDate}
						tick={{ fontSize: 14, fill: "currentColor" }}
						className='fill-muted-foreground'
						axisLine={false}
						tickLine={false}
						dy={8}
						interval='preserveStartEnd'
					/>

					<YAxis
						tick={{ fontSize: 14, fill: "currentColor" }}
						className='fill-muted-foreground'
						axisLine={false}
						tickLine={false}
						dx={-4}
						allowDecimals={false}
					/>

					<Tooltip
						content={({ active, payload }) => {
							if (!active || !payload?.length) return null;
							const point = payload[0].payload as DailyApplicationPoint;

							return (
								<div className='min-w-40 rounded-lg border bg-popover px-3 py-2 text-sm shadow-md'>
									<p className='font-semibold text-foreground'>{formatShortDate(point.date)}</p>
									<p className='whitespace-nowrap text-muted-foreground'>
										{formatNumber(point.total)} đơn ứng tuyển
									</p>
								</div>
							);
						}}
					/>

					<Area
						type='monotone'
						dataKey='total'
						fill='url(#areaGradient)'
						stroke='#2563eb'
						strokeWidth={3}
						dot={{ r: 4, fill: "#2563eb", strokeWidth: 0 }}
						activeDot={{ r: 7, fill: "#2563eb", strokeWidth: 0 }}
					/>
				</AreaChart>
			</ResponsiveContainer>
		</div>
	);
}

const donutColors: Record<string, string> = {
	PENDING: "#F59E0B",
	REVIEWING: "#3B82F6",
	INTERVIEW: "#8B5CF6",
	HIRED: "#10B981",
	REJECTED: "#EF4444",
};

function ApplicationStatusDonutChart({
	data,
	totalApplications,
}: {
	data: StatusDistributionPoint[];
	totalApplications: number;
}) {
	const [hoveredIndex, setHoveredIndex] = useState<number | null>(null);

	if (!data.length || totalApplications === 0) {
		return (
			<div className='flex h-80 items-center justify-center rounded-xl border border-dashed bg-muted/30 text-center'>
				<div>
					<p className='text-sm font-medium text-foreground'>Chưa có dữ liệu trạng thái ứng tuyển</p>
					<p className='mt-1 text-sm text-muted-foreground'>
						Biểu đồ sẽ hiển thị khi có hồ sơ ứng tuyển trong khoảng thời gian này.
					</p>
				</div>
			</div>
		);
	}

	const segments = data.map((item) => ({
		...item,
		meta: getStatusMeta(item.status),
	}));

	return (
		<div className='grid min-h-80 gap-5 rounded-xl border bg-background p-5 xl:grid-cols-[230px_1fr] xl:items-center'>
			<div className='relative mx-auto h-[230px] w-[230px]'>
				<ResponsiveContainer
					width='100%'
					height='100%'
				>
					<PieChart>
						<Pie
							data={data}
							dataKey='total'
							nameKey='status'
							cx='50%'
							cy='50%'
							innerRadius={60}
							outerRadius={90}
							paddingAngle={2}
							strokeWidth={0}
						>
							{data.map((entry) => (
								<Cell
									key={entry.status}
									fill={donutColors[entry.status] ?? "#64748B"}
									opacity={hoveredIndex === null ? 1 : hoveredIndex === data.indexOf(entry) ? 1 : 0.3}
									onMouseEnter={() => setHoveredIndex(data.indexOf(entry))}
									onMouseLeave={() => setHoveredIndex(null)}
								/>
							))}
						</Pie>

						<Tooltip
							content={({ active, payload }) => {
								if (!active || !payload?.length) return null;
								const entry = payload[0].payload as StatusDistributionPoint;

								return (
									<div className='min-w-40 rounded-lg border bg-popover px-3 py-2 text-sm shadow-md'>
										<p className='font-semibold text-foreground'>
											{getStatusMeta(entry.status).label}
										</p>
										<p className='whitespace-nowrap text-muted-foreground'>
											{formatNumber(entry.total)} hồ sơ · {entry.percentage}%
										</p>
									</div>
								);
							}}
						/>
					</PieChart>
				</ResponsiveContainer>

				<div className='pointer-events-none absolute inset-0 flex flex-col items-center justify-center text-center'>
					<p className='text-3xl font-bold text-foreground'>{formatNumber(totalApplications)}</p>
					<p className='mt-1 text-xs text-muted-foreground'>Tổng hồ sơ</p>
				</div>
			</div>

			<div className='space-y-3'>
				{hoveredIndex !== null ? (
					<div className='rounded-xl border bg-muted/30 p-3'>
						<p className='text-sm font-semibold text-foreground'>{segments[hoveredIndex].meta.label}</p>
						<p className='mt-1 text-sm text-muted-foreground'>
							{formatNumber(segments[hoveredIndex].total)} hồ sơ · {segments[hoveredIndex].percentage}%
						</p>
					</div>
				) : (
					<div className='rounded-xl border bg-muted/30 p-3'>
						<p className='text-sm font-semibold text-foreground'>Phân phối trạng thái</p>
						<p className='mt-1 text-sm text-muted-foreground'>
							Hover vào vòng tròn hoặc dòng trạng thái để xem chi tiết.
						</p>
					</div>
				)}

				<div className='space-y-2'>
					{segments.map((segment) => (
						<div
							key={segment.status}
							className={`flex items-center justify-between gap-3 rounded-lg border px-3 py-2 transition-colors ${
								hoveredIndex === segments.indexOf(segment)
									? "bg-muted"
									: "bg-background hover:bg-muted/50"
							}`}
							onMouseEnter={() => setHoveredIndex(segments.indexOf(segment))}
							onMouseLeave={() => setHoveredIndex(null)}
						>
							<div className='flex min-w-0 items-center gap-3'>
								<span className={`size-3 shrink-0 rounded-full ${segment.meta.bgClassName}`} />

								<div className='min-w-0'>
									<p className='truncate text-sm font-medium text-foreground'>{segment.meta.label}</p>
									<p className='text-xs text-muted-foreground'>{segment.status}</p>
								</div>
							</div>

							<div className='shrink-0 text-right'>
								<p className='text-sm font-semibold text-foreground'>{formatNumber(segment.total)}</p>
								<p className='text-xs text-muted-foreground'>{segment.percentage}%</p>
							</div>
						</div>
					))}
				</div>
			</div>
		</div>
	);
}

export default function AdminStatisticsPage() {
	const [days, setDays] = useState<StatisticsRange>(7);

	const { data, isLoading, isError, isFetching, refetch } = useAdminApplicationChartStats(days);

	return (
		<div className='space-y-6'>
			<Card className='overflow-hidden border-primary/10 bg-gradient-to-br from-primary/10 via-background to-muted/40 shadow-sm'>
				<CardContent className='p-6'>
					<div className='flex flex-col gap-5 lg:flex-row lg:items-center lg:justify-between'>
						<div>
							<div className='inline-flex items-center gap-2 rounded-full bg-primary/10 px-3 py-1 text-sm font-medium text-primary'>
								<BarChart3 className='size-4' />
								Admin Statistics
							</div>

							<h1 className='mt-4 text-3xl font-bold tracking-tight text-foreground'>
								Thống kê ứng tuyển
							</h1>

							<p className='mt-2 max-w-2xl text-sm leading-6 text-muted-foreground'>
								Theo dõi xu hướng số đơn ứng tuyển theo thời gian và phân phối trạng thái hồ sơ để admin
								nắm nhanh tình hình tuyển dụng.
							</p>
						</div>

						<div className='flex w-fit rounded-xl border bg-background/80 p-1 shadow-sm'>
							<Button
								type='button'
								size='sm'
								variant={days === 7 ? "default" : "ghost"}
								onClick={() => setDays(7)}
							>
								7 ngày
							</Button>

							<Button
								type='button'
								size='sm'
								variant={days === 30 ? "default" : "ghost"}
								onClick={() => setDays(30)}
							>
								30 ngày
							</Button>
						</div>
					</div>
				</CardContent>
			</Card>

			{isError ? (
				<Card className='border-destructive/40 bg-destructive/5 shadow-sm'>
					<CardContent className='flex flex-col gap-4 py-6 text-sm text-destructive sm:flex-row sm:items-center sm:justify-between'>
						<span>Không thể tải dữ liệu thống kê ứng tuyển. Vui lòng thử lại sau.</span>

						<Button
							type='button'
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
			) : null}

			{isLoading ? (
				<div className='grid gap-6 xl:grid-cols-[1.15fr_0.85fr]'>
					<ChartCardSkeleton />
					<ChartCardSkeleton />
				</div>
			) : !isError ? (
				<>
					<div className='grid gap-4 md:grid-cols-3'>
						<Card className='border-blue-100 bg-blue-50/50 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-blue-700'>Khoảng thời gian</p>
								<p className='mt-2 text-3xl font-bold text-blue-950'>{data?.days ?? days} ngày</p>
								<p className='mt-1 text-sm text-blue-700/80'>
									{data?.fromDate} → {data?.toDate}
								</p>
							</CardContent>
						</Card>

						<Card className='border-emerald-100 bg-emerald-50/60 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-emerald-700'>Tổng đơn ứng tuyển</p>
								<p className='mt-2 text-3xl font-bold text-emerald-950'>
									{formatNumber(data?.totalApplications ?? 0)}
								</p>
								<p className='mt-1 text-sm text-emerald-700/80'>Trong khoảng thời gian đã chọn</p>
							</CardContent>
						</Card>

						<Card className='border-amber-100 bg-amber-50/60 shadow-sm'>
							<CardContent className='p-5'>
								<p className='text-sm font-medium text-amber-700'>Số trạng thái</p>
								<p className='mt-2 text-3xl font-bold text-amber-950'>
									{formatNumber(data?.statusDistribution.length ?? 0)}
								</p>
								<p className='mt-1 text-sm text-amber-700/80'>Trạng thái có phát sinh hồ sơ</p>
							</CardContent>
						</Card>
					</div>

					<div className='grid gap-6 xl:grid-cols-[1.15fr_0.85fr]'>
						<Card className='shadow-sm'>
							<CardHeader>
								<div className='flex items-center gap-2'>
									<div className='rounded-lg bg-blue-500/10 p-2 text-blue-600'>
										<BarChart3 className='size-5' />
									</div>

									<div>
										<CardTitle className='text-base'>Xu hướng đơn ứng tuyển theo ngày</CardTitle>
										<p className='mt-1 text-sm text-muted-foreground'>
											Biểu đồ đường tăng trưởng số đơn ứng tuyển theo từng ngày.
										</p>
									</div>
								</div>
							</CardHeader>

							<CardContent>
								<ApplicationsLineChart data={data?.dailyApplications ?? []} />
							</CardContent>
						</Card>

						<Card className='shadow-sm'>
							<CardHeader>
								<div className='flex items-center gap-2'>
									<div className='rounded-lg bg-emerald-500/10 p-2 text-emerald-600'>
										<PieChartIcon className='size-5' />
									</div>

									<div>
										<CardTitle className='text-base'>Phân phối trạng thái ứng tuyển</CardTitle>
										<p className='mt-1 text-sm text-muted-foreground'>
											Biểu đồ donut theo trạng thái hồ sơ ứng tuyển.
										</p>
									</div>
								</div>
							</CardHeader>

							<CardContent>
								<ApplicationStatusDonutChart
									data={data?.statusDistribution ?? []}
									totalApplications={data?.totalApplications ?? 0}
								/>
							</CardContent>
						</Card>
					</div>
				</>
			) : null}
		</div>
	);
}
