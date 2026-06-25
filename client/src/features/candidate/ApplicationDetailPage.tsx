import { useApplicationDetail, useApplicationTimeline } from "@/hooks/useApplications";
import { ApplicationStatusBadge } from "./components/ApplicationStatusBadge";
import { ApplicationTimeline } from "./components/ApplicationTimeline";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import RouterRoutes from "@/utils/RouterRoutes";
import { Building2, ExternalLink, MapPin, ArrowLeft } from "lucide-react";
import { Link, useParams } from "react-router-dom";

function formatDate(value: string | null) {
	if (!value) return "—";
	return new Intl.DateTimeFormat("vi-VN", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}

function ApplicationDetailSkeleton() {
	return (
		<div className="mx-auto flex w-full max-w-3xl flex-col gap-6">
			<Skeleton className="h-8 w-48" />
			<div className="rounded-lg border bg-card p-6">
				<div className="flex gap-4">
					<Skeleton className="size-14 rounded-lg" />
					<div className="flex-1 space-y-2">
						<Skeleton className="h-5 w-64" />
						<Skeleton className="h-4 w-40" />
						<Skeleton className="h-4 w-32" />
					</div>
				</div>
			</div>
			<div className="rounded-lg border bg-card p-6">
				<Skeleton className="mb-4 h-5 w-40" />
				<div className="space-y-4 pl-6">
					{Array.from({ length: 3 }).map((_, i) => (
						<div key={i} className="flex gap-3">
							<Skeleton className="size-4 rounded-full" />
							<div className="flex-1 space-y-2">
								<Skeleton className="h-4 w-24" />
								<Skeleton className="h-3 w-48" />
							</div>
						</div>
					))}
				</div>
			</div>
		</div>
	);
}

export default function CandidateApplicationDetailPage() {
	const { id } = useParams<{ id: string }>();

	const { data: application, isLoading: detailLoading } = useApplicationDetail(id);

	const { data: timeline, isLoading: timelineLoading, isError, error } = useApplicationTimeline(id);

	const isLoading = detailLoading || timelineLoading;

	if (isLoading) {
		return <ApplicationDetailSkeleton />;
	}

	if (!application) {
		return (
			<div className="mx-auto flex w-full max-w-3xl flex-col items-center gap-4 py-16">
				<p className="text-muted-foreground">Không tìm thấy đơn ứng tuyển.</p>
				<Button asChild variant="outline">
					<Link to={RouterRoutes.CANDIDATE_APPLICATIONS}>Quay lại danh sách</Link>
				</Button>
			</div>
		);
	}

	return (
		<div className="mx-auto flex w-full max-w-3xl flex-col gap-6">
			<Link
				to={RouterRoutes.CANDIDATE_APPLICATIONS}
				className="inline-flex items-center gap-1 text-sm text-muted-foreground hover:text-foreground w-fit"
			>
				<ArrowLeft className="size-4" />
				Quay lại danh sách
			</Link>

			<div className="rounded-lg border bg-card p-6">
				<div className="flex gap-4">
					<div className="flex size-14 shrink-0 items-center justify-center overflow-hidden rounded-lg border bg-muted">
						{application.companyLogoUrl ? (
							<img
								src={application.companyLogoUrl}
								alt={application.companyName}
								className="h-full w-full object-cover"
							/>
						) : (
							<Building2 className="size-6 text-muted-foreground" />
						)}
					</div>
					<div className="min-w-0 flex-1">
						<h1 className="text-lg font-semibold text-foreground">{application.jobTitle}</h1>
						<p className="text-sm text-muted-foreground">{application.companyName}</p>
						{application.jobLocation && (
							<div className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
								<MapPin className="size-3.5" />
								<span>{application.jobLocation}</span>
							</div>
						)}
						<div className="mt-3 flex flex-wrap items-center gap-3">
						<ApplicationStatusBadge status={application.status} />
							<span className="text-xs text-muted-foreground">
								Nộp ngày {formatDate(application.appliedAt)}
							</span>
						</div>
					</div>
					<Button variant="outline" size="sm" asChild className="shrink-0 self-start">
						<Link to={RouterRoutes.JOB_DETAIL(application.jobSlug)} target="_blank">
							<ExternalLink className="mr-1 size-3.5" />
							Xem tin
						</Link>
					</Button>
				</div>
			</div>

			<div className="rounded-lg border bg-card p-6">
				<h2 className="mb-4 text-base font-semibold text-foreground">Lịch sử trạng thái</h2>
				{isError ? (
					<p className="py-6 text-center text-sm text-destructive">
						{(error as Error)?.message || "Không thể tải lịch sử trạng thái."}
					</p>
				) : (
					<ApplicationTimeline
						timeline={timeline ?? []}
						currentStatus={application.status}
					/>
				)}
			</div>
		</div>
	);
}
