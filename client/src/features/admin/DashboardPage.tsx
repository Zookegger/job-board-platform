import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Briefcase, Building2, FileText, Users } from "lucide-react";
import type { ComponentType } from "react";

type MetricCardItem = {
	label: string;
	value: number;
	description: string;
	icon: ComponentType<{ className?: string }>;
};

const dashboardMetrics: MetricCardItem[] = [
	{
		label: "Tổng người dùng",
		value: 0,
		description: "Tất cả tài khoản trên hệ thống",
		icon: Users,
	},
	{
		label: "Tổng công ty",
		value: 0,
		description: "Công ty đã đăng ký trên nền tảng",
		icon: Building2,
	},
	{
		label: "Tổng tin tuyển dụng",
		value: 0,
		description: "Tất cả tin tuyển dụng trong hệ thống",
		icon: Briefcase,
	},
	{
		label: "Tổng hồ sơ ứng tuyển",
		value: 0,
		description: "Tổng số lượt ứng tuyển của ứng viên",
		icon: FileText,
	},
];

function formatNumber(value: number) {
	return new Intl.NumberFormat("vi-VN").format(value);
}

function MetricCard({ metric }: { metric: MetricCardItem }) {
	const Icon = metric.icon;

	return (
		<Card className='shadow-sm'>
			<CardHeader className='flex flex-row items-center justify-between space-y-0 pb-2'>
				<CardTitle className='text-sm font-medium text-muted-foreground'>
					{metric.label}
				</CardTitle>

				<div className='rounded-lg bg-primary/10 p-2 text-primary'>
					<Icon className='size-5' />
				</div>
			</CardHeader>

			<CardContent>
				<div className='text-3xl font-bold text-foreground'>
					{formatNumber(metric.value)}
				</div>

				<p className='mt-1 text-sm text-muted-foreground'>
					{metric.description}
				</p>
			</CardContent>
		</Card>
	);
}

export default function AdminDashboardPage() {
	return (
		<div className='space-y-6'>
			<div>
				<h1 className='text-2xl font-bold text-foreground'>
					Bảng điều khiển quản trị
				</h1>

				<p className='mt-1 text-sm text-muted-foreground'>
					Theo dõi nhanh các chỉ số tổng quan của nền tảng.
				</p>
			</div>

			<div className='grid gap-4 sm:grid-cols-2 xl:grid-cols-4'>
				{dashboardMetrics.map((metric) => (
					<MetricCard
						key={metric.label}
						metric={metric}
					/>
				))}
			</div>
		</div>
	);
}