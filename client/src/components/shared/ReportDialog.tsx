import { useCreateReport } from "@/hooks/useReports";
import { ReportReason } from "@/types/report";
import getErrorMessage from "@/utils/getErrorMessage";
import { AlertTriangle } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";
import { BaseDialog } from "./BaseDialog";
import { Button } from "../ui/button";

const REASON_LABELS: Record<ReportReason, string> = {
	[ReportReason.SPAM]: "Spam",
	[ReportReason.SCAM]: "Lừa đảo",
	[ReportReason.INAPPROPRIATE]: "Nội dung không phù hợp",
	[ReportReason.OTHER]: "Khác",
};

interface ReportDialogProps {
	isOpen: boolean;
	onClose: () => void;
	targetType: "job" | "company";
	targetId: string;
}

export function ReportDialog({ isOpen, onClose, targetType, targetId }: ReportDialogProps) {
	const [reason, setReason] = useState<ReportReason>(ReportReason.SPAM);
	const [details, setDetails] = useState("");
	const createReport = useCreateReport();

	const handleSubmit = () => {
		const payload = {
			...(targetType === "job" ? { jobId: targetId } : { companyId: targetId }),
			reason,
			details: details.trim() || undefined,
		};

		createReport.mutate(payload, {
			onSuccess: () => {
				toast.success("Báo cáo đã được gửi. Cảm ơn bạn đã đóng góp!");
				setReason(ReportReason.SPAM);
				setDetails("");
				onClose();
			},
			onError: (err) => toast.error(getErrorMessage(err, "Không thể gửi báo cáo")),
		});
	};

	return (
		<BaseDialog
			isOpen={isOpen}
			onClose={onClose}
			title='Báo cáo vi phạm'
			description={`Báo cáo ${targetType === "job" ? "tin tuyển dụng" : "công ty"} này.`}
			size='md'
		>
			<div className='space-y-4 px-4'>
				<div className='space-y-2'>
					<label className='text-sm font-medium text-foreground'>Lý do báo cáo</label>
					<div className='space-y-2'>
						{Object.values(ReportReason).map((r) => (
							<label
								key={r}
								className='flex cursor-pointer items-center gap-3 rounded-md border border-input p-3 text-sm has-checked:border-primary has-checked:bg-primary/5'
							>
								<input
									type='radio'
									name='reason'
									value={r}
									checked={reason === r}
									onChange={() => setReason(r)}
									className='size-4 accent-primary'
								/>
								{REASON_LABELS[r]}
							</label>
						))}
					</div>
				</div>

				<div className='space-y-2'>
					<label className='text-sm font-medium text-foreground'>Chi tiết (tuỳ chọn)</label>
					<textarea
						value={details}
						onChange={(e) => setDetails(e.target.value)}
						rows={4}
						placeholder='Mô tả thêm về lý do báo cáo...'
						className='w-full resize-none rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
					/>
				</div>
			</div>

			<div className='flex justify-end gap-3 px-4 pb-4'>
				<Button
					variant='outline'
					onClick={onClose}
					disabled={createReport.isPending}
				>
					Hủy
				</Button>
				<Button
					variant='destructive'
					onClick={handleSubmit}
					disabled={createReport.isPending}
				>
					<AlertTriangle />
					{createReport.isPending ? "Đang gửi..." : "Gửi báo cáo"}
				</Button>
			</div>
		</BaseDialog>
	);
}
