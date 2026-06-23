import { zodResolver } from "@hookform/resolvers/zod";
import { useQueryClient } from "@tanstack/react-query";
import {
	Briefcase,
	Building2,
	Calendar,
	Clock,
	Coins,
	Loader2,
	MapPin,
	Pencil,
	Save,
	Search,
	SendHorizonal,
	Trash2,
	X,
} from "lucide-react";
import { useCallback, useEffect, useMemo, useState } from "react";
import { Controller, useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";

import { BaseDialog } from "@/components/shared/BaseDialog";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogFooter, DialogHeader, DialogTitle } from "@/components/ui/dialog";
import { Field, FieldContent, FieldError, FieldGroup, FieldLabel } from "@/components/ui/field";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { useCategories } from "@/hooks/useCategories";
import {
	EMPLOYER_JOB_KEY,
	useCreateEmployerJob,
	useDeleteEmployerJob,
	useEmployerJobDetail,
	useSubmitForReview,
	useUpdateEmployerJob,
} from "@/hooks/useEmployerJobs";
import { useAllSkills } from "@/hooks/useSkills";
import { jobSchema, type JobFormData } from "@/lib/schemas/job";
import type { CategoryResponse, JobRequest, JobResponse, JobStatus, SkillResponse } from "@/types/job";
import { EMPLOYMENT_TYPE_LABELS, EXPERIENCE_LEVEL_LABELS, JOB_STATUS_LABELS, LOCATION_TYPES_LABELS } from "@/types/job";
import { formatDate } from "@/utils/DateUtils";
import getErrorMessage from "@/utils/getErrorMessage";
import { formatSalaryDisplay } from "@/utils/StringUtil";

// ── Constants & helpers ──────────────────────────────────────────────────

const DEFAULT_VALUES: JobFormData = {
	title: "",
	description: "",
	requirements: "",
	benefits: "",
	categoryId: 0,
	numberOfOpenings: 1,
	salaryMin: null,
	salaryMax: null,
	currency: "VND",
	location: "",
	locationTypes: "ONSITE",
	employmentType: "FULL_TIME",
	experienceLevel: "JUNIOR",
	skillIds: [],
};

function toFormData(job: JobResponse): JobFormData {
	return {
		title: job.title,
		description: job.description,
		requirements: job.requirements ?? "",
		benefits: job.benefits ?? "",
		categoryId: job.categoryId,
		numberOfOpenings: job.numberOfOpenings ?? 1,
		salaryMin: job.salaryMin ?? null,
		salaryMax: job.salaryMax ?? null,
		currency: job.currency ?? "VND",
		location: job.location ?? "",
		locationTypes: job.locationTypes,
		employmentType: job.employmentType,
		experienceLevel: job.experienceLevel,
		skillIds: job.skills?.map((s) => s.id) ?? [],
	};
}

function toRequest(data: JobFormData): JobRequest {
	return {
		...data,
		requirements: data.requirements || null,
		benefits: data.benefits || null,
		location: data.location || null,
		numberOfOpenings: data.numberOfOpenings ?? 1,
		salaryMin: data.salaryMin ?? null,
		salaryMax: data.salaryMax ?? null,
		skillIds: !data.skillIds ? null : data.skillIds.length > 0 ? data.skillIds : null,
	};
}

function formatSalary(min: number | null, max: number | null, currency: string): string {
	if (!min && !max) return "Thương lượng";
	const fmt = (v: number) =>
		new Intl.NumberFormat("vi-VN", {
			style: "currency",
			currency: currency || "VND",
			maximumFractionDigits: 0,
		}).format(v);
	if (min && max) return `${fmt(min)} – ${fmt(max)}`;
	if (min) return `Từ ${fmt(min)}`;
	return `Đến ${fmt(max!)}`;
}

// ── CurrencyInput ────────────────────────────────────────────────────────

function CurrencyInput({
	value,
	onChange,
	id,
	placeholder,
}: {
	value: number | null | undefined;
	onChange: (value: number | null) => void;
	id?: string;
	placeholder?: string;
}) {
	const displayValue = value != null && !Number.isNaN(value) ? formatSalaryDisplay(value, "VND") : "";

	const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const digits = e.target.value.replace(/[^\d]/g, "");
		onChange(digits === "" ? null : Number(digits));
	};

	return (
		<div className='relative'>
			<input
				id={id}
				value={displayValue}
				onChange={handleChange}
				placeholder={placeholder}
				className='h-10 w-full rounded-md border border-input bg-background px-3 pr-12 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
			/>
			<span className='pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-sm text-muted-foreground'>
				VND
			</span>
		</div>
	);
}

// ── JobStatusBadge ───────────────────────────────────────────────────────

function JobStatusBadge({ status }: { status: JobStatus }) {
	const label = JOB_STATUS_LABELS[status];
	switch (status) {
		case "DRAFT":
			return (
				<Badge
					className='py-3'
					variant='secondary'
				>
					{label}
				</Badge>
			);
		case "PENDING_APPROVAL":
			return (
				<Badge
					variant='outline'
					className='border-warning/40 bg-warning/20 text-warning-foreground py-3'
				>
					{label}
				</Badge>
			);
		case "ACTIVE":
			return (
				<Badge
					variant='outline'
					className='border-success/80 bg-success/20 text-success font-semibold py-3'
				>
					{label}
				</Badge>
			);
		case "EXPIRED":
			return (
				<Badge
					variant='outline'
					className='py-3'
				>
					{label}
				</Badge>
			);
		case "REJECTED":
			return (
				<Badge
					variant='destructive'
					className='py-3'
				>
					{label}
				</Badge>
			);
	}
}

// ── ViewMode ─────────────────────────────────────────────────────────────

interface ViewModeProps {
	job: JobResponse;
	actionPending: boolean;
	onEdit: () => void;
	onSubmit: () => void;
	onUnpublish: () => void;
	onDelete: () => void;
	onClose: () => void;
}

function ViewMode({ job, actionPending, onEdit, onSubmit, onUnpublish, onDelete, onClose }: ViewModeProps) {
	return (
		<>
			<DialogHeader className='shrink-0 border-b px-6 pb-4 pt-6'>
				<div className='flex items-start gap-3'>
					<div className='min-w-0'>
						<h2 className='text-lg leading-tight font-heading font-medium text-foreground'>{job.title}</h2>
						<div className='mt-1 flex items-center gap-1.5 text-sm text-muted-foreground'>
							<Building2 className='size-4 shrink-0' />
							<span className='line-clamp-1'>{job.companyName}</span>
						</div>
					</div>
					<div className='shrink-0'>
						<JobStatusBadge status={job.status} />
					</div>
				</div>
			</DialogHeader>

			<div className='flex-1 space-y-5 overflow-y-auto px-6 py-4'>
				<div className='flex flex-wrap gap-2'>
					<Badge
						variant='outline'
						className='py-3'
					>
						{EMPLOYMENT_TYPE_LABELS[job.employmentType]}
					</Badge>
					<Badge
						variant='outline'
						className='py-3'
					>
						{EXPERIENCE_LEVEL_LABELS[job.experienceLevel]}
					</Badge>
					<Badge
						variant='secondary'
						className='py-3'
					>
						{LOCATION_TYPES_LABELS[job.locationTypes]}
					</Badge>
					{job.categoryName && (
						<Badge
							variant='secondary'
							className='py-3'
						>
							{job.categoryName}
						</Badge>
					)}
				</div>

				<div className='grid grid-cols-2 gap-4 text-sm'>
					<div>
						<p className='mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
							Địa điểm
						</p>
						<div className='flex items-center gap-1.5'>
							<MapPin className='size-4 shrink-0 text-muted-foreground' />
							<span>{job.location ?? "Chưa rõ"}</span>
						</div>
					</div>
					<div>
						<p className='mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
							Mức lương
						</p>
						<div className='flex items-center gap-1.5'>
							<Coins className='size-4 shrink-0 text-muted-foreground' />
							<span>{formatSalary(job.salaryMin, job.salaryMax, job.currency ?? "VND")}</span>
						</div>
					</div>
				</div>

				<div className='grid grid-cols-2 gap-4 text-sm'>
					<div>
						<p className='mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
							Số lượng tuyển
						</p>
						<span>{job.numberOfOpenings ?? 1} người</span>
					</div>
					<div>
						<p className='mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
							Ngày tạo
						</p>
						<div className='flex items-center gap-1.5'>
							<Calendar className='size-4 shrink-0 text-muted-foreground' />
							<span>{formatDate(job.createdAt)}</span>
						</div>
					</div>
					<div>
						<p className='mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
							Ngày hết hạn
						</p>
						<div className='flex items-center gap-1.5'>
							<Clock className='size-4 shrink-0 text-muted-foreground' />
							<span>{job.expirationDate ? formatDate(job.expirationDate) : "Không có"}</span>
						</div>
					</div>
					<div>
						<p className='mb-1 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
							Cập nhật lần cuối
						</p>
						<span>{formatDate(job.updatedAt)}</span>
					</div>
				</div>

				<Separator />

				{job.description && (
					<div>
						<p className='mb-2 text-sm font-semibold'>Mô tả công việc</p>
						<p className='whitespace-pre-wrap text-sm text-muted-foreground'>{job.description}</p>
					</div>
				)}
				{job.requirements && (
					<div>
						<p className='mb-2 text-sm font-semibold'>Yêu cầu ứng viên</p>
						<p className='whitespace-pre-wrap text-sm text-muted-foreground'>{job.requirements}</p>
					</div>
				)}
				{job.benefits && (
					<div>
						<p className='mb-2 text-sm font-semibold'>Quyền lợi</p>
						<p className='whitespace-pre-wrap text-sm text-muted-foreground'>{job.benefits}</p>
					</div>
				)}
				{(job.skills?.length ?? 0) > 0 && (
					<div>
						<p className='mb-2 text-sm font-semibold'>Kỹ năng yêu cầu</p>
						<div className='flex flex-wrap gap-1.5'>
							{job.skills?.map((skill) => (
								<Badge
									key={skill.id}
									variant='secondary'
								>
									{skill.name}
								</Badge>
							))}
						</div>
					</div>
				)}
			</div>

			<DialogFooter className='shrink-0 border-t px-6 py-4'>
				<div className='flex w-full items-center justify-between'>
					<div className='flex gap-2'>
						<Button
							variant='destructive'
							size='sm'
							disabled={actionPending}
							onClick={onDelete}
						>
							<Trash2 /> Xóa
						</Button>
						{(job.status === "ACTIVE" || job.status === "PENDING_APPROVAL") && (
							<Button
								variant='outline'
								size='sm'
								disabled={actionPending}
								onClick={onUnpublish}
							>
								{job.status === "PENDING_APPROVAL" ? "Thu hồi" : "Gỡ đăng"}
							</Button>
						)}
						<Button
							variant='primary'
							size='sm'
							onClick={onEdit}
						>
							<Pencil /> Chỉnh sửa
						</Button>
					</div>
					<div className='flex gap-2'>
						<Button
							variant='outline'
							onClick={onClose}
						>
							<X /> Đóng
						</Button>
						{job.status === "DRAFT" && (
							<Button
								variant='outline'
								onClick={onSubmit}
								disabled={actionPending}
							>
								<SendHorizonal /> Gửi duyệt
							</Button>
						)}
					</div>
				</div>
			</DialogFooter>
		</>
	);
}

// ── FormMode ─────────────────────────────────────────────────────────────

interface FormModeProps {
	isCreate: boolean;
	formId: string;
	register: ReturnType<typeof useForm<JobFormData>>["register"];
	errors: ReturnType<typeof useForm<JobFormData>>["formState"]["errors"];
	control: ReturnType<typeof useForm<JobFormData>>["control"];
	setValue: ReturnType<typeof useForm<JobFormData>>["setValue"];
	isSubmitting: boolean;
	onSubmit: React.SubmitEventHandler<HTMLFormElement>;
	onCancel: () => void;
	categories?: CategoryResponse[];
	allSkills?: SkillResponse[];
	actionPending: boolean;
}

function FormMode({
	isCreate,
	formId,
	register,
	errors,
	control,
	setValue,
	isSubmitting,
	onSubmit,
	onCancel,
	categories,
	allSkills,
	actionPending,
}: FormModeProps) {
	const watchSkillIds = useWatch({ name: "skillIds", control }) ?? [];
	const [skillSearch, setSkillSearch] = useState("");

	const filteredSkills = useMemo(() => {
		if (!allSkills) return [];
		if (!skillSearch.trim()) return allSkills;
		const q = skillSearch.trim().toLowerCase();
		return allSkills.filter((s) => s.name.toLowerCase().includes(q));
	}, [allSkills, skillSearch]);

	return (
		<>
			<DialogHeader className='shrink-0 border-b px-6 pb-4 pt-6'>
				<h2 className='text-lg font-heading font-medium text-foreground'>
					{isCreate ? "Tạo tin tuyển dụng" : "Chỉnh sửa tin tuyển dụng"}
				</h2>
			</DialogHeader>

			<div className='flex-1 overflow-y-auto px-6 py-4'>
				<form
					id={formId}
					onSubmit={onSubmit}
				>
					<FieldGroup>
						<Field>
							<FieldLabel htmlFor='title'>Tiêu đề *</FieldLabel>
							<FieldContent>
								<Input
									id='title'
									className='h-10'
									placeholder='Nhập tiêu đề việc làm'
									{...register("title")}
								/>
							</FieldContent>
							<FieldError errors={errors.title ? [{ message: errors.title.message as string }] : []} />
						</Field>

						<div className='grid grid-cols-3 gap-4'>
							<Field>
								<FieldLabel htmlFor='categoryId'>Ngành nghề *</FieldLabel>
								<FieldContent>
									<select
										id='categoryId'
										{...register("categoryId", { valueAsNumber: true })}
										className='h-10 w-full rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
									>
										<option
											value={0}
											disabled
										>
											Chọn ngành nghề
										</option>
										{categories?.map((cat) => (
											<option
												key={cat.id}
												value={cat.id}
											>
												{cat.name}
											</option>
										))}
									</select>
								</FieldContent>
								<FieldError
									errors={errors.categoryId ? [{ message: errors.categoryId.message as string }] : []}
								/>
							</Field>

							<Field>
								<FieldLabel htmlFor='employmentType'>Loại hình *</FieldLabel>
								<FieldContent>
									<select
										id='employmentType'
										{...register("employmentType")}
										className='h-10 w-full rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
									>
										<option value='FULL_TIME'>Toàn thời gian</option>
										<option value='PART_TIME'>Bán thời gian</option>
										<option value='CONTRACT'>Hợp đồng</option>
										<option value='INTERNSHIP'>Thực tập</option>
									</select>
								</FieldContent>
								<FieldError
									errors={
										errors.employmentType
											? [{ message: errors.employmentType.message as string }]
											: []
									}
								/>
							</Field>

							<Field>
								<FieldLabel htmlFor='experienceLevel'>Kinh nghiệm *</FieldLabel>
								<FieldContent>
									<select
										id='experienceLevel'
										{...register("experienceLevel")}
										className='h-10 w-full rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
									>
										<option value='INTERN'>Thực tập sinh</option>
										<option value='JUNIOR'>Junior</option>
										<option value='MID'>Mid-level</option>
										<option value='SENIOR'>Senior</option>
										<option value='LEAD'>Lead / Manager</option>
									</select>
								</FieldContent>
								<FieldError
									errors={
										errors.experienceLevel
											? [{ message: errors.experienceLevel.message as string }]
											: []
									}
								/>
							</Field>
						</div>

						<div className='grid grid-cols-2 gap-4'>
							<Field>
								<FieldLabel htmlFor='locationTypes'>Hình thức làm việc *</FieldLabel>
								<FieldContent>
									<select
										id='locationTypes'
										{...register("locationTypes")}
										className='h-10 w-full rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
									>
										<option value='ONSITE'>Tại văn phòng</option>
										<option value='REMOTE'>Remote</option>
										<option value='HYBRID'>Hybrid</option>
									</select>
								</FieldContent>
								<FieldError
									errors={
										errors.locationTypes
											? [{ message: errors.locationTypes.message as string }]
											: []
									}
								/>
							</Field>

							<Field>
								<FieldLabel htmlFor='location'>Địa điểm</FieldLabel>
								<FieldContent>
									<Input
										className='h-10'
										id='location'
										placeholder='VD: Quận 1, TP. Hồ Chí Minh'
										{...register("location")}
									/>
								</FieldContent>
							</Field>
						</div>

						<div className='grid grid-cols-2 gap-4'>
							<Field>
								<FieldLabel htmlFor='numberOfOpenings'>Số lượng tuyển</FieldLabel>
								<FieldContent>
									<Input
										className='h-10'
										id='numberOfOpenings'
										type='number'
										min={1}
										{...register("numberOfOpenings", { valueAsNumber: true })}
									/>
								</FieldContent>
								<FieldError
									errors={
										errors.numberOfOpenings
											? [{ message: errors.numberOfOpenings.message as string }]
											: []
									}
								/>
							</Field>

							<Field>
								<FieldLabel htmlFor='currency'>Đơn vị tiền tệ</FieldLabel>
								<FieldContent>
									<select
										id='currency'
										{...register("currency")}
										className='h-10 w-full rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
									>
										<option value='VND'>VND</option>
										<option value='USD'>USD</option>
									</select>
								</FieldContent>
							</Field>
						</div>

						<div className='grid grid-cols-2 gap-4'>
							<Field>
								<FieldLabel htmlFor='salaryMin'>Lương tối thiểu</FieldLabel>
								<FieldContent>
									<Controller
										name='salaryMin'
										control={control}
										render={({ field }) => (
											<CurrencyInput
												id='salaryMin'
												value={field.value}
												onChange={field.onChange}
												placeholder='VD: 10.000.000'
											/>
										)}
									/>
								</FieldContent>
								<FieldError
									errors={errors.salaryMin ? [{ message: errors.salaryMin.message as string }] : []}
								/>
							</Field>

							<Field>
								<FieldLabel htmlFor='salaryMax'>Lương tối đa</FieldLabel>
								<FieldContent>
									<Controller
										name='salaryMax'
										control={control}
										render={({ field }) => (
											<CurrencyInput
												id='salaryMax'
												value={field.value}
												onChange={field.onChange}
												placeholder='VD: 30.000.000'
											/>
										)}
									/>
								</FieldContent>
								<FieldError
									errors={errors.salaryMax ? [{ message: errors.salaryMax.message as string }] : []}
								/>
							</Field>
						</div>

						<Field>
							<FieldLabel htmlFor='description'>Mô tả công việc *</FieldLabel>
							<FieldContent>
								<textarea
									id='description'
									rows={6}
									placeholder='Nhập mô tả chi tiết về công việc...'
									{...register("description")}
									className='w-full rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
								/>
							</FieldContent>
							<FieldError
								errors={errors.description ? [{ message: errors.description.message as string }] : []}
							/>
						</Field>

						<Field>
							<FieldLabel htmlFor='requirements'>Yêu cầu ứng viên</FieldLabel>
							<FieldContent>
								<textarea
									id='requirements'
									rows={4}
									placeholder='Nhập yêu cầu về ứng viên...'
									{...register("requirements")}
									className='w-full rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
								/>
							</FieldContent>
						</Field>

						<Field>
							<FieldLabel htmlFor='benefits'>Quyền lợi</FieldLabel>
							<FieldContent>
								<textarea
									id='benefits'
									rows={4}
									placeholder='Nhập quyền lợi cho ứng viên...'
									{...register("benefits")}
									className='w-full rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
								/>
							</FieldContent>
						</Field>

						<Field>
							<FieldLabel>Kỹ năng yêu cầu</FieldLabel>
							<FieldContent>
								{allSkills && allSkills.length > 0 ? (
									<div className='rounded-md border border-input'>
										<div className='relative'>
											<Search className='pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-muted-foreground' />
											<input
												type='text'
												placeholder='Tìm kỹ năng...'
												value={skillSearch}
												onChange={(e) => setSkillSearch(e.target.value)}
												className='h-9 w-full border-0 bg-transparent pl-9 pr-3 text-sm outline-none'
											/>
										</div>
										<div className='grid max-h-40 grid-cols-2 gap-2 overflow-y-auto border-t p-3'>
											{filteredSkills.map((skill) => {
												const isChecked = watchSkillIds.includes(skill.id);
												return (
													<label
														key={skill.id}
														className='flex cursor-pointer items-center gap-2'
													>
														<input
															type='checkbox'
															checked={isChecked}
															onChange={() => {
																const next = isChecked
																	? watchSkillIds.filter((id) => id !== skill.id)
																	: [...watchSkillIds, skill.id];
																setValue("skillIds", next);
															}}
															className='size-4 rounded border-gray-300 text-primary focus:ring-primary'
														/>
														<span className='text-sm'>{skill.name}</span>
													</label>
												);
											})}
											{filteredSkills.length === 0 && skillSearch.trim() && (
												<p className='col-span-2 py-2 text-center text-sm text-muted-foreground'>
													Không tìm thấy kỹ năng phù hợp.
												</p>
											)}
										</div>
									</div>
								) : (
									<p className='text-sm text-muted-foreground'>Đang tải kỹ năng...</p>
								)}
							</FieldContent>
						</Field>
					</FieldGroup>
				</form>
			</div>

			<DialogFooter className='shrink-0 border-t px-6 py-4'>
				<div className='flex justify-end gap-3'>
					<Button
						variant='outline'
						onClick={onCancel}
					>
						<X /> {isCreate ? "Hủy" : "Quay lại"}
					</Button>
					<Button
						type='submit'
						form={formId}
						disabled={actionPending || isSubmitting}
						variant='primary'
					>
						{actionPending || isSubmitting ? (
							<Loader2 className='size-4 animate-spin' />
						) : (
							<Save className='size-4' />
						)}
						{isCreate ? "Tạo tin" : "Lưu thay đổi"}
					</Button>
				</div>
			</DialogFooter>
		</>
	);
}

// ── JobFormDialog ────────────────────────────────────────────────────────

interface JobFormDialogProps {
	open: boolean;
	onOpenChange: (open: boolean) => void;
	mode: "create" | "detail" | "edit";
	jobId?: string;
	onCreated?: (id: string) => void;
	onEditJob?: () => void;
	onCancelEdit?: () => void;
}

export default function JobFormDialog({
	open,
	onOpenChange,
	mode,
	jobId,
	onCreated,
	onEditJob,
	onCancelEdit,
}: JobFormDialogProps) {
	const isCreate = mode === "create";
	const showForm = isCreate || mode === "edit";
	const showView = !showForm;

	const { data: jobDetail, isLoading: detailLoading } = useEmployerJobDetail(jobId ?? "", {
		enabled: !!jobId && mode === "detail",
	});
	const { data: categories } = useCategories();
	const { data: allSkills } = useAllSkills();
	const queryClient = useQueryClient();

	const createJob = useCreateEmployerJob();
	const updateJob = useUpdateEmployerJob(jobId ?? "");
	const submitForReview = useSubmitForReview(jobId ?? "");
	const deleteJob = useDeleteEmployerJob(jobId ?? "");

	const actionPending =
		createJob.isPending || updateJob.isPending || submitForReview.isPending || deleteJob.isPending;

	const formValues = useMemo(() => {
		if (isCreate) return undefined;
		if (jobDetail) return toFormData(jobDetail);
		return undefined;
	}, [isCreate, jobDetail]);

	const {
		register,
		handleSubmit,
		reset,
		control,
		setValue,
		formState: { errors, isSubmitting },
	} = useForm<JobFormData>({
		resolver: zodResolver(jobSchema),
		values: formValues ?? DEFAULT_VALUES,
	});

	useEffect(() => {
		if (jobDetail && !isCreate) {
			reset(toFormData(jobDetail));
		}
	}, [jobDetail, isCreate, reset]);

	const rawSubmit = useCallback(
		async (data: JobFormData) => {
			const request = toRequest(data);
			try {
				if (isCreate) {
					const result = await createJob.mutateAsync(request);
					toast.success("Tạo tin tuyển dụng thành công");
					onCreated?.(result.id);
				} else {
					await updateJob.mutateAsync(request);
					queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEY.lists() });
					toast.success("Cập nhật tin tuyển dụng thành công");
					onCancelEdit?.();
				}
			} catch (err) {
				toast.error(getErrorMessage(err, "Không thể lưu tin tuyển dụng"));
			}
		},
		[isCreate, createJob, onCreated, updateJob, queryClient],
	);

	const onFormSubmit = handleSubmit(rawSubmit);

	const [dialogOpen, setDialogOpen] = useState(false);
	const [dialogAction, setDialogAction] = useState<"submit" | "unpublish" | "delete">("submit");

	const openDialog = (action: "submit" | "unpublish" | "delete") => {
		setDialogAction(action);
		setDialogOpen(true);
	};

	const handleDialogConfirm = () => {
		if (dialogAction === "submit") {
			submitForReview.mutate(undefined, {
				onSuccess: () => {
					toast.success(`Đã gửi duyệt tin "${jobDetail?.title}"`);
					setDialogOpen(false);
				},
				onError: (err) => toast.error(getErrorMessage(err, "Không thể gửi duyệt")),
			});
		} else if (dialogAction === "unpublish") {
			if (!jobDetail) return;
			const data = toFormData(jobDetail);
			updateJob.mutate(toRequest(data), {
				onSuccess: () => {
					queryClient.invalidateQueries({ queryKey: EMPLOYER_JOB_KEY.lists() });
					toast.success("Đã gỡ đăng tin tuyển dụng");
					setDialogOpen(false);
				},
				onError: (err) => toast.error(getErrorMessage(err, "Không thể gỡ đăng")),
			});
		} else if (dialogAction === "delete") {
			deleteJob.mutate(undefined, {
				onSuccess: () => {
					toast.success(`Đã xóa tin "${jobDetail?.title}"`);
					setDialogOpen(false);
					onOpenChange(false);
				},
				onError: (err) => toast.error(getErrorMessage(err, "Không thể xóa")),
			});
		}
	};

	return (
		<>
			<Dialog
				open={open}
				onOpenChange={onOpenChange}
			>
				<DialogContent
					className='max-w-2xl max-h-[85vh] flex flex-col gap-0 p-0 overflow-hidden'
					aria-describedby={undefined}
				>
					<DialogTitle className='sr-only'>
						{isCreate
							? "Tạo tin tuyển dụng"
							: showForm
								? "Chỉnh sửa tin tuyển dụng"
								: (jobDetail?.title ?? "Chi tiết việc làm")}
					</DialogTitle>

					{showView && detailLoading && (
						<div className='flex flex-1 items-center justify-center'>
							<Loader2 className='size-8 animate-spin text-muted-foreground' />
						</div>
					)}

					{showView && !detailLoading && !jobDetail && (
						<div className='flex flex-1 flex-col items-center justify-center gap-2 text-muted-foreground'>
							<Briefcase className='size-12' />
							<p>Không tìm thấy tin tuyển dụng</p>
						</div>
					)}

					{showView && jobDetail && !detailLoading && (
						<ViewMode
							job={jobDetail}
							actionPending={actionPending}
							onEdit={() => onEditJob?.()}
							onSubmit={() => openDialog("submit")}
							onUnpublish={() => openDialog("unpublish")}
							onDelete={() => openDialog("delete")}
							onClose={() => onOpenChange(false)}
						/>
					)}

					{showForm && (
						<FormMode
							isCreate={isCreate}
							formId='job-form'
							register={register}
							errors={errors}
							control={control}
							setValue={setValue}
							isSubmitting={isSubmitting}
							onSubmit={onFormSubmit}
							onCancel={() => {
								if (isCreate) onOpenChange(false);
								else onCancelEdit?.();
							}}
							categories={categories}
							allSkills={allSkills}
							actionPending={actionPending}
						/>
					)}
				</DialogContent>
			</Dialog>

			<BaseDialog
				isOpen={dialogOpen}
				onClose={() => setDialogOpen(false)}
				title={
					dialogAction === "submit"
						? "Xác nhận gửi duyệt"
						: dialogAction === "unpublish"
							? "Xác nhận gỡ đăng"
							: "Xóa tin tuyển dụng"
				}
				description={
					dialogAction === "submit"
						? `Bạn có chắc muốn gửi duyệt tin tuyển dụng "${jobDetail?.title}"?`
						: dialogAction === "unpublish"
							? `Tin "${jobDetail?.title}" sẽ được chuyển về trạng thái nháp. Bạn có thể chỉnh sửa và gửi duyệt lại sau.`
							: `Bạn có chắc muốn xóa tin "${jobDetail?.title}"? Hành động này không thể hoàn tác.`
				}
				footer={
					<div className='flex gap-3'>
						<Button
							variant='outline'
							onClick={() => setDialogOpen(false)}
						>
							Hủy
						</Button>
						<Button
							variant={dialogAction === "delete" ? "destructive" : "primary"}
							onClick={handleDialogConfirm}
							disabled={actionPending}
						>
							{dialogAction === "submit" && "Xác nhận gửi"}
							{dialogAction === "unpublish" && "Xác nhận gỡ"}
							{dialogAction === "delete" && "Xác nhận xóa"}
						</Button>
					</div>
				}
			/>
		</>
	);
}
