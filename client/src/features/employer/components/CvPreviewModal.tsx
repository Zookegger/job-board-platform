import { BaseDialog } from "@/components/shared/BaseDialog";
import { Button } from "@/components/ui/button";
import { ExternalLink, FileX, Loader2 } from "lucide-react";
import { useState } from "react";

/**
 * Chuẩn hoá resumeUrl thành URL path hợp lệ để dùng trong iframe.
 * Xử lý cả:
 *   - URL path đúng:   "/uploads/resumes/uuid.pdf"  → giữ nguyên
 *   - OS path tương đối: "uploads\resumes\uuid.pdf"  → "/uploads/resumes/uuid.pdf"
 *   - OS path tuyệt đối: "C:\...\uploads\resumes\uuid.pdf" → "/uploads/resumes/uuid.pdf"
 */
function normalizeResumeUrl(raw: string | null): string | null {
	if (!raw) return null;
	const normalized = raw.replace(/\\/g, "/");
	// Đã là URL path hợp lệ
	if (normalized.startsWith("/uploads/")) return normalized;
	// Có chứa "uploads/" ở giữa → cắt từ đó
	const idx = normalized.indexOf("uploads/");
	if (idx !== -1) return "/" + normalized.substring(idx);
	return normalized;
}

interface CvPreviewModalProps {
	candidateName: string;
	resumeUrl: string | null;
	open: boolean;
	onClose: () => void;
}

export function CvPreviewModal({ candidateName, resumeUrl, open, onClose }: CvPreviewModalProps) {
	const [loading, setLoading] = useState(true);
	const [loadError, setLoadError] = useState(false);

	const resolvedUrl = normalizeResumeUrl(resumeUrl);

	return (
		<BaseDialog
			isOpen={open}
			onClose={onClose}
			title="Xem CV ứng viên"
			description={`Ứng viên: ${candidateName}`}
			size="3xl"
			modal={false}
		>
			<div className="flex flex-col gap-3">
				{resolvedUrl ? (
					<>
						{/* Toolbar */}
						<div className="flex items-center justify-between">
							<p className="text-xs text-muted-foreground">
								Nhúng trực tiếp — file PDF sẽ hiển thị bên dưới.
							</p>
							<Button variant="ghost" size="sm" asChild>
								<a href={resolvedUrl} target="_blank" rel="noopener noreferrer">
									<ExternalLink className="mr-1.5 size-3.5" />
									Mở tab mới
								</a>
							</Button>
						</div>

						{/* PDF frame */}
						<div className="relative h-[72vh] overflow-hidden rounded-md border bg-muted/30">
							{loading && !loadError && (
								<div className="absolute inset-0 flex flex-col items-center justify-center gap-2 text-muted-foreground">
									<Loader2 className="size-6 animate-spin" />
									<p className="text-sm">Đang tải tài liệu...</p>
								</div>
							)}

							{loadError && (
								<div className="absolute inset-0 flex flex-col items-center justify-center gap-2 text-muted-foreground">
									<FileX className="size-10 opacity-40" />
									<p className="text-sm font-medium">Không thể hiển thị tài liệu.</p>
									<Button variant="outline" size="sm" asChild>
										<a href={resolvedUrl} target="_blank" rel="noopener noreferrer">
											<ExternalLink className="mr-1.5 size-3.5" />
											Tải xuống để xem
										</a>
									</Button>
								</div>
							)}

							<iframe
								key={resolvedUrl}
								src={resolvedUrl}
								title={`CV của ${candidateName}`}
								className="h-full w-full"
								allow="fullscreen"
								onLoad={() => setLoading(false)}
								onError={() => { setLoading(false); setLoadError(true); }}
								style={{ display: loadError ? "none" : "block" }}
							/>
						</div>
					</>
				) : (
					<div className="flex flex-col items-center justify-center py-16 text-muted-foreground gap-3">
						<FileX className="size-12 opacity-40" />
						<p className="text-sm">Ứng viên chưa tải lên CV.</p>
					</div>
				)}
			</div>
		</BaseDialog>
	);
}
