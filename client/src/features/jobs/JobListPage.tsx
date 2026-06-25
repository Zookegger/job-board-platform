import { useState } from "react";
import { Link } from "react-router-dom";
import { Briefcase, MapPin, ChevronLeft, ChevronRight } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { usePublicJobs } from "@/hooks/usePublicJobs";
import { EMPLOYMENT_TYPE_LABELS, EXPERIENCE_LEVEL_LABELS, LOCATION_TYPES_LABELS } from "@/types/job";
import RouterRoutes from "@/utils/RouterRoutes";
import type { JobListResponse } from "@/types/job";

function formatSalary(min: number | null, max: number | null, currency: string | null) {
	if (!min && !max) return "Thỏa thuận";
	const fmt = (n: number) => new Intl.NumberFormat("vi-VN").format(n);
	const cur = currency ?? "VND";
	if (min && max) return `${fmt(min)} – ${fmt(max)} ${cur}`;
	if (min) return `Từ ${fmt(min)} ${cur}`;
	return `Đến ${fmt(max!)} ${cur}`;
}

function JobCard({ job }: { job: JobListResponse }) {
	return (
		<Card className="hover:shadow-md transition-shadow">
			<CardContent className="p-5">
				<div className="flex items-start justify-between gap-3">
					<div className="flex-1 min-w-0">
						<Link
							to={RouterRoutes.JOB_DETAIL(job.slug)}
							className="text-base font-semibold hover:text-primary line-clamp-2"
						>
							{job.title}
						</Link>
						<p className="mt-1 text-sm text-muted-foreground flex items-center gap-1">
							<Briefcase className="h-3.5 w-3.5 shrink-0" />
							{job.companyName}
						</p>
					</div>
				</div>

				<div className="mt-3 flex flex-wrap gap-2">
					<Badge variant="secondary">{EMPLOYMENT_TYPE_LABELS[job.employmentType]}</Badge>
					<Badge variant="secondary">{LOCATION_TYPES_LABELS[job.locationTypes]}</Badge>
					<Badge variant="outline">{EXPERIENCE_LEVEL_LABELS[job.experienceLevel]}</Badge>
				</div>

				<div className="mt-3 flex items-center justify-between text-sm">
					<span className="font-medium text-green-700">
						{formatSalary(job.salaryMin, job.salaryMax, job.currency)}
					</span>
					{job.numberOfOpenings && (
						<span className="text-muted-foreground">{job.numberOfOpenings} vị trí</span>
					)}
				</div>
			</CardContent>
		</Card>
	);
}

export function JobListPage() {
	const [page, setPage] = useState(0);
	const { data, isLoading, isError } = usePublicJobs(page, 12);

	const jobs = data?.content ?? [];
	const totalPages = data?.totalPages ?? 0;
	const totalElements = data?.totalElements ?? 0;

	return (
		<div className="container mx-auto px-4 py-8 max-w-4xl">
			<div className="mb-6">
				<h1 className="text-2xl font-bold">Việc làm mới nhất</h1>
				{!isLoading && (
					<p className="mt-1 text-sm text-muted-foreground">
						{totalElements} tin tuyển dụng đang mở
					</p>
				)}
			</div>

			{isLoading && (
				<div className="space-y-4">
					{Array.from({ length: 6 }).map((_, i) => (
						<Skeleton key={i} className="h-36 w-full rounded-lg" />
					))}
				</div>
			)}

			{isError && (
				<div className="text-center py-16 text-muted-foreground">
					Không thể tải danh sách việc làm. Vui lòng thử lại.
				</div>
			)}

			{!isLoading && !isError && jobs.length === 0 && (
				<div className="text-center py-16 text-muted-foreground">
					Hiện chưa có tin tuyển dụng nào.
				</div>
			)}

			{!isLoading && jobs.length > 0 && (
				<>
					<div className="space-y-4">
						{jobs.map((job) => (
							<JobCard key={job.id} job={job} />
						))}
					</div>

					{totalPages > 1 && (
						<div className="mt-8 flex items-center justify-center gap-3">
							<Button
								variant="outline"
								size="sm"
								disabled={page === 0}
								onClick={() => setPage((p) => p - 1)}
							>
								<ChevronLeft className="h-4 w-4" />
								Trước
							</Button>
							<span className="text-sm text-muted-foreground">
								Trang {page + 1} / {totalPages}
							</span>
							<Button
								variant="outline"
								size="sm"
								disabled={page >= totalPages - 1}
								onClick={() => setPage((p) => p + 1)}
							>
								Tiếp
								<ChevronRight className="h-4 w-4" />
							</Button>
						</div>
					)}
				</>
			)}
		</div>
	);
}

