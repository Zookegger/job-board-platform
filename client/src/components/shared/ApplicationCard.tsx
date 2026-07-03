import { Link } from "react-router-dom";
import { Building2, Eye, ExternalLink, MapPin } from "lucide-react";

import { Button } from "@/components/ui/button";
import { ApplicationStatusBadge } from "@/components/shared/ApplicationTimeline";
import type { ApplicationListResponse } from "@/types/application";
import { formatDate } from "@/utils/DateUtils";
import RouterRoutes from "@/utils/RouterRoutes";

interface ApplicationCardProps {
	application: ApplicationListResponse;
}

function CompanyLogo({ application }: { application: ApplicationListResponse }) {
	return (
		<div className='flex size-12 shrink-0 items-center justify-center overflow-hidden rounded-xl border bg-muted/50 shadow-sm'>
			{application.companyLogoUrl ? (
				<img
					src={application.companyLogoUrl}
					alt={application.companyName}
					className='h-full w-full object-cover'
				/>
			) : (
				<Building2 className='size-5 text-muted-foreground/70' />
			)}
		</div>
	);
}

export function ApplicationCard({ application }: ApplicationCardProps) {
	return (
		<div className='flex flex-col gap-3 rounded-xl border bg-card p-4 shadow-sm transition hover:border-blue-500 hover:shadow-md'>
			<div className='flex items-start justify-between gap-3'>
				<div className='flex min-w-0 flex-1 items-start gap-3'>
					<CompanyLogo application={application} />
					<div className='min-w-0 flex-1'>
						<Link to={RouterRoutes.JOB_DETAIL(application.jobSlug)}>
							<p className='truncate text-base font-semibold text-foreground transition-colors hover:text-primary'>
								{application.jobTitle}
							</p>
						</Link>
						<p className='mt-0.5 truncate text-sm font-medium text-muted-foreground'>
							{application.companyName}
						</p>
					</div>
				</div>
				<ApplicationStatusBadge status={application.status} />
			</div>

			<div className='flex flex-wrap items-center gap-x-3 gap-y-1 text-xs text-muted-foreground/80'>
				{application.jobLocation && (
					<div className='flex items-center gap-1'>
						<MapPin className='size-3.5 shrink-0' />
						<span className='truncate'>{application.jobLocation}</span>
					</div>
				)}
				<span>Nộp ngày: {formatDate(application.appliedAt)}</span>
			</div>

			<div className='flex items-center gap-2 border-t pt-3'>
				<Button
					variant='outline'
					size='sm'
					className='h-8'
					onClick={() => {
						window.location.href = RouterRoutes.CANDIDATE_APPLICATION_DETAIL(application.id);
					}}
				>
					<Eye className='mr-1.5 size-4' />
					Chi tiết
				</Button>
				<Button
					variant='outline'
					size='sm'
					className='h-8'
					onClick={() => {
						window.location.href = RouterRoutes.JOB_DETAIL(application.jobSlug);
					}}
				>
					<ExternalLink className='mr-1.5 size-4' />
					Tin đăng
				</Button>
			</div>
		</div>
	);
}
