import { DataTable } from "@/components/shared/DataTable";
import { Button } from "@/components/ui/button";
import { ApplicationStatusBadge } from "@/features/candidate/components/ApplicationStatusBadge";
import { useMyApplications } from "@/hooks/useApplications";
import type { ApplicationListResponse, ApplicationStatus } from "@/types/application";
import RouterRoutes from "@/utils/RouterRoutes";
import { Building2, ExternalLink, FileText, MapPin, RefreshCw } from "lucide-react";
import { useMemo, useState } from "react";
import { Link } from "react-router-dom";

const DEFAULT_PAGE_SIZE = 10;

type StatusFilter = "all" | ApplicationStatus;

function formatDate(value: string | null) {
	if (!value) return "—";
	return new Intl.DateTimeFormat("vi-VN", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}

function CompanyLogo({ application }: { application: ApplicationListResponse }) {
	return (
		<div className="flex size-11 shrink-0 items-center justify-center overflow-hidden rounded-lg border bg-muted">
			{application.companyLogoUrl ? (
				<img
					src={application.companyLogoUrl}
					alt={application.companyName}
					className="h-full w-full object-cover"
				/>
			) : (
				<Building2 className="size-5 text-muted-foreground" />
			)}
		</div>
	);
}

export default function CandidateApplicationsPage() {
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
	const [statusFilter, setStatusFilter] = useState<StatusFilter>("all");

	const queryParams = useMemo(
		() => ({
			page,
			size: pageSize,
			status: statusFilter === "all" ? undefined : statusFilter,
		}),
		[page, pageSize, statusFilter],
	);

	const { data, isError, isFetching, isLoading, refetch, error } = useMyApplications(queryParams);

	const applications = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	return (
		<div className="mx-auto flex w-full max-w-7xl flex-col gap-5">
			<div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
				<div>
					<h1 className="text-2xl font-semibold text-foreground">Đơn ứng tuyển của tôi</h1>
					<p className="mt-1 text-sm text-muted-foreground">
						{totalElements.toLocaleString("vi-VN")} hồ sơ đã nộp
					</p>
				</div>
				<Button variant="outline" onClick={() => refetch()} disabled={isFetching} className="w-fit">
					<RefreshCw className={isFetching ? "animate-spin" : ""} />
					Làm mới
				</Button>
			</div>

			<div className="rounded-lg border bg-card p-4">
				<select
					value={statusFilter}
					onChange={(event) => {
						setStatusFilter(event.target.value as StatusFilter);
						setPage(0);
					}}
					className="h-10 w-full max-w-xs rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50"
				>
					<option value="all">Tất cả trạng thái</option>
					<option value="PENDING">Chờ xử lý</option>
					<option value="REVIEWING">Đang xem xét</option>
					<option value="INTERVIEW">Phỏng vấn</option>
					<option value="HIRED">Đã tuyển</option>
					<option value="REJECTED">Từ chối</option>
				</select>
			</div>

			<DataTable
				columns={[
					{
						key: "job",
						header: "Công việc",
						className: "align-top",
						render: (application) => (
							<div className="flex gap-3">
								<CompanyLogo application={application} />
								<div className="min-w-0">
									<p className="font-medium text-foreground">{application.jobTitle}</p>
									<p className="mt-1 text-sm text-muted-foreground">{application.companyName}</p>
									{application.jobLocation && (
										<div className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
											<MapPin className="size-3.5" />
											<span className="line-clamp-1">{application.jobLocation}</span>
										</div>
									)}
									<Link
										to={RouterRoutes.JOB_DETAIL(application.jobId)}
										className="mt-2 inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline"
									>
										Xem tin tuyển dụng <ExternalLink className="size-3" />
									</Link>
								</div>
							</div>
						),
					},
					{
						key: "appliedAt",
						header: "Ngày nộp",
						className: "align-top text-sm text-muted-foreground",
						render: (application) => formatDate(application.appliedAt),
					},
					{
						key: "status",
						header: "Trạng thái",
						className: "align-top",
						render: (application) => <ApplicationStatusBadge status={application.status} />,
					},
				]}
				data={applications}
				isLoading={isLoading}
				isError={isError}
				error={error}
				onRetry={() => refetch()}
				emptyState={{
					icon: FileText,
					title: "Bạn chưa nộp đơn nào",
					subtitle:
						statusFilter === "all"
							? "Khám phá việc làm phù hợp và bắt đầu ứng tuyển ngay."
							: "Không có đơn ứng tuyển nào với trạng thái này.",
				}}
				pageResponse={data}
				pageable={{
					page,
					pageSize,
					totalPages,
					totalElements,
					onPageChange: setPage,
					onPageSizeChange: (newSize) => {
						setPageSize(newSize);
						setPage(0);
					},
					isFetching,
					label: "đơn ứng tuyển",
				}}
				minWidth="min-w-[720px]"
			/>
		</div>
	);
}
