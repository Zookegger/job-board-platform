import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import {
	Briefcase,
	MapPin,
	Calendar,
	Clock,
	Users,
	ChevronLeft,
	DollarSign,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { ApplyDialog } from "./components/ApplyDialog";
import { useAuth } from "@/hooks/useAuth";
import { usePublicJobDetail } from "@/hooks/usePublicJobs";
import {
	EMPLOYMENT_TYPE_LABELS,
	EXPERIENCE_LEVEL_LABELS,
	LOCATION_TYPES_LABELS,
} from "@/types/job";
import RouterRoutes from "@/utils/RouterRoutes";

function formatSalary(min: number | null, max: number | null, currency: string | null) {
	if (!min && !max) return "Thỏa thuận";
	const fmt = (n: number) => new Intl.NumberFormat("vi-VN").format(n);
	const cur = currency ?? "VND";
	if (min && max) return `${fmt(min)} – ${fmt(max)} ${cur}`;
	if (min) return `Từ ${fmt(min)} ${cur}`;
	return `Đến ${fmt(max!)} ${cur}`;
}

function formatDate(dateStr: string | null | undefined) {
	if (!dateStr) return null;
	return new Intl.DateTimeFormat("vi-VN", { day: "2-digit", month: "2-digit", year: "numeric" }).format(
		new Date(dateStr),
	);
}

export function JobDetailPage() {
	const { id } = useParams<{ id: string }>();
	const { user } = useAuth();
	const [applyOpen, setApplyOpen] = useState(false);

	const { data: job, isLoading, isError } = usePublicJobDetail(id ?? "");

	const isCandidate = user?.role === "CANDIDATE";

	if (isLoading) {
		return (
			<div className="container mx-auto px-4 py-8 max-w-3xl space-y-4">
				<Skeleton className="h-8 w-3/4" />
				<Skeleton className="h-5 w-1/3" />
				<Skeleton className="h-48 w-full" />
			</div>
		);
	}

	if (isError || !job) {
		return (
			<div className="container mx-auto px-4 py-16 text-center max-w-3xl">
				<p className="text-muted-foreground">Không tìm thấy tin tuyển dụng.</p>
				<Button variant="link" asChild className="mt-3">
					<Link to={RouterRoutes.JOBS}>← Quay lại danh sách</Link>
				</Button>
			</div>
		);
	}

	return (
		<div className="container mx-auto px-4 py-8 max-w-3xl">
			<Button variant="ghost" size="sm" asChild className="mb-4 -ml-2">
				<Link to={RouterRoutes.JOBS}>
					<ChevronLeft className="h-4 w-4 mr-1" />
					Danh sách việc làm
				</Link>
			</Button>

			<div className="space-y-6">
				{/* Header */}
				<div>
					<h1 className="text-2xl font-bold">{job.title}</h1>
					<p className="mt-1 text-muted-foreground flex items-center gap-1.5">
						<Briefcase className="h-4 w-4" />
						{job.companyName}
					</p>
				</div>

				{/* Meta badges */}
				<div className="flex flex-wrap gap-2">
					<Badge variant="secondary">{EMPLOYMENT_TYPE_LABELS[job.employmentType]}</Badge>
					<Badge variant="secondary">{LOCATION_TYPES_LABELS[job.locationTypes]}</Badge>
					<Badge variant="outline">{EXPERIENCE_LEVEL_LABELS[job.experienceLevel]}</Badge>
					{job.categoryName && <Badge variant="outline">{job.categoryName}</Badge>}
				</div>

				{/* Info grid */}
				<div className="grid grid-cols-2 gap-3 text-sm">
					<div className="flex items-center gap-2 text-green-700 font-medium">
						<DollarSign className="h-4 w-4" />
						{formatSalary(job.salaryMin, job.salaryMax, job.currency)}
					</div>
					{job.location && (
						<div className="flex items-center gap-2 text-muted-foreground">
							<MapPin className="h-4 w-4" />
							{job.location}
						</div>
					)}
					{job.numberOfOpenings && (
						<div className="flex items-center gap-2 text-muted-foreground">
							<Users className="h-4 w-4" />
							{job.numberOfOpenings} vị trí tuyển dụng
						</div>
					)}
					{job.expirationDate && (
						<div className="flex items-center gap-2 text-muted-foreground">
							<Calendar className="h-4 w-4" />
							Hết hạn: {formatDate(job.expirationDate)}
						</div>
					)}
					{job.postedDate && (
						<div className="flex items-center gap-2 text-muted-foreground">
							<Clock className="h-4 w-4" />
							Đăng ngày: {formatDate(job.postedDate)}
						</div>
					)}
				</div>

				{/* Skills */}
				{job.skills && job.skills.length > 0 && (
					<div>
						<p className="text-sm font-medium mb-2">Kỹ năng yêu cầu</p>
						<div className="flex flex-wrap gap-2">
							{job.skills.map((s) => (
								<Badge key={s.id} variant="secondary">
									{s.name}
								</Badge>
							))}
						</div>
					</div>
				)}

				{isCandidate && (
					<Button onClick={() => setApplyOpen(true)} size="lg">
						<Briefcase className="mr-2 h-4 w-4" />
						Ứng tuyển ngay
					</Button>
				)}

				<Separator />

				{/* Description */}
				{job.description && (
					<section>
						<h2 className="text-lg font-semibold mb-3">Mô tả công việc</h2>
						<div className="prose prose-sm max-w-none whitespace-pre-wrap text-sm text-muted-foreground">
							{job.description}
						</div>
					</section>
				)}

				{/* Requirements */}
				{job.requirements && (
					<section>
						<h2 className="text-lg font-semibold mb-3">Yêu cầu ứng viên</h2>
						<div className="prose prose-sm max-w-none whitespace-pre-wrap text-sm text-muted-foreground">
							{job.requirements}
						</div>
					</section>
				)}

				{/* Benefits */}
				{job.benefits && (
					<section>
						<h2 className="text-lg font-semibold mb-3">Phúc lợi</h2>
						<div className="prose prose-sm max-w-none whitespace-pre-wrap text-sm text-muted-foreground">
							{job.benefits}
						</div>
					</section>
				)}
			</div>

			{job && (
				<ApplyDialog
					isOpen={applyOpen}
					onClose={() => setApplyOpen(false)}
					jobId={job.id}
					jobTitle={job.title}
					companyName={job.companyName}
				/>
			)}
		</div>
	);
}

