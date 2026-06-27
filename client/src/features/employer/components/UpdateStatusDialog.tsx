import { Button } from "@/components/ui/button";
import { Dialog, DialogContent, DialogDescription, DialogTitle } from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { useUpdateApplicationStatus } from "@/hooks/useEmployerApplications";
import { cn } from "@/lib/utils";
import {
	APPLICATION_STATUS_LABELS,
	type ApplicationStatus,
	type CandidateApplicationListResponse,
} from "@/types/application";
import getErrorMessage from "@/utils/getErrorMessage";
import { ArrowRight, Check, Loader2 } from "lucide-react";
import { useEffect } from "react";
import { useForm, useWatch } from "react-hook-form";
import { toast } from "sonner";

// ─── Status config (token-aligned với ApplicationTimeline) ───────────────────

const EMPLOYER_STATUSES: ApplicationStatus[] = ["REVIEWING", "INTERVIEW", "HIRED", "REJECTED"];

interface StatusMeta {
	chipClass: string;
	btnClass: string;
}

const STATUS_META: Record<string, StatusMeta> = {
	PENDING: {
		chipClass: "bg-amber-50 text-amber-700 border border-amber-300",
		btnClass: "bg-amber-600 hover:bg-amber-700 focus-visible:ring-amber-500",
	},
	REVIEWING: {
		chipClass: "bg-blue-50 text-blue-700 border border-blue-300",
		btnClass: "bg-blue-600 hover:bg-blue-700 focus-visible:ring-blue-500",
	},
	INTERVIEW: {
		chipClass: "bg-violet-50 text-violet-700 border border-violet-300",
		btnClass: "bg-violet-600 hover:bg-violet-700 focus-visible:ring-violet-500",
	},
	HIRED: {
		chipClass: "bg-green-50 text-green-700 border border-green-300",
		btnClass: "bg-green-600 hover:bg-green-700 focus-visible:ring-green-500",
	},
	REJECTED: {
		chipClass: "bg-red-50 text-red-700 border border-red-300",
		btnClass: "bg-red-600 hover:bg-red-700 focus-visible:ring-red-500",
	},
	WITHDRAWN: {
		chipClass: "bg-gray-100 text-gray-600 border border-gray-300",
		btnClass: "bg-gray-600 hover:bg-gray-700 focus-visible:ring-gray-500",
	},
};

// ─── Avatar helpers ───────────────────────────────────────────────────────────

const AVATAR_PALETTES = [
	"bg-blue-100 text-blue-700",
	"bg-violet-100 text-violet-700",
	"bg-emerald-100 text-emerald-700",
	"bg-orange-100 text-orange-700",
	"bg-rose-100 text-rose-700",
	"bg-cyan-100 text-cyan-700",
];

function getInitials(name: string): string {
	const parts = name.trim().split(/\s+/).filter(Boolean);
	if (parts.length === 0) return "?";
	if (parts.length === 1) return parts[0].charAt(0).toUpperCase();
	return (parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase();
}

function pickAvatarPalette(name: string): string {
	let hash = 0;
	for (const ch of name) hash = (hash * 31 + ch.charCodeAt(0)) >>> 0;
	return AVATAR_PALETTES[hash % AVATAR_PALETTES.length];
}

// ─── StatusChip ───────────────────────────────────────────────────────────────

function StatusChip({ status }: { status: string }) {
	const meta = STATUS_META[status] ?? STATUS_META.REVIEWING;
	return (
		<span className={cn("inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium", meta.chipClass)}>
			{APPLICATION_STATUS_LABELS[status as ApplicationStatus] ?? status}
		</span>
	);
}

// ─── Types ────────────────────────────────────────────────────────────────────

interface FormValues {
	status: ApplicationStatus;
	reason: string;
}

interface UpdateStatusDialogProps {
	application: CandidateApplicationListResponse | null;
	open: boolean;
	onOpenChange: (open: boolean) => void;
}

// ─── Component ────────────────────────────────────────────────────────────────

export function UpdateStatusDialog({ application, open, onOpenChange }: UpdateStatusDialogProps) {
	const { mutate, isPending } = useUpdateApplicationStatus();

	const { register, handleSubmit, setValue, control, reset } = useForm<FormValues>({
		defaultValues: { status: "REVIEWING", reason: "" },
	});

	const selectedStatus = useWatch({ name: "status", control });

	useEffect(() => {
		if (open && application) {
			const selectable = EMPLOYER_STATUSES.includes(application.status as ApplicationStatus);
			reset({
				status: selectable ? (application.status as ApplicationStatus) : "REVIEWING",
				reason: "",
			});
		}
	}, [open, application, reset]);

	function onSubmit(data: FormValues) {
		if (!application) return;
		mutate(
			{ id: application.id, status: data.status, reason: data.reason || undefined },
			{
				onSuccess: () => {
					toast.success("Cập nhật trạng thái thành công");
					onOpenChange(false);
				},
				onError: (err) => {
					toast.error(getErrorMessage(err));
				},
			},
		);
	}

	if (!application) return null;

	const initials = getInitials(application.candidateName);
	const avatarPalette = pickAvatarPalette(application.candidateName);
	const btnMeta = STATUS_META[selectedStatus] ?? STATUS_META.REVIEWING;

	return (
		<Dialog
			open={open}
			onOpenChange={onOpenChange}
		>
			<DialogContent className='gap-0 overflow-hidden p-0 sm:max-w-md'>
				{/* ── Header ────────────────────────────────────────────────────── */}
				<div className='border-b border-border px-6 py-4'>
					<DialogTitle className='text-base font-semibold leading-snug'>
						Cập nhật trạng thái hồ sơ
					</DialogTitle>
					<DialogDescription className='sr-only'>
						Cập nhật trạng thái ứng tuyển cho {application.candidateName}
					</DialogDescription>

					<div className='mt-3 flex items-center gap-2.5'>
						<div
							className={cn(
								"flex h-8 w-8 shrink-0 items-center justify-center rounded-full text-xs font-bold",
								avatarPalette,
							)}
							aria-hidden='true'
						>
							{initials}
						</div>
						<p className='text-sm text-muted-foreground'>
							Ứng viên: <span className='font-semibold text-foreground'>{application.candidateName}</span>
						</p>
					</div>
				</div>

				{/* ── Form body ─────────────────────────────────────────────────── */}
				<form onSubmit={handleSubmit(onSubmit)}>
					<div className='flex flex-col gap-5 px-6 py-5'>
						{/* Transition preview */}
						<div className='flex items-center gap-2 rounded-md border border-border bg-muted/40 px-3 py-2'>
							<span className='shrink-0 text-xs text-muted-foreground'>Chuyển sang:</span>
							<StatusChip status={application.status} />
							<ArrowRight className='size-3.5 shrink-0 text-muted-foreground' />
							<StatusChip status={selectedStatus} />
						</div>

						{/* Status select */}
						<div className='flex flex-col gap-1.5'>
							<Label
								htmlFor='status'
								className='text-sm font-medium'
							>
								Trạng thái mới{" "}
								<span
									className='text-destructive'
									aria-hidden='true'
								>
									*
								</span>
							</Label>
							<Select
								value={selectedStatus}
								onValueChange={(val) => setValue("status", val as ApplicationStatus)}
							>
								<SelectTrigger
									id='status'
									className='w-full'
								>
									<SelectValue />
								</SelectTrigger>
								<SelectContent>
									{EMPLOYER_STATUSES.map((s) => (
										<SelectItem
											key={s}
											value={s}
											textValue={APPLICATION_STATUS_LABELS[s] ?? s}
										>
											<StatusChip status={s} />
										</SelectItem>
									))}
								</SelectContent>
							</Select>
						</div>

						{/* Reason textarea */}
						<div className='flex flex-col gap-1.5'>
							<Label
								htmlFor='reason'
								className='text-sm font-medium'
							>
								Ghi chú / Lý do
							</Label>
							<Textarea
								id='reason'
								rows={3}
								placeholder='Nhập lý do hoặc ghi chú (tuỳ chọn)...'
								className='resize-none text-sm'
								{...register("reason")}
							/>
						</div>
					</div>

					{/* ── Footer ──────────────────────────────────────────────────── */}
					<div className='flex items-center justify-end gap-2 border-t border-border px-6 py-4'>
						<Button
							type='button'
							variant='outline'
							onClick={() => onOpenChange(false)}
							disabled={isPending}
							className='min-w-[80px]'
						>
							Huỷ
						</Button>
						<Button
							type='submit'
							disabled={isPending}
							className={cn("min-w-[130px] text-white", btnMeta.btnClass)}
						>
							{isPending ? (
								<Loader2 className='mr-2 size-4 animate-spin' />
							) : (
								<Check className='mr-2 size-4' />
							)}
							Lưu thay đổi
						</Button>
					</div>
				</form>
			</DialogContent>
		</Dialog>
	);
}
