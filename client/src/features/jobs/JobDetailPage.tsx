import { AuthRequiredDialog } from "@/components/shared/AuthRequiredDialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { useApplicationByJob, useWithdrawApplication } from "@/hooks/useApplications";
import { useAuth } from "@/hooks/useAuth";
import { usePublicJobDetail } from "@/hooks/usePublicJobs";
import { useToast } from "@/providers/ToastProvider";
import { EMPLOYMENT_TYPE_LABELS, EXPERIENCE_LEVEL_LABELS, LOCATION_TYPES_LABELS } from "@/types/job";
import { formatDate } from "@/utils/DateUtils";
import RouterRoutes from "@/utils/RouterRoutes";
import { formatSalary } from "@/utils/StringUtil";
import {
	Briefcase,
	Calendar,
	CheckCircle2,
	ChevronLeft,
	Clock,
	DollarSign,
	Loader2,
	MapPin,
	Users,
} from "lucide-react";
import { useState } from "react";
import { Link, useParams } from "react-router-dom";
import { ApplyDialog } from "./components/ApplyDialog";

export function JobDetailPage() {
	const { slug } = useParams<{ slug: string }>();
	const { data: job, isLoading, isError } = usePublicJobDetail(slug ?? "");

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
		<div className='container mx-auto px-4 py-8 max-w-3xl'>
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

			<div className='space-y-6'>
				{/* Header */}
				<div>
					<h1 className='text-2xl font-bold'>{job.title}</h1>
					<p className='mt-1 text-muted-foreground flex items-center gap-1.5'>
						<Briefcase className='h-4 w-4' />
						{job.companyName}
					</p>
				</div>

				{/* Meta badges */}
				<div className='flex flex-wrap gap-2'>
					<Badge variant='secondary'>{EMPLOYMENT_TYPE_LABELS[job.employmentType]}</Badge>
					<Badge variant='secondary'>{LOCATION_TYPES_LABELS[job.locationTypes]}</Badge>
					<Badge variant='outline'>{EXPERIENCE_LEVEL_LABELS[job.experienceLevel]}</Badge>
					{job.categoryName && <Badge variant='outline'>{job.categoryName}</Badge>}
				</div>

				{/* Info grid */}
				<div className='grid grid-cols-2 gap-3 text-sm'>
					<div className='flex items-center gap-2 text-green-700 font-medium'>
						<DollarSign className='h-4 w-4' />
						{formatSalary(job.salaryMin, job.salaryMax, job.currency ?? "VND")}
					</div>
					{job.location && (
						<div className='flex items-center gap-2 text-muted-foreground'>
							<MapPin className='h-4 w-4' />
							{job.location}
						</div>
					)}
					{job.numberOfOpenings && (
						<div className='flex items-center gap-2 text-muted-foreground'>
							<Users className='h-4 w-4' />
							{job.numberOfOpenings} vị trí tuyển dụng
						</div>
					)}
					{job.expirationDate && (
						<div className='flex items-center gap-2 text-muted-foreground'>
							<Calendar className='h-4 w-4' />
							Hết hạn: {formatDate(job.expirationDate)}
						</div>
					)}
					{job.postedDate && (
						<div className='flex items-center gap-2 text-muted-foreground'>
							<Clock className='h-4 w-4' />
							Đăng ngày: {formatDate(job.postedDate)}
						</div>
					)}
				</div>

				{/* Skills */}
				{job.skills && job.skills.length > 0 && (
					<div>
						<p className='text-sm font-medium mb-2'>Kỹ năng yêu cầu</p>
						<div className='flex flex-wrap gap-2'>
							{job.skills.map((s) => (
								<Badge
									key={s.id}
									variant='secondary'
								>
									{s.name}
								</Badge>
							))}
						</div>
					</div>
				)}

				<ApplySection
					jobId={job.id}
					jobTitle={job.title}
					companyName={job.companyName}
				/>

				<Separator />

				{/* Description */}
				{job.description && (
					<section>
						<h2 className='text-lg font-semibold mb-3'>Mô tả công việc</h2>
						<div className='prose prose-sm max-w-none whitespace-pre-wrap text-sm text-muted-foreground'>
							{job.description}
						</div>
					</section>
				)}

				{/* Requirements */}
				{job.requirements && (
					<section>
						<h2 className='text-lg font-semibold mb-3'>Yêu cầu ứng viên</h2>
						<div className='prose prose-sm max-w-none whitespace-pre-wrap text-sm text-muted-foreground'>
							{job.requirements}
						</div>
					</section>
				)}

				{/* Benefits */}
				{job.benefits && (
					<section>
						<h2 className='text-lg font-semibold mb-3'>Phúc lợi</h2>
						<div className='prose prose-sm max-w-none whitespace-pre-wrap text-sm text-muted-foreground'>
							{job.benefits}
						</div>
					</section>
				)}
			</div>
		</div>
	);
}

/** Khu vực ứng tuyển — chỉ mount khi user là candidate */
function ApplySection({ jobId, jobTitle, companyName }: { jobId: string; jobTitle: string; companyName: string }) {
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
							<CheckCircle2 className='h-4 w-4 text-green-600' />
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
					>
						<Briefcase className='mr-2 h-4 w-4' />
						Ứng tuyển ngay
					</Button>
				)}
			</div>
			<ApplyDialog
				isOpen={applyOpen}
				onClose={() => setApplyOpen(false)}
				jobId={jobId}
				jobTitle={jobTitle}
				companyName={companyName}
			/>
			<AuthRequiredDialog
				isOpen={authDialogOpen}
				onClose={() => setAuthDialogOpen(false)}
				message='Bạn cần đăng nhập để ứng tuyển vào vị trí này.'
			/>
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
