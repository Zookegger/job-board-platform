import { AuthRequiredDialog } from "@/components/shared/AuthRequiredDialog";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { useApplicationByJob, useWithdrawApplication } from "@/hooks/useApplications";
import { useAuth } from "@/hooks/useAuth";
import { usePublicJobDetail, usePublicRelatedJobList } from "@/hooks/usePublicJobs";
import { useToast } from "@/providers/ToastProvider";
import { EMPLOYMENT_TYPE_LABELS, EXPERIENCE_LEVEL_LABELS, LOCATION_TYPES_LABELS } from "@/types/job";
import { formatDate, TimeFromNow } from "@/utils/DateUtils";
import RouterRoutes from "@/utils/RouterRoutes";
import { formatSalary } from "@/utils/StringUtil";
import {
	Briefcase,
	Calendar,
	ChartBarStacked,
	CheckCircle2,
	ChevronLeft,
	DollarSign,
	Loader2,
	MapPin,
	TrendingUp,
	Users,
	Wrench
} from "lucide-react";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { JobCardPublic } from "./JobCardPublic";
import { ApplyDialog } from "./components/ApplyDialog";

export function JobDetailPage() {
	const { slug } = useParams<{ slug: string }>();
	const { data: job, isLoading, isError } = usePublicJobDetail(slug ?? "");
	const isJobExpired = job?.expirationDate && new Date(job.expirationDate) < new Date() ? true : false;
	const { data: relatedJobs } = usePublicRelatedJobList(job?.id ?? "", { related: true });

	if (isLoading) {
		return (
			<div className='container mx-auto px-4 py-8 max-w-3xl space-y-4'>
				<Skeleton className='h-8 w-3/4' />
				<Skeleton className='h-5 w-1/3' />
				<Skeleton className='h-48 w-full' />
			</div>
		);
	}

	if (isError || !job) {
		return (
			<div className='container mx-auto px-4 py-16 text-center max-w-3xl'>
				<p className='text-muted-foreground'>Không tìm thấy tin tuyển dụng.</p>
				<Button
					variant='link'
					asChild
					className='mt-3'
				>
					<Link to={RouterRoutes.JOBS}>← Quay lại danh sách</Link>
				</Button>
			</div>
		);
	}

	return (
		<div className='container mx-auto px-4 pt-4 pb-8 max-w-6xl'>
			<Button
				variant='ghost'
				size='sm'
				asChild
				className='mb-4 -ml-2'
			>
				<Link to={RouterRoutes.JOBS}>
					<ChevronLeft className='h-4 w-4 mr-1' />
					Danh sách việc làm
				</Link>
			</Button>

			<div className='grid grid-cols-1 lg:grid-cols-3 gap-8 items-start'>
				{/* Main Content Area */}
				<div className='lg:col-span-2 space-y-8 '>
					<div className='bg-card shadow-sm rounded-xl p-6 space-y-6 border border-border'>
						{/* Header Section */}
						<div className='space-y-4'>
							<div className='flex flex-col gap-2'>
								<h1 className='text-3xl font-extrabold'>{job.title}</h1>
								<div className='flex items-center text-green-700 font-semibold text-lg gap-1'>
									<DollarSign className='h-5 w-5 shrink-0' />
									{job.salaryMin && job.salaryMax
										? formatSalary(job.salaryMin, job.salaryMax, job.currency ?? "VND")
										: "Thương lượng"}
								</div>
							</div>

							{/* Info grid */}
							<div className='grid grid-cols-1 sm:grid-cols-2 gap-4 text-sm text-muted-foreground bg-muted/30 p-4 rounded-xl border border-border/50'>
								{job.location && (
									<div className='flex items-center gap-2'>
										<MapPin className='h-4 w-4 shrink-0' />
										<span className='truncate'>{job.location}</span>
									</div>
								)}
								{job.numberOfOpenings && (
									<div className='flex items-center gap-2'>
										<Users className='h-4 w-4 shrink-0' />
										<span>{job.numberOfOpenings} vị trí tuyển dụng</span>
									</div>
								)}
								{job.expirationDate && (
									<div className='flex items-center gap-2'>
										<Calendar className='h-4 w-4 shrink-0' />
										<span>
											Hết hạn: {isJobExpired ? "Đã hết hạn" : TimeFromNow(job.expirationDate)}
										</span>
									</div>
								)}
							</div>
						</div>

						<ApplySection
							jobId={job.id}
							jobTitle={job.title}
							companyName={job.companyName}
							isJobExpired={isJobExpired}
						/>

						<Separator />

						{/* Content Blocks (Descriptions, Requirements, Benefits) */}
						<div className='space-y-8'>
							{job.description && (
								<section>
									<h2 className='text-lg font-bold mb-4 tracking-tight'>Mô tả công việc</h2>
									<div className='prose prose-sm sm:prose-base max-w-none whitespace-pre-wrap text-muted-foreground'>
										{job.description}
									</div>
								</section>
							)}

							{job.requirements && (
								<section>
									<h2 className='text-lg font-bold mb-4 tracking-tight'>Yêu cầu ứng viên</h2>
									<div className='prose prose-sm sm:prose-base max-w-none whitespace-pre-wrap text-muted-foreground'>
										{job.requirements}
									</div>
								</section>
							)}

							{job.benefits && (
								<section>
									<h2 className='text-lg font-bold mb-4 tracking-tight'>Phúc lợi</h2>
									<div className='prose prose-sm sm:prose-base max-w-none whitespace-pre-wrap text-muted-foreground bg-primary/5 p-6 rounded-xl border border-primary/10'>
										{job.benefits}
									</div>
								</section>
							)}

							<section>
								<h2 className='text-lg font-bold mb-4 tracking-tight'>Thông tin công việc</h2>

								<div className='grid grid-cols-1 sm:grid-cols-2 gap-6'>
									<section className='flex items-start gap-3'>
										<div className='bg-muted flex h-10 w-10 shrink-0 items-center justify-center rounded-full'>
											<Calendar className='h-5 w-5 text-muted-foreground' />
										</div>
										<div className='space-y-1.5 pt-0.5'>
											<h3 className='text-xs font-bold text-muted-foreground uppercase'>
												Ngày đăng
											</h3>
											<p className='text-sm font-medium text-foreground'>
												{formatDate(job.createdAt, {
													day: "2-digit",
													month: "2-digit",
													year: "numeric",
												})}
											</p>
										</div>
									</section>

									<section className='flex items-start gap-3'>
										<div className='bg-muted flex h-10 w-10 shrink-0 items-center justify-center rounded-full'>
											<TrendingUp className='h-5 w-5 text-muted-foreground' />
										</div>
										<div className='space-y-1.5 pt-0.5'>
											<h3 className='text-xs font-bold text-muted-foreground uppercase'>
												Cấp bậc
											</h3>
											<p className='text-sm font-medium text-foreground'>
												{EXPERIENCE_LEVEL_LABELS[job.experienceLevel]}
											</p>
										</div>
									</section>

									<section className='flex items-start gap-3'>
										<div className='bg-muted flex h-10 w-10 shrink-0 items-center justify-center rounded-full'>
											<MapPin className='h-5 w-5 text-muted-foreground' />
										</div>
										<div className='space-y-1.5 pt-0.5'>
											<h3 className='text-xs font-bold text-muted-foreground uppercase'>
												Hình thức làm việc
											</h3>
											<p className='text-sm font-medium text-foreground'>
												{LOCATION_TYPES_LABELS[job.locationTypes]}
											</p>
										</div>
									</section>

									<section className='flex items-start gap-3'>
										<div className='bg-muted flex h-10 w-10 shrink-0 items-center justify-center rounded-full'>
											<ChartBarStacked className='h-5 w-5 text-muted-foreground' />
										</div>
										<div className='space-y-1.5 pt-0.5'>
											<h3 className='text-xs font-bold text-muted-foreground uppercase'>
												Ngành nghề
											</h3>
											<p className='text-sm font-medium text-foreground'>{job.categoryName}</p>
										</div>
									</section>

									<section className='flex items-start gap-3'>
										<div className='bg-muted flex h-10 w-10 shrink-0 items-center justify-center rounded-full'>
											<Briefcase className='h-5 w-5 text-muted-foreground' />
										</div>
										<div className='space-y-1.5 pt-0.5'>
											<h3 className='text-xs font-bold text-muted-foreground uppercase'>
												Loại hình
											</h3>
											<p className='text-sm font-medium text-foreground'>
												{EMPLOYMENT_TYPE_LABELS[job.employmentType]}
											</p>
										</div>
									</section>

									<section className='flex items-start gap-3'>
										<div className='bg-muted flex h-10 w-10 shrink-0 items-center justify-center rounded-full'>
											<Wrench className='h-5 w-5 text-muted-foreground' />
										</div>
										<div className='space-y-1.5 pt-0.5'>
											<h3 className='text-xs font-bold text-muted-foreground uppercase'>
												Kỹ năng
											</h3>
											{job.skills?.length > 0 ? (
												<div className='flex flex-wrap gap-x-1 gap-y-0.5 text-sm font-medium text-foreground'>
													{job.skills.map((s, i, arr) => (
														<span key={s.id}>
															{s.name}
															{i !== arr.length - 1 && (
																<span className='text-muted-foreground'>,</span>
															)}
														</span>
													))}
												</div>
											) : (
												<p className='text-sm text-muted-foreground italic'>Không yêu cầu</p>
											)}
										</div>
									</section>
								</div>
							</section>
						</div>
					</div>
				</div>

				{/* Sidebar Area */}
				<div className='flex-1 lg:col-span-1 flex flex-col gap-4 order-last'>
					<div className='top-6 border rounded-xl border-border bg-card p-6 flex flex-col items-center text-center gap-4 shadow-sm'>
						<div className='bg-white p-2 rounded-lg border border-border shadow-sm'>
							{job.companyLogoUrl ? (
								<img
									src={job.companyLogoUrl}
									alt={`${job.companyName} logo`}
									className='w-24 h-24 object-contain rounded'
								/>
							) : (
								<Briefcase className='w-24 h-24 p-4 text-muted-foreground opacity-50' />
							)}
						</div>

						<div className='space-y-1 w-full'>
							<Link
								to={RouterRoutes.COMPANY_DETAIL(job.companySlug)}
								className='text-xl font-bold hover:text-primary transition-colors block'
							>
								{job.companyName}
							</Link>
						</div>

						<Separator className='w-full my-2' />

						<div className='flex flex-col gap-3 w-full text-sm text-left'>
							<div className='flex items-start gap-2.5 text-muted-foreground'>
								<MapPin className='h-4 w-4 shrink-0 mt-0.5' />
								<p className='leading-tight'>{job.companyAddress}</p>
							</div>
							{job.categoryName && (
								<div className='flex items-center justify-start gap-1.5 text-muted-foreground text-sm'>
									<ChartBarStacked className='h-4 w-4 shrink-0' />
									<p>{job.categoryName}</p>
								</div>
							)}
						</div>
					</div>
					<div className='flex-1 top-6 border rounded-xl border-border bg-card p-6 flex flex-col gap-4 shadow-sm'>
						<h2 className='font-bold text-xl'>Việc làm tương tự</h2>
						{relatedJobs && relatedJobs.content.length > 0 ? (
							<div className='flex flex-col gap-3'>
								{relatedJobs.content.slice(0, 5).map((rj) => (
									<JobCardPublic key={rj.id} job={rj} />
								))}
							</div>
						) : (
							<p className='text-sm text-muted-foreground text-center py-4'>
								Không có việc làm tương tự.
							</p>
						)}
					</div>
				</div>
			</div>
		</div>
	);
}

/** Khu vực ứng tuyển — chỉ mount khi user là candidate */
function ApplySection({
	jobId,
	jobTitle,
	companyName,
	isJobExpired,
}: {
	jobId: string;
	jobTitle: string;
	companyName: string;
	isJobExpired: boolean;
}) {
	const { isAuthenticated } = useAuth();

	const [applyOpen, setApplyOpen] = useState(false);
	const [authDialogOpen, setAuthDialogOpen] = useState(false);
	const { data: applicationData, isLoading: checkLoading } = useApplicationByJob(jobId);
	const hasApplied = applicationData?.applied ?? false;
	const applicationId = applicationData?.applicationId ?? null;

	return (
		<>
			<div className='flex items-center gap-3'>
				{checkLoading ? (
					<Button
						disabled
						size='lg'
					>
						<Loader2 className='mr-2 h-4 w-4 animate-spin' />
						Đang kiểm tra...
					</Button>
				) : hasApplied ? (
					<>
						<Button
							disabled
							variant='secondary'
							size='lg'
							className='gap-2 cursor-not-allowed'
						>
							<CheckCircle2 className='h-4 w-4 text-gray-900' />
							Đã ứng tuyển
						</Button>
						<WithdrawButton
							applicationId={applicationId}
							jobId={jobId}
						/>
					</>
				) : (
					<Button
						variant={"primary"}
						onClick={() => (isAuthenticated ? setApplyOpen(true) : setAuthDialogOpen(true))}
						size='lg'
						disabled={isJobExpired}
					>
						<Briefcase className='mr-2 h-4 w-4' />
						Ứng tuyển ngay
					</Button>
				)}
			</div>
			{applyOpen && (
				<ApplyDialog
					isOpen={applyOpen}
					onClose={() => setApplyOpen(false)}
					jobId={jobId}
					jobTitle={jobTitle}
					companyName={companyName}
				/>
			)}
			{authDialogOpen && (
				<AuthRequiredDialog
					isOpen={authDialogOpen}
					onClose={() => setAuthDialogOpen(false)}
					message='Bạn cần đăng nhập để ứng tuyển vào vị trí này.'
				/>
			)}
		</>
	);
}

/** Nút rút đơn — chỉ dùng nội bộ trong trang này */
function WithdrawButton({ applicationId, jobId }: { applicationId: string | null; jobId: string }) {
	const toast = useToast();
	const withdrawMutation = useWithdrawApplication();

	const handleWithdraw = () => {
		if (!applicationId) {
			toast.error("Không tìm thấy đơn ứng tuyển.", { position: "bottom-right" });
			return;
		}
		if (!confirm("Bạn có chắc muốn rút đơn ứng tuyển này không?")) return;
		withdrawMutation.mutate({ id: applicationId, jobId });
	};

	return (
		<Button
			variant='outline'
			size='sm'
			onClick={handleWithdraw}
			disabled={withdrawMutation.isPending}
		>
			{withdrawMutation.isPending ? "Đang rút..." : "Rút đơn"}
		</Button>
	);
}
