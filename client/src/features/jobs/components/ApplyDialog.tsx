import { useState } from "react";
import { toast } from "sonner";
import { Button } from "@/components/ui/button";
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import { Label } from "@/components/ui/label";
import { Textarea } from "@/components/ui/textarea";
import { useResume } from "@/hooks/useProfile";
import { useSubmitApplication } from "@/hooks/useApplications";

interface ApplyDialogProps {
	open: boolean;
	onOpenChange: (open: boolean) => void;
	jobId: string;
	jobTitle: string;
	onSuccess?: () => void;
}

export function ApplyDialog({ open, onOpenChange, jobId, jobTitle, onSuccess }: ApplyDialogProps) {
	const [coverLetter, setCoverLetter] = useState("");
	const { data: resume, isLoading: resumeLoading } = useResume();
	const submitMutation = useSubmitApplication();

	const handleSubmit = () => {
		submitMutation.mutate(
			{ jobId, coverLetter: coverLetter.trim() || undefined },
			{
				onSuccess: () => {
					toast.success("Nộp đơn thành công! Chúc bạn may mắn.", { position: "bottom-right" });
					onOpenChange(false);
					setCoverLetter("");
					onSuccess?.();
				},
				onError: (error) => {
					toast.error(error.message || "Nộp đơn thất bại. Vui lòng thử lại.", {
						position: "bottom-right",
					});
				},
			},
		);
	};

	return (
		<Dialog open={open} onOpenChange={onOpenChange}>
			<DialogContent className="sm:max-w-lg">
				<DialogHeader>
					<DialogTitle>Ứng tuyển vị trí</DialogTitle>
					<DialogDescription className="font-medium text-foreground">{jobTitle}</DialogDescription>
				</DialogHeader>

				<div className="space-y-4 py-2">
					{/* CV đính kèm */}
					<div className="space-y-1">
						<Label>CV đính kèm</Label>
						{resumeLoading ? (
							<p className="text-sm text-muted-foreground">Đang tải CV...</p>
						) : resume ? (
							<div className="rounded-md border bg-muted/40 px-3 py-2 text-sm">
								📄 {resume.originalFileName}{" "}
								<span className="text-muted-foreground">
									({(resume.fileSize / 1024).toFixed(0)} KB)
								</span>
							</div>
						) : (
							<p className="text-sm text-destructive">
								Bạn chưa upload CV. Vui lòng{" "}
								<a href="/candidate/profile" className="underline">
									cập nhật hồ sơ
								</a>{" "}
								trước khi ứng tuyển.
							</p>
						)}
					</div>

					{/* Cover letter */}
					<div className="space-y-1">
						<Label htmlFor="coverLetter">
							Thư xin việc <span className="text-muted-foreground">(tùy chọn)</span>
						</Label>
						<Textarea
							id="coverLetter"
							placeholder="Giới thiệu bản thân và lý do bạn phù hợp với vị trí này..."
							rows={5}
							maxLength={5000}
							value={coverLetter}
							onChange={(e) => setCoverLetter(e.target.value)}
						/>
						<p className="text-xs text-muted-foreground text-right">
							{coverLetter.length}/5000
						</p>
					</div>
				</div>

				<DialogFooter>
					<Button variant="outline" onClick={() => onOpenChange(false)}>
						Hủy
					</Button>
					<Button
						onClick={handleSubmit}
						disabled={!resume || submitMutation.isPending}
					>
						{submitMutation.isPending ? "Đang nộp..." : "Nộp đơn"}
					</Button>
				</DialogFooter>
			</DialogContent>
		</Dialog>
	);
}
