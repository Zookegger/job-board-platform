import { BaseDialog } from "@/components/shared/BaseDialog";
import { Button } from "@/components/ui/button";
import {
	useApproveCompany,
	useRejectCompany,
	useSuspendCompany,
} from "@/hooks/useAdminCompanies";
import type { AdminPendingCompanyResponse, CompanyResponse } from "@/types/company";
import getErrorMessage from "@/utils/getErrorMessage";
import { AlertTriangle, CheckCircle2, XCircle } from "lucide-react";
import { useState } from "react";
import { toast } from "sonner";

type ModalAction = "approve" | "reject" | "suspend";

type CompanyApprovalModalProps = {
	company: AdminPendingCompanyResponse | CompanyResponse;
	action: ModalAction;
	isOpen: boolean;
	onClose: () => void;
};

const TITLES: Record<ModalAction, string> = {
	approve: "Xác nhận duyệt công ty",
	reject: "Từ chối hồ sơ công ty",
	suspend: "Tạm ngưng công ty",
};

const DESCRIPTIONS: Record<ModalAction, string> = {
	approve: 'Bạn có chắc muốn duyệt công ty "{company}"?',
	reject: 'Nhập lý do từ chối cho công ty "{company}".',
	suspend: 'Nhập lý do tạm ngưng cho công ty "{company}".',
};

const SUCCESS_MESSAGES: Record<ModalAction, string> = {
	approve: 'Đã duyệt công ty "{company}"',
	reject: 'Đã từ chối công ty "{company}"',
	suspend: 'Đã tạm ngưng công ty "{company}"',
};

const ERROR_MESSAGES: Record<ModalAction, string> = {
	approve: "Không thể duyệt công ty",
	reject: "Không thể từ chối công ty",
	suspend: "Không thể tạm ngưng công ty",
};

export default function CompanyApprovalModal({
	company,
	action,
	isOpen,
	onClose,
}: CompanyApprovalModalProps) {
	const [reason, setReason] = useState("");

	const approve = useApproveCompany();
	const reject = useRejectCompany();
	const suspend = useSuspendCompany();

	const actionPending = approve.isPending || reject.isPending || suspend.isPending;

	const needReason = action === "reject" || action === "suspend";

	function handleConfirm() {
		if (action === "approve") {
			approve.mutate(company.id, {
				onSuccess: () => {
					toast.success(SUCCESS_MESSAGES.approve.replace("{company}", company.companyName));
					onClose();
				},
				onError: (error) => toast.error(getErrorMessage(error, ERROR_MESSAGES.approve)),
			});
			return;
		}

		if (!reason.trim()) {
			toast.error(`Vui lòng nhập lý do ${action === "reject" ? "từ chối" : "tạm ngưng"}`);
			return;
		}

		const input = { companyId: company.id, reason: reason.trim() };

		if (action === "reject") {
			reject.mutate(input, {
				onSuccess: () => {
					toast.success(SUCCESS_MESSAGES.reject.replace("{company}", company.companyName));
					setReason("");
					onClose();
				},
				onError: (error) => toast.error(getErrorMessage(error, ERROR_MESSAGES.reject)),
			});
		} else {
			suspend.mutate(input, {
				onSuccess: () => {
					toast.success(SUCCESS_MESSAGES.suspend.replace("{company}", company.companyName));
					setReason("");
					onClose();
				},
				onError: (error) => toast.error(getErrorMessage(error, ERROR_MESSAGES.suspend)),
			});
		}
	}

	function handleClose() {
		setReason("");
		onClose();
	}

	const confirmVariant = action === "approve" ? "success" : "destructive";
	const ConfirmIcon = action === "approve" ? CheckCircle2 : action === "reject" ? XCircle : AlertTriangle;
	const confirmLabel =
		action === "approve"
			? "Xác nhận duyệt"
			: action === "reject"
				? "Xác nhận từ chối"
				: "Xác nhận tạm ngưng";

	return (
		<BaseDialog
			isOpen={isOpen}
			onClose={handleClose}
			title={TITLES[action]}
			description={DESCRIPTIONS[action].replace("{company}", company.companyName)}
			size='xl'
			children={
				needReason ? (
					<div className='px-4'>
						<textarea
							value={reason}
							onChange={(event) => setReason(event.target.value)}
							rows={6}
							placeholder={action === "reject" ? "Nhập lý do từ chối" : "Nhập lý do tạm ngưng"}
							className='w-full resize-none rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
						/>
					</div>
				) : undefined
			}
			footer={
				<div className='flex justify-end gap-3'>
					<Button variant='outline' onClick={handleClose} disabled={actionPending}>
						Hủy
					</Button>
					<Button
						variant={confirmVariant}
						onClick={handleConfirm}
						disabled={actionPending || (needReason && !reason.trim())}
					>
						<ConfirmIcon /> {confirmLabel}
					</Button>
				</div>
			}
		/>
	);
}
