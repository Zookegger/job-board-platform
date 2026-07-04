import { BaseDialog } from "@/components/shared/BaseDialog";
import UserAvatar from "@/components/shared/UserAvatar";
import { Badge } from "@/components/ui/badge";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { cn } from "@/lib/utils";
import type { UserFullJobPosting, UserFullResponse } from "@/types/admin";
import { UserRole } from "@/types/auth";
import { formatDate } from "@/utils/DateUtils";
import { formatFileSize } from "@/utils/FileUtils";
import { Briefcase, Building2, Calendar, FileText, Mail, Phone } from "lucide-react";

interface UserDetailModalProps {
	user: UserFullResponse | null;
	isOpen: boolean;
	onClose: () => void;
}

function RoleBadge({ role }: { role: UserRole }) {
	const styles: Record<UserRole, string> = {
		ADMIN: "border-purple-500/40 bg-purple-500/10 text-purple-700",
		EMPLOYER: "border-blue-500/40 bg-blue-500/10 text-blue-700",
		CANDIDATE: "border-emerald-500/40 bg-emerald-500/10 text-emerald-700",
	};

	return (
		<Badge
			variant='outline'
			className={cn("", styles[role])}
		>
			{role === UserRole.ADMIN
				? "Quản trị viên"
				: role === UserRole.EMPLOYER
					? "Nhà tuyển dụng"
					: "Người tìm việc"}
		</Badge>
	);
}

function StatusBadge({ isActive }: { isActive: boolean }) {
	return (
		<Badge
			variant='outline'
			className='border-emerald-500/40 bg-emerald-500/10 text-emerald-700'
		>
			{isActive ? "Đang hoạt động" : "Đã khóa"}
		</Badge>
	);
}

export function UserDetailModal({ user, isOpen, onClose }: UserDetailModalProps) {
	const hasCandidateInfo =
		!!user &&
		((user.skills && user.skills.length > 0) || user.resume || (user.applications && user.applications.length > 0));
	const hasEmployerInfo =
		!!user && (user.company || (user.jobPostings && user.jobPostings.length > 0) || user.hiringActivity);

	return (
		<BaseDialog
			isOpen={isOpen}
			onClose={onClose}
			title={user ? "ID: " + user.id : "Chi tiết người dùng"}
			description=""
			size='2xl'
		>
			{!user && <p className='text-sm text-muted-foreground'>Không có dữ liệu.</p>}

			{user && (
				<div className='space-y-6'>
					{/* Header */}
					<div className='flex items-start gap-4'>
						<UserAvatar
							fullName={user.fullName ?? user.email}
							avatarUrl={user.avatarUrl}
							size='lg'
						/>

						<div className='min-w-0 flex-1 space-y-1'>
							<div className='flex flex-col flex-wrap items-start gap-2'>
								<h3 className='text-lg font-semibold truncate'>{user.fullName ?? "Chưa có tên"}</h3>
								<div className="gap-2 flex flex-wrap">
									<RoleBadge role={user.role} />
									<StatusBadge isActive={user.isActive} />
								</div>
							</div>
						</div>
					</div>

					<Separator />

					<Tabs defaultValue='overview'>
						<TabsList variant='line'>
							<TabsTrigger value='overview'>Tổng quan</TabsTrigger>
							{hasCandidateInfo && <TabsTrigger value='candidate'>Ứng viên</TabsTrigger>}
							{hasEmployerInfo && <TabsTrigger value='employer'>Nhà tuyển dụng</TabsTrigger>}
						</TabsList>

						{/* Overview */}
						<TabsContent
							value='overview'
							className='space-y-4 pt-4'
						>
							<dl className='grid grid-cols-2 gap-4 text-sm'>
								<div>
									<dt className='text-muted-foreground'>Email</dt>
									<dd className='flex items-center gap-1.5 truncate'>
										<Mail className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
										{user.email}
									</dd>
								</div>
								<div>
									<dt className='text-muted-foreground'>Số điện thoại</dt>
									<dd className='flex items-center gap-1.5'>
										<Phone className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
										{user.phone ?? "—"}
									</dd>
								</div>
								<div>
									<dt className='text-muted-foreground'>Ngày tham gia</dt>
									<dd className='flex items-center gap-1.5'>
										<Calendar className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
										{formatDate(user.createdAt)}
									</dd>
								</div>
								<div>
									<dt className='text-muted-foreground'>Cập nhật lần cuối</dt>
									<dd className='flex items-center gap-1.5'>
										<Calendar className='h-3.5 w-3.5 shrink-0 text-muted-foreground' />
										{formatDate(user.updatedAt)}
									</dd>
								</div>
								{user.roleInCompany && (
									<div>
										<dt className='text-muted-foreground'>Vai trò trong công ty</dt>
										<dd>{user.roleInCompany}</dd>
									</div>
								)}
							</dl>
						</TabsContent>

						{/* Candidate */}
						{hasCandidateInfo && (
							<TabsContent
								value='candidate'
								className='space-y-5 pt-4'
							>
								{user.skills && user.skills.length > 0 && (
									<div>
										<h4 className='mb-2 text-sm font-medium'>Kỹ năng</h4>
										<div className='flex flex-wrap gap-2'>
											{user.skills.map((skill) => (
												<Badge
													key={skill.skillId}
													variant='secondary'
												>
													{skill.skillName}
												</Badge>
											))}
										</div>
									</div>
								)}

								{user.resume && (
									<div>
										<h4 className='mb-2 text-sm font-medium'>CV</h4>
										<div className='flex items-center gap-2 rounded-md border p-3 text-sm'>
											<FileText className='h-4 w-4 shrink-0 text-muted-foreground' />
											<div className='min-w-0 flex-1'>
												<p className='truncate font-medium'>{user.resume.title}</p>
												<p className='truncate text-xs text-muted-foreground'>
													{user.resume.originalFileName} ·{" "}
													{formatFileSize(user.resume.fileSize, "MB")} ·{" "}
													{user.resume.fileType}
												</p>
											</div>
										</div>
									</div>
								)}

								{user.applications && user.applications.length > 0 && (
									<div>
										<h4 className='mb-2 text-sm font-medium'>
											Đơn ứng tuyển ({user.applications.length})
										</h4>
										<div className='max-h-56 space-y-2 overflow-y-auto pr-1'>
											{user.applications.map((app) => (
												<div
													key={app.id}
													className='flex items-center justify-between rounded-md border p-2.5 text-sm'
												>
													<div className='min-w-0'>
														<p className='truncate font-medium'>{app.jobTitle}</p>
														<p className='truncate text-xs text-muted-foreground'>
															{app.companyName}
														</p>
													</div>
													<Badge variant='outline'>{app.status}</Badge>
												</div>
											))}
										</div>
									</div>
								)}
							</TabsContent>
						)}

						{/* Employer */}
						{hasEmployerInfo && (
							<TabsContent
								value='employer'
								className='space-y-5 pt-4'
							>
								{user.company && (
									<div className='flex items-center gap-3 rounded-md border p-3'>
										<Building2 className='h-8 w-8 shrink-0 text-muted-foreground' />
										<div className='min-w-0'>
											<p className='truncate font-medium'>{user.company.companyName}</p>
											{user.roleInCompany && (
												<p className='text-xs text-muted-foreground'>{user.roleInCompany}</p>
											)}
										</div>
									</div>
								)}

								{user.hiringActivity && (
									<div>
										<h4 className='mb-2 text-sm font-medium'>Hoạt động tuyển dụng</h4>
										<div className='grid grid-cols-3 gap-2 text-center sm:grid-cols-6'>
											{[
												{ label: "Tổng", value: user.hiringActivity.totalApplications },
												{ label: "Chờ xử lý", value: user.hiringActivity.pendingApplications },
												{
													label: "Đang xem xét",
													value: user.hiringActivity.reviewingApplications,
												},
												{
													label: "Phỏng vấn",
													value: user.hiringActivity.interviewApplications,
												},
												{ label: "Đã tuyển", value: user.hiringActivity.hiredApplications },
												{ label: "Từ chối", value: user.hiringActivity.rejectedApplications },
											].map((stat) => (
												<div
													key={stat.label}
													className='rounded-md border p-2'
												>
													<p className='text-lg font-semibold'>{stat.value}</p>
													<p className='text-[11px] text-muted-foreground'>{stat.label}</p>
												</div>
											))}
										</div>
									</div>
								)}

								{user.jobPostings && user.jobPostings.length > 0 && (
									<div>
										<h4 className='mb-2 text-sm font-medium'>
											Tin tuyển dụng ({user.jobPostings.length})
										</h4>
										<div className='max-h-56 space-y-2 overflow-y-auto pr-1'>
											{user.jobPostings.map((job: UserFullJobPosting) => (
												<div
													key={job.id}
													className='flex items-center justify-between rounded-md border p-2.5 text-sm'
												>
													<div className='min-w-0 flex items-center gap-2'>
														<Briefcase className='h-4 w-4 shrink-0 text-muted-foreground' />
														<div className='min-w-0'>
															<p className='truncate font-medium'>{job.title}</p>
															<p className='truncate text-xs text-muted-foreground'>
																{job.categoryName}
																{job.location ? ` · ${job.location}` : ""}
															</p>
														</div>
													</div>
													<Badge variant='outline'>{job.status}</Badge>
												</div>
											))}
										</div>
									</div>
								)}
							</TabsContent>
						)}
					</Tabs>
				</div>
			)}
		</BaseDialog>
	);
}
