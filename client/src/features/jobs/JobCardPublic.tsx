import { Badge } from "@/components/ui/badge";
import type { JobListResponse } from "@/types/job";
import {
	EMPLOYMENT_TYPE_LABELS,
	EXPERIENCE_LEVEL_LABELS,
	LOCATION_TYPES_LABELS,
} from "@/types/job";
import { TimeAgo } from "@/utils/DateUtils";
import RouterRoutes from "@/utils/RouterRoutes";
import { formatSalary } from "@/utils/StringUtil";
import { CalendarDays, Clock, MapPin } from "lucide-react";
import { Link } from "react-router-dom";


function CompanyLogo({
	companyName,
	logoUrl,
}: {
	companyName: string;
	logoUrl: string | null;
}) {
	if (logoUrl) {
		return (
			<img
				src={logoUrl}
				alt={companyName}
				className="size-12 rounded-lg object-cover border"
			/>
		);
	}
	return (
		<div className="size-12 rounded-lg bg-primary/10 flex items-center justify-center text-lg font-bold text-primary shrink-0">
			{companyName.charAt(0).toUpperCase()}
		</div>
	);
}

export function JobCardPublic({ job }: { job: JobListResponse }) {
	const expired =
		job.expirationDate && new Date(job.expirationDate) < new Date();

	return (
		<div className="border rounded-lg p-5 hover:shadow-md transition-shadow bg-card flex flex-col justify-between gap-3">
			<div className="flex items-start gap-3">
				<CompanyLogo
					companyName={job.companyName}
					logoUrl={job.companyLogoUrl}
				/>
				<div className="flex-1 min-w-0">
					<Link
						to={RouterRoutes.JOB_DETAIL(job.slug)}
						className="text-base font-semibold hover:text-primary line-clamp-2 leading-tight"
					>
						{job.title}
					</Link>
					<p className="mt-1 text-sm text-muted-foreground truncate">
						{job.companyName}
					</p>
				</div>
			</div>

			<div className="flex flex-wrap gap-1.5">
				<Badge variant="secondary" className="text-xs">
					{EMPLOYMENT_TYPE_LABELS[
						job.employmentType as keyof typeof EMPLOYMENT_TYPE_LABELS
					] ?? job.employmentType}
				</Badge>
				<Badge variant="outline" className="text-xs">
					{EXPERIENCE_LEVEL_LABELS[
						job.experienceLevel as keyof typeof EXPERIENCE_LEVEL_LABELS
					] ?? job.experienceLevel}
				</Badge>
				<Badge variant="secondary" className="text-xs">
					<MapPin className="h-3 w-3 mr-0.5" />
					{LOCATION_TYPES_LABELS[
						job.locationTypes as keyof typeof LOCATION_TYPES_LABELS
					] ?? job.locationTypes}
				</Badge>
			</div>

			<div className="text-sm font-medium text-green-700">
				{formatSalary(job.salaryMin, job.salaryMax, job.currency)}
			</div>

			<div className="flex items-center gap-3 text-xs text-muted-foreground mt-auto pt-1 border-t">
				<span className="flex items-center gap-1">
					<Clock className="h-3.5 w-3.5" />
					{TimeAgo(job.updatedAt ?? job.createdAt)}
				</span>
				{job.expirationDate && (
					<span
						className={`flex items-center gap-1 ${expired ? "text-destructive" : ""}`}
					>
						<CalendarDays className="h-3.5 w-3.5" />
						{expired ? "Hết hạn" : TimeAgo(job.expirationDate)}
					</span>
				)}
			</div>
		</div>
	);
}
