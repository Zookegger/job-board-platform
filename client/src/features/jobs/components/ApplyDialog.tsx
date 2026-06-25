import { BaseDialog } from "@/components/shared/BaseDialog";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { useSubmitApplication } from "@/hooks/useApplications";
import { useResume } from "@/hooks/useProfile";
import { useToast } from "@/providers/ToastProvider";
import { AlertCircle, CheckCircle2, FileText, Loader2 } from "lucide-react";
import { useState } from "react";

interface ApplyDialogProps {
	isOpen: boolean;
	onClose: () => void;
	jobId: string;
	jobTitle: string;
	companyName: string;
}

export function ApplyDialog({ isOpen, onClose, jobId, jobTitle, companyName }: ApplyDialogProps) {
	const [coverLetter, setCoverLetter] = useState("");
	const { data: resume, isLoading: resumeLoading } = useResume();
	const submitMutation = useSubmitApplication();
	const toast = useToast();

	function handleClose() {
		if (submitMutation.isPending) return;
		setCoverLetter("");
		submitMutation.reset();
		onClose();
	}

	function handleSubmit() {
		submitMutation.mutate(
			{ jobId, coverLetter: coverLetter.trim() || undefined },
			{
				onSuccess: () => {
					toast.success(`Đơn ứng tuyển vào vị trí "${jobTitle}" đã được gửi.`);
					handleClose();
				},
				onError: (error) => {
					toast.error(error.message || "Đã có lỗi xảy ra. Vui lòng thử lại.");
				},
			},
		);
	}

	const footer = (
		<>
			<Button
				variant='outline'
				onClick={handleClose}
				disabled={submitMutation.isPending}
			>
				Hủy
			</Button>
			<Button
				onClick={handleSubmit}
				variant='primary'
				disabled={submitMutation.isPending || !resume}
			>
				{submitMutation.isPending && <Loader2 className='mr-2 h-4 w-4 animate-spin' />}
				Nộp đơn
			</Button>
		</>
	);

	return (
		<BaseDialog
			isOpen={isOpen}
			onClose={handleClose}
			title='Ứng tuyển vị trí'
			description={`${jobTitle} — ${companyName}`}
			size='lg'
			footer={footer}
		>
			<div className='space-y-5'>
				{/* CV Section */}
				<div className='space-y-2'>
					<Label>CV đính kèm</Label>
					{resumeLoading ? (
						<div className='flex items-center gap-2 rounded-lg border p-3 text-sm text-muted-foreground'>
							<Loader2 className='h-4 w-4 animate-spin' />
							Đang tải thông tin CV...
						</div>
					) : resume ? (
						<div className='flex items-center gap-3 rounded-lg border bg-muted/40 p-3'>
							<FileText className='h-5 w-5 shrink-0 text-primary' />
							<div className='min-w-0 flex-1'>
								<p className='truncate text-sm font-medium'>{resume.title}</p>
								<p className='text-xs text-muted-foreground'>{resume.originalFileName}</p>
							</div>
							<CheckCircle2 className='h-4 w-4 shrink-0 text-green-500' />
						</div>
					) : (
						<div className='flex items-start gap-3 rounded-lg border border-destructive/40 bg-destructive/5 p-3'>
							<AlertCircle className='mt-0.5 h-4 w-4 shrink-0 text-destructive' />
							<p className='text-sm text-destructive'>
								Bạn chưa upload CV. Vui lòng{" "}
								<a
									href='/profile'
									className='font-medium underline underline-offset-2'
									onClick={handleClose}
								>
									cập nhật hồ sơ
								</a>{" "}
								trước khi ứng tuyển.
							</p>
						</div>
					)}
				</div>

				{/* Cover Letter */}
				<div className='space-y-2'>
					<Label htmlFor='cover-letter'>
						Thư ứng tuyển{" "}
						<span className='text-xs font-normal text-muted-foreground'>(không bắt buộc)</span>
					</Label>
					<textarea
						id='cover-letter'
						placeholder='Giới thiệu bản thân và lý do bạn phù hợp với vị trí này...'
						rows={6}
						maxLength={5000}
						value={coverLetter}
						onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setCoverLetter(e.target.value)}
						className='flex min-h-[80px] w-full resize-none rounded-md border border-input bg-background px-3 py-2 text-sm ring-offset-background placeholder:text-muted-foreground focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:cursor-not-allowed disabled:opacity-50'
						disabled={submitMutation.isPending}
					/>
					<p className='text-right text-xs text-muted-foreground'>{coverLetter.length}/5000</p>
				</div>
			</div>
		</BaseDialog>
	);
}
