import { useState } from "react";
import { useParams } from "react-router-dom";
import { CheckCircle2, Loader2 } from "lucide-react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import { UserRole } from "@/types/auth";
import { useAuth } from "@/hooks/useAuth";
import { useHasApplied, useWithdrawApplication } from "@/hooks/useApplications";
import { ApplyDialog } from "./components/ApplyDialog";

export function JobDetailPage() {
	const { id } = useParams<{ id: string }>();
	const { user } = useAuth();
	const isCandidate = user?.role === UserRole.CANDIDATE;

	const { data: hasAppliedData, isLoading: checkLoading } = useHasApplied(id);
	const hasApplied = hasAppliedData?.applied ?? false;

	const [applyDialogOpen, setApplyDialogOpen] = useState(false);

	return (
		<div className="mx-auto max-w-3xl px-4 py-8">
			{/* Placeholder job info — sẽ được thay bằng dữ liệu thực từ API */}
			<div className="rounded-lg border bg-card p-6 shadow-sm">
				<h1 className="text-2xl font-bold">Chi tiết việc làm</h1>
				<p className="mt-1 text-muted-foreground">Mã việc làm: {id}</p>
				<p className="mt-4 text-muted-foreground italic">Nội dung chi tiết đang được phát triển...</p>

				{/* Nút ứng tuyển — chỉ hiện với CANDIDATE */}
				{isCandidate && (
					<div className="mt-6 flex items-center gap-3">
						{checkLoading ? (
							<Button disabled>
								<Loader2 className="mr-2 h-4 w-4 animate-spin" />
								Đang kiểm tra...
							</Button>
						) : hasApplied ? (
							<>
								<Button disabled variant="secondary" className="gap-2 cursor-not-allowed">
									<CheckCircle2 className="h-4 w-4 text-green-600" />
									Đã ứng tuyển
								</Button>
								<WithdrawButton jobId={id!} />
							</>
						) : (
							<Button onClick={() => setApplyDialogOpen(true)}>Ứng tuyển ngay</Button>
						)}
					</div>
				)}
			</div>

			{id && (
				<ApplyDialog
					open={applyDialogOpen}
					onOpenChange={setApplyDialogOpen}
					jobId={id}
					jobTitle={`Việc làm #${id}`}
				/>
			)}
		</div>
	);
}

/** Nút rút đơn — chỉ dùng nội bộ trong trang này */
function WithdrawButton({ jobId }: { jobId: string }) {
	const withdrawMutation = useWithdrawApplication();

	const handleWithdraw = () => {
		if (!confirm("Bạn có chắc muốn rút đơn ứng tuyển này không?")) return;
		// applicationId không có sẵn ở đây vì JobDetailPage chỉ có jobId
		// Trong thực tế cần fetch application detail để lấy id
		// Tạm thời disabled — logic đầy đủ khi có API getMyApplicationByJobId
		toast.info("Chức năng rút đơn đang được phát triển.", { position: "bottom-right" });
	};

	return (
		<Button
			variant="outline"
			size="sm"
			onClick={handleWithdraw}
			disabled={withdrawMutation.isPending}
		>
			{withdrawMutation.isPending ? "Đang rút..." : "Rút đơn"}
		</Button>
	);
}

