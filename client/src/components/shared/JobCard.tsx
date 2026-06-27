import { Link } from "react-router-dom";

import type { PublicCompanyJob } from "@/api/publicCompany";
import { formatDate } from "@/utils/DateUtils";

const STATUS_MAP: Record<string, string> = {
	ACTIVE: "Đang tuyển",
	EXPIRED: "Đã hết hạn",
	PENDING_APPROVAL: "Chờ duyệt",
	DRAFT: "Bản nháp",
	REJECTED: "Bị từ chối",
};


interface JobCardProps {
	job: PublicCompanyJob;
}

function JobStatusBadge({ status }: { status?: string }) {
	const label = STATUS_MAP[status || ""] || status || "Không rõ";
	return (
		<span className="inline-block rounded-full bg-indigo-50 px-2.5 py-0.5 text-xs font-medium text-indigo-700">
			{label}
		</span>
	);
}

function JobCard({ job }: JobCardProps) {
	return (
		<div className="flex flex-col gap-3 rounded-xl border border-gray-200 p-4 transition hover:border-blue-500 hover:shadow-md">
			<div className="flex items-start justify-between gap-4">
				<div className="min-w-0 flex-1">
					<Link
						to={`/jobs/${job.slug}`}
						className="text-base font-semibold text-gray-900 hover:text-blue-600"
					>
						{job.title}
					</Link>
					{job.companyName && (
						<p className="mt-0.5 text-sm text-gray-500">
							{job.companySlug ? (
								<Link
									to={`/companies/${job.companySlug}`}
									className="hover:text-blue-600"
								>
									{job.companyName}
								</Link>
							) : (
								job.companyName
							)}
						</p>
					)}
				</div>
				<Link
					to={`/jobs/${job.slug}`}
					className="shrink-0 rounded-lg bg-blue-600 px-3.5 py-2 text-sm font-semibold text-white hover:bg-blue-700"
				>
					Xem chi tiết
				</Link>
			</div>
			<div className="flex flex-wrap items-center gap-2">
				{job.location && (
					<span className="rounded-full bg-blue-50 px-2.5 py-0.5 text-xs font-medium text-blue-700">
						{job.location}
					</span>
				)}
				<JobStatusBadge status={job.status} />
				{job.createdAt && (
					<span className="rounded-full bg-gray-100 px-2.5 py-0.5 text-xs text-gray-600">
						Đăng ngày {formatDate(job.createdAt, { dateStyle: "short" })}
					</span>
				)}
				{job.skills && job.skills.length > 0 && (
					<>
						{job.skills.slice(0, 4).map((skill) => (
							<span
								key={skill.id}
								className="rounded-full bg-green-50 px-2.5 py-0.5 text-xs font-medium text-green-700"
							>
								{skill.name}
							</span>
						))}
						{job.skills.length > 4 && (
							<span className="text-xs text-gray-400">+{job.skills.length - 4}</span>
						)}
					</>
				)}
			</div>
		</div>
	);
}

export { JobCard };

