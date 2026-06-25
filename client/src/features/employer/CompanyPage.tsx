import { zodResolver } from "@hookform/resolvers/zod";
import {
	Building2,
	Camera,
	CheckCircle2,
	Clock,
	FileText,
	Globe,
	Loader2,
	Mail,
	MapPin,
	Pencil,
	Phone,
	RefreshCw,
	Save,
	X,
	XCircle,
} from "lucide-react";
import { useRef, useState } from "react";
import { useForm } from "react-hook-form";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCompanyStatus } from "@/hooks/useCompanyStatus";
import { useEmployerProfile, useUpdateEmployerProfile, useUploadCompanyLogo } from "@/hooks/useProfile";
import { companySchema, type CompanyFormData } from "@/lib/schemas/company";
import { useToast } from "@/providers/ToastProvider";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";

// ── Status helpers ────────────────────────────────────────────────────────────

type ApprovalStatus = "PENDING" | "APPROVED" | "REJECTED" | "SUSPENDED";

const STATUS_CONFIG: Record<ApprovalStatus, { label: string; className: string; icon: React.ReactNode }> = {
	PENDING: {
		label: "Đang chờ duyệt",
		className: "bg-amber-100 text-amber-800 border-amber-300",
		icon: <Clock className='h-3 w-3' />,
	},
	APPROVED: {
		label: "Đã được duyệt",
		className: "bg-green-100 text-green-800 border-green-300",
		icon: <CheckCircle2 className='h-3 w-3' />,
	},
	REJECTED: {
		label: "Bị từ chối",
		className: "bg-red-100 text-red-800 border-red-300",
		icon: <XCircle className='h-3 w-3' />,
	},
	SUSPENDED: {
		label: "Đã bị đình chỉ",
		className: "bg-gray-100 text-gray-700 border-gray-300",
		icon: <XCircle className='h-3 w-3' />,
	},
};

function StatusBadge({ status }: { status: string }) {
	const cfg = STATUS_CONFIG[status as ApprovalStatus] ?? {
		label: status,
		className: "bg-gray-100 text-gray-700",
		icon: null,
	};
	return (
		<Badge
			variant='outline'
			className={`inline-flex items-center gap-1 px-3 py-1 text-sm font-medium ${cfg.className}`}
		>
			{cfg.icon}
			{cfg.label}
		</Badge>
	);
}

// function ApprovalTimeline({ logs }: { logs: ApprovalLogResponse[] }) {
// 	if (logs.length === 0) {
// 		return <p className='py-6 text-center text-sm text-muted-foreground'>Chưa có lịch sử phê duyệt.</p>;
// 	}

// 	return (
// 		<ol className='relative border-l border-gray-200 pl-6'>
// 			{logs.map((log, i) => (
// 				<li
// 					key={i}
// 					className='mb-6 ml-2'
// 				>
// 					<span className='absolute -left-1.5 mt-1.5 h-3 w-3 rounded-full border border-white bg-gray-400' />
// 					<div className='flex flex-wrap items-center gap-2'>
// 						<StatusBadge status={log.newStatus} />
// 						{log.oldStatus && (
// 							<span className='text-xs text-muted-foreground'>
// 								← {STATUS_CONFIG[log.oldStatus as ApprovalStatus]?.label ?? log.oldStatus}
// 							</span>
// 						)}
// 					</div>
// 					{log.note && <p className='mt-1 text-sm text-gray-600'>{log.note}</p>}
// 					<time className='mt-1 block text-xs text-muted-foreground'>
// 						{formatDate(log.createdAt, { dateStyle: "short", timeStyle: "short" })}
// 					</time>
// 				</li>
// 			))}
// 		</ol>
// 	);
// }

// ── Main component ────────────────────────────────────────────────────────────

export default function EmployerCompanyPage() {
	const [activeTab, setActiveTab] = useState("profile");

	const { data: profile, isLoading: profileLoading } = useEmployerProfile();
	const updateProfile = useUpdateEmployerProfile();
	const uploadLogo = useUploadCompanyLogo();
	const toast = useToast();
	const logoInputRef = useRef<HTMLInputElement>(null);
	const [isEditing, setIsEditing] = useState(false);

	const {
		data: companyStatus,
		isLoading: statusLoading,
		isError: statusError,
		error: statusErrorData,
		refetch: refetchStatus,
		dataUpdatedAt,
	} = useCompanyStatus();
	// const { data: history = [], isLoading: historyLoading, refetch: refetchHistory } = useCompanyApprovalHistory();

	const {
		register,
		handleSubmit,
		formState: { errors, isSubmitting },
		reset,
	} = useForm<CompanyFormData>({
		resolver: zodResolver(companySchema),
		values: {
			companyName: profile?.companyName ?? "",
			address: profile?.address ?? "",
			description: profile?.description ?? "",
			website: profile?.website ?? "",
			companyEmail: profile?.companyEmail ?? "",
			companyPhone: profile?.companyPhone ?? "",
			taxCode: profile?.taxCode ?? "",
		},
	});

	const onSubmit = async (data: CompanyFormData) => {
		try {
			await updateProfile.mutateAsync(data);
			toast.success("Cập nhật thông tin công ty thành công");
			setIsEditing(false);
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
	};

	const handleLogoChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
		const file = e.target.files?.[0];
		if (!file) return;
		try {
			await uploadLogo.mutateAsync(file);
			toast.success("Đã cập nhật logo công ty");
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
		e.target.value = "";
	};

	const cancelEdit = () => {
		reset();
		setIsEditing(false);
	};

	const handleRefresh = () => {
		refetchStatus();
		// refetchHistory();
	};

	const renderProfileSkeleton = () => (
		<>
			<Card>
				<CardContent className='flex items-center gap-6 p-6'>
					<Skeleton className='h-24 w-24 rounded-lg' />
					<div className='space-y-2'>
						<Skeleton className='h-6 w-48' />
						<Skeleton className='h-4 w-32' />
					</div>
				</CardContent>
			</Card>
			<Card>
				<CardContent className='space-y-4 p-6'>
					<Skeleton className='h-4 w-full' />
					<Skeleton className='h-4 w-3/4' />
					<Skeleton className='h-4 w-1/2' />
				</CardContent>
			</Card>
		</>
	);

	const renderProfileContent = () => (
		<>
			<Card>
				<CardHeader>
					<CardTitle className='flex items-center gap-2'>
						<Building2 className='h-5 w-5' />
						Nhận diện thương hiệu
					</CardTitle>
				</CardHeader>
				<CardContent className='flex items-center gap-6'>
					<button
						type='button'
						onClick={() => logoInputRef.current?.click()}
						className='group relative flex h-24 w-24 shrink-0 cursor-pointer items-center justify-center overflow-hidden rounded-xl border-2 border-dashed border-muted-foreground/30 bg-muted hover:border-primary/50'
						disabled={uploadLogo.isPending}
					>
						{profile?.logoUrl ? (
							<img
								src={profile.logoUrl}
								alt='Logo công ty'
								className='h-full w-full object-contain p-1'
							/>
						) : (
							<Building2 className='h-10 w-10 text-muted-foreground' />
						)}
						<div className='absolute inset-0 flex items-center justify-center rounded-xl bg-black/40 opacity-0 transition-opacity group-hover:opacity-100'>
							{uploadLogo.isPending ? (
								<Loader2 className='h-6 w-6 animate-spin text-white' />
							) : (
								<Camera className='h-6 w-6 text-white' />
							)}
						</div>
					</button>
					<input
						ref={logoInputRef}
						type='file'
						accept='image/*'
						className='hidden'
						onChange={handleLogoChange}
					/>
					<div>
						<p className='text-xl font-semibold'>{profile?.companyName || "—"}</p>
						<p className='mt-1 text-sm text-muted-foreground'>
							Nhấn vào logo để thay đổi. PNG, JPG tối đa 5MB.
						</p>
					</div>
				</CardContent>
			</Card>

			<Card>
				<CardHeader className='flex flex-row items-center justify-between space-y-0'>
					<CardTitle>Thông tin công ty</CardTitle>

					{!isEditing && (
						<Button
							variant='outline'
							size='sm'
							onClick={() => setIsEditing(true)}
						>
							<Pencil className='h-4 w-4' />
							Chỉnh sửa
						</Button>
					)}
				</CardHeader>
				<CardContent>
					{isEditing ? (
						<form onSubmit={handleSubmit(onSubmit)}>
							<FieldGroup>
								<Field>
									<FieldLabel htmlFor='companyName'>Tên công ty</FieldLabel>
									<FieldContent>
										<Input
											id='companyName'
											placeholder='Tên công ty'
											{...register("companyName")}
										/>
									</FieldContent>
									<FieldError
										errors={errors.companyName ? [{ message: errors.companyName.message }] : []}
									/>
								</Field>

								<Field>
									<FieldLabel htmlFor='address'>Địa chỉ</FieldLabel>
									<FieldContent>
										<Input
											id='address'
											placeholder='Địa chỉ công ty'
											{...register("address")}
										/>
									</FieldContent>
									<FieldError errors={errors.address ? [{ message: errors.address.message }] : []} />
								</Field>

								<Field>
									<FieldLabel htmlFor='website'>Website</FieldLabel>
									<FieldContent>
										<Input
											id='website'
											placeholder='https://company.com'
											{...register("website")}
										/>
									</FieldContent>
									<FieldError errors={errors.website ? [{ message: errors.website.message }] : []} />
								</Field>

								<Field>
									<FieldLabel htmlFor='companyEmail'>Email công ty</FieldLabel>
									<FieldContent>
										<Input
											id='companyEmail'
											placeholder='contact@company.com'
											{...register("companyEmail")}
										/>
									</FieldContent>
									<FieldError
										errors={errors.companyEmail ? [{ message: errors.companyEmail.message }] : []}
									/>
								</Field>

								<Field>
									<FieldLabel htmlFor='companyPhone'>Số điện thoại công ty</FieldLabel>
									<FieldContent>
										<Input
											id='companyPhone'
											placeholder='02812345678'
											{...register("companyPhone")}
										/>
									</FieldContent>
									<FieldError
										errors={errors.companyPhone ? [{ message: errors.companyPhone.message }] : []}
									/>
								</Field>

								<Field>
									<FieldLabel htmlFor='taxCode'>Mã số thuế</FieldLabel>
									<FieldContent>
										<Input
											id='taxCode'
											placeholder='Mã số thuế'
											{...register("taxCode")}
										/>
									</FieldContent>
									<FieldError errors={errors.taxCode ? [{ message: errors.taxCode.message }] : []} />
								</Field>

								<Field>
									<FieldLabel htmlFor='description'>Mô tả công ty</FieldLabel>
									<FieldContent>
										<textarea
											id='description'
											placeholder='Mô tả về công ty...'
											rows={4}
											className='w-full rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
											{...register("description")}
										/>
									</FieldContent>
								</Field>
							</FieldGroup>

							<div className='mt-6 flex gap-3'>
								<Button
									type='submit'
									variant='primary'
									disabled={isSubmitting || updateProfile.isPending}
								>
									{isSubmitting || updateProfile.isPending ? (
										<Loader2
											key='loader'
											className='h-4 w-4 animate-spin'
										/>
									) : (
										<Save
											key='save'
											className='h-4 w-4'
										/>
									)}
									Lưu thay đổi
								</Button>
								<Button
									type='button'
									variant='outline'
									onClick={cancelEdit}
								>
									<X className='h-4 w-4' />
									Huỷ
								</Button>
							</div>
						</form>
					) : (
						<div className='space-y-4'>
							<div className='flex items-start gap-3'>
								<Building2 className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div className='flex flex-row items-center gap-4'>
									<div>
										<p className='text-sm text-muted-foreground'>Tên công ty</p>
										<p className='font-medium'>{profile?.companyName || "—"}</p>
									</div>
									<StatusBadge status={companyStatus?.approvalStatus || "PENDING"} />
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<MapPin className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Địa chỉ</p>
									<p className='font-medium'>{profile?.address || "—"}</p>
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<Globe className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Website</p>
									{profile?.website ? (
										<a
											href={profile.website}
											target='_blank'
											rel='noopener noreferrer'
											className='font-medium text-primary hover:underline'
										>
											{profile.website}
										</a>
									) : (
										<p className='font-medium'>—</p>
									)}
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<Mail className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Email công ty</p>
									<p className='font-medium'>{profile?.companyEmail || "—"}</p>
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<Phone className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Số điện thoại công ty</p>
									<p className='font-medium'>{profile?.companyPhone || "—"}</p>
								</div>
							</div>
							<div className='flex items-start gap-3'>
								<FileText className='mt-0.5 h-4 w-4 shrink-0 text-muted-foreground' />
								<div>
									<p className='text-sm text-muted-foreground'>Mã số thuế</p>
									<p className='font-medium'>{profile?.taxCode || "—"}</p>
								</div>
							</div>
							<div>
								<p className='text-sm text-muted-foreground'>Mô tả</p>
								<p className='mt-1 whitespace-pre-line font-medium'>{profile?.description || "—"}</p>
							</div>
						</div>
					)}
				</CardContent>
			</Card>
		</>
	);

	const renderStatusContent = () => {
		if (statusLoading) {
			return (
				<div className='space-y-4'>
					<Skeleton className='h-7 w-48' />
					<Skeleton className='h-5 w-32' />
					<Skeleton className='h-24 w-full' />
					<Skeleton className='h-16 w-full' />
				</div>
			);
		}

		if (statusError) {
			return (
				<div className='rounded-lg border border-red-200 bg-red-50 p-4 text-red-700'>
					<p className='font-medium'>Không thể tải dữ liệu</p>
					<p className='mt-1 text-sm'>{getErrorMessage(statusErrorData)}</p>
					<Button
						variant='outline'
						size='sm'
						className='mt-3'
						onClick={handleRefresh}
					>
						Thử lại
					</Button>
				</div>
			);
		}

		if (!companyStatus) return null;

		const status = companyStatus.approvalStatus as ApprovalStatus;

		const alertContent: Record<ApprovalStatus, React.ReactNode> = {
			PENDING: (
				<p className='text-sm text-amber-800'>
					Hồ sơ đang được xét duyệt, thường mất <strong>1–3 ngày làm việc</strong>.
				</p>
			),
			APPROVED: (
				<p className='text-sm text-green-800'>
					Công ty đã được phê duyệt. Bạn có thể <strong>đăng tuyển ngay</strong>.
				</p>
			),
			REJECTED: (
				<p className='text-sm text-red-800'>
					Hồ sơ bị từ chối.{" "}
					{companyStatus.reviewNote && (
						<>
							Lý do: <strong>{companyStatus.reviewNote}</strong>.{" "}
						</>
					)}
					Vui lòng cập nhật và gửi lại.
				</p>
			),
			SUSPENDED: (
				<p className='text-sm text-gray-700'>
					Tài khoản công ty đã bị đình chỉ. Vui lòng liên hệ quản trị viên để biết thêm chi tiết.
				</p>
			),
		};

		const alertBg: Record<ApprovalStatus, string> = {
			PENDING: "bg-amber-50 border-amber-200",
			APPROVED: "bg-green-50 border-green-200",
			REJECTED: "bg-red-50 border-red-200",
			SUSPENDED: "bg-gray-50 border-gray-200",
		};

		return (
			<div className='space-y-6'>
				{/* Status card */}
				<Card>
					<CardHeader className='flex flex-row items-center justify-between space-y-0'>
						<CardTitle className='text-base'>Trạng thái phê duyệt</CardTitle>
						<Button
							variant='outline'
							size='sm'
							onClick={handleRefresh}
							// disabled={historyLoading}
							className='gap-1.5'
						>
							<RefreshCw className='h-4 w-4' />
							Làm mới
						</Button>
					</CardHeader>
					<CardContent className='space-y-4'>
						<StatusBadge status={status} />

						<div className={`rounded-lg border p-3 ${alertBg[status]}`}>{alertContent[status]}</div>

						<div className='grid grid-cols-2 gap-4 text-sm'>
							<div>
								<p className='text-muted-foreground'>Ngày gửi hồ sơ</p>
								<p className='font-medium'>{formatDate(companyStatus.submittedAt)}</p>
							</div>
							<div>
								<p className='text-muted-foreground'>Ngày duyệt</p>
								<p className='font-medium'>{formatDate(companyStatus.reviewedAt)}</p>
							</div>
						</div>
					</CardContent>
				</Card>

				{dataUpdatedAt && (
					<p className='text-right text-xs text-muted-foreground'>
						Cập nhật lần cuối:{" "}
						{formatDate(new Date(dataUpdatedAt), { dateStyle: "short", timeStyle: "short" })}
					</p>
				)}

				<Separator />

				{/* History card */}
				{/* <Card>
					<CardHeader>
						<CardTitle className='text-base'>Lịch sử phê duyệt</CardTitle>
					</CardHeader>
					<CardContent>
						{historyLoading ? (
							<div className='space-y-3'>
								<Skeleton className='h-10 w-full' />
								<Skeleton className='h-10 w-full' />
							</div>
						) : (
							<ApprovalTimeline logs={history} />
						)}
					</CardContent>
				</Card> */}
			</div>
		);
	};

	return (
		<div className='mx-auto max-w-3xl space-y-6 p-6'>
			<Tabs
				value={activeTab}
				onValueChange={setActiveTab}
			>
				<TabsList className='w-full'>
					<TabsTrigger
						value='profile'
						className='flex-1'
					>
						Hồ sơ công ty
					</TabsTrigger>
					<TabsTrigger
						value='status'
						className='flex-1'
					>
						Trạng thái duyệt
					</TabsTrigger>
				</TabsList>

				<TabsContent
					value='profile'
					className='space-y-6 pt-4'
				>
					{profileLoading ? renderProfileSkeleton() : renderProfileContent()}
				</TabsContent>

				<TabsContent
					value='status'
					className='space-y-6 pt-4'
				>
					{renderStatusContent()}
				</TabsContent>
			</Tabs>
		</div>
	);
}
