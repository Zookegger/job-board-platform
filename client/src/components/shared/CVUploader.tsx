import { Eye, FileText, Files, Loader2, Upload, X, ZoomIn, ZoomOut } from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";
import { useDropzone } from "react-dropzone";
import { Document, Page, pdfjs } from "react-pdf";
import "react-pdf/dist/Page/TextLayer.css";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { PDFSkeleton } from "@/components/ui/skeleton";
import { useUploadResume } from "@/hooks/useProfile";
import { useToast } from "@/providers/ToastProvider";
import { formatFileSize } from "@/utils/FileUtils";
import getErrorMessage from "@/utils/getErrorMessage";

pdfjs.GlobalWorkerOptions.workerSrc = new URL("pdfjs-dist/build/pdf.worker.min.mjs", import.meta.url).toString();

const ERROR_MESSAGES: Record<string, string> = {
	"file-too-large": "File quá lớn. Vui lòng chọn file nhỏ hơn 10MB.",
	"file-invalid-type": "Định dạng file không hợp lệ. Vui lòng chọn file PDF.",
	"too-many-files": "Chỉ được phép tải lên một file.",
};

/**
 * Component cho phép ứng viên tải lên CV (file PDF), xem trước file trước khi gửi, và validation định dạng.
 *
 * LƯU Ý: Nếu có xài IDM thì exclude domain của API để tránh bị lỗi khi preview file (IDM sẽ can thiệp vào request)
 */
export default function CVUploader({ onSuccess }: { onSuccess?: () => void }) {
	const uploadResume = useUploadResume();
	const toast = useToast();

	const [uploadProgress, setUploadProgress] = useState(0);
	const [selectedFile, setSelectedFile] = useState<File | null>(null);
	const [isPdfLoading, setIsPdfLoading] = useState(false);

	const [scale, setScale] = useState(1);
	const previewContainerRef = useRef<HTMLDivElement>(null);
	const [containerWidth, setContainerWidth] = useState(600);

	const previewUrl = useMemo(() => {
		if (!selectedFile) return null;
		return URL.createObjectURL(selectedFile);
	}, [selectedFile]);

	useEffect(() => {
		return () => {
			if (previewUrl) {
				URL.revokeObjectURL(previewUrl);
			}
		};
	}, [previewUrl]);

	useEffect(() => {
		const container = previewContainerRef.current;
		if (!container) return;

		const observer = new ResizeObserver((entries) => {
			setContainerWidth(entries[0].contentRect.width - 32);
		});
		observer.observe(container);

		return () => observer.disconnect();
	}, []);

	const { getRootProps, getInputProps, isDragActive } = useDropzone({
		accept: { "application/pdf": [".pdf"] },
		maxFiles: 1,
		maxSize: 10 * 1024 * 1024,
		onDropAccepted: (acceptedFiles) => {
			setSelectedFile(acceptedFiles[0]);
			setIsPdfLoading(true);
		},
		onDropRejected: (rejections) => {
			const firstError = rejections[0]?.errors[0];
			const message = firstError
				? ERROR_MESSAGES[firstError.code] || "Có lỗi xảy ra khi tải lên file"
				: "Có lỗi xảy ra khi tải lên file";
			toast.error(message);
		},
	});

	const [numPages, setNumPages] = useState<number>();

	function onDocumentLoadSuccess({ numPages }: { numPages: number }) {
		setNumPages(numPages);
		setIsPdfLoading(false);
	}

	async function handleUpload() {
		if (!selectedFile) return;
		setUploadProgress(0);
		try {
			await uploadResume.mutateAsync({
				file: selectedFile,
				onUploadProgress: setUploadProgress,
			});
			toast.success("Đã cập nhật CV");
			setUploadProgress(0);
			setSelectedFile(null);
			onSuccess?.();
		} catch (error) {
			toast.error(getErrorMessage(error) || "Có lỗi xảy ra khi cập nhật CV");
			setUploadProgress(0);
		}
	}

	function handleCancel() {
		setSelectedFile(null);
		setUploadProgress(0);
	}

	const showPreview = selectedFile && !uploadResume.isPending;

	return (
		<Card>
			<CardHeader>
				<CardTitle>Sơ yếu lý lịch / CV</CardTitle>
			</CardHeader>
			<CardContent className='space-y-4'>
				{showPreview ? (
					<div className='relative'>
						{/* Thanh công cụ */}
						<div className='flex flex-col sm:flex-row items-start sm:items-center justify-between gap-3 rounded-lg border px-3.5 py-2.5 mb-3 bg-background'>
							<div className='flex items-center gap-2.5 min-w-0 w-full sm:w-auto'>
								<div className='shrink-0 w-8.5 h-8.5 rounded-md bg-info/10 flex items-center justify-center'>
									<FileText className='h-4.5 w-4.5 text-info-foreground' />
								</div>
								<div className='min-w-0 flex-1'>
									<p className='text-sm font-medium text-foreground truncate max-w-full sm:max-w-[320px]'>
										{selectedFile.name}
									</p>
									<p className='text-xs text-muted-foreground mt-px'>
										PDF &middot; {formatFileSize(selectedFile.size)}
									</p>
								</div>
								<span className='shrink-0 text-[11px] font-bold px-2 py-0.75 rounded-full bg-success text-success-foreground self-start sm:self-auto'>
									Sẵn sàng
								</span>
							</div>
							<div className='flex items-center gap-2 w-full sm:w-auto justify-end sm:justify-start'>
								<Button
									type='button'
									variant='outline'
									size='sm'
									onClick={handleCancel}
									className='w-full sm:w-auto'
								>
									<X className='h-3.5 w-3.5' />
									Huỷ
								</Button>
								<Button
									type='button'
									variant='dark'
									size='sm'
									onClick={handleUpload}
									className='w-full sm:w-auto'
								>
									<Upload className='h-3.75 w-3.75' />
									Tải lên
								</Button>
							</div>
						</div>

						{/* Xem trước PDF */}
						<div
							ref={previewContainerRef}
							className='bg-muted rounded-lg border border-muted-foreground/20 overflow-auto relative'
						>
							<div className='flex flex-col items-start p-4'>
								<div className='flex items-center gap-1 bg-background p-1 rounded-md border shadow-sm mb-4'>
									<Button
										variant='ghost'
										size='icon-xs'
										onClick={() => setScale((prev) => Math.max(0.5, prev - 0.25))}
										disabled={scale <= 0.5}
									>
										<ZoomOut className='h-4 w-4' />
									</Button>
									<span className='text-xs font-medium w-10 sm:w-12 text-center'>
										{Math.round(scale * 100)}%
									</span>
									<Button
										variant='ghost'
										size='icon-xs'
										onClick={() => setScale((prev) => Math.min(3, prev + 0.25))}
										disabled={scale >= 3}
									>
										<ZoomIn className='h-4 w-4' />
									</Button>
								</div>
								<Document
									file={previewUrl}
									onLoadSuccess={onDocumentLoadSuccess}
									loading={<PDFSkeleton />}
									error={<p className='text-sm text-destructive'>Không thể hiển thị PDF</p>}
								>
									<Page
										key={`preview-${Math.round(scale * 100)}`}
										pageNumber={1}
										width={containerWidth}
										scale={scale}
										renderTextLayer={false}
										renderAnnotationLayer={false}
									/>
								</Document>
							</div>

							{numPages && numPages > 1 && (
								<>
									<div className='absolute bottom-11 right-5 bg-background border border-muted-foreground/20 rounded-full text-[11px] font-medium text-muted-foreground px-2.25 py-0.75 flex items-center gap-1'>
										<Files className='h-3 w-3' />
										{numPages} trang
									</div>
									<div className='flex items-center justify-center gap-1.5 px-4 py-2.5 border-t border-muted-foreground/20 text-xs text-muted-foreground/60'>
										<Eye className='h-3.5 w-3.5' />
										Trang 1 đang hiển thị
									</div>
								</>
							)}
						</div>

						{isPdfLoading && (
							<div className='absolute inset-0 z-10 flex items-center justify-center rounded-lg bg-background/80 backdrop-blur-sm'>
								<Loader2 className='h-8 w-8 animate-spin text-primary' />
							</div>
						)}
					</div>
				) : uploadResume.isPending ? (
					<>
						<div className='flex flex-col items-center justify-center gap-4 rounded-lg border-2 border-dashed p-6 sm:p-10 pointer-events-none opacity-60'>
							<Loader2 className='h-10 w-10 animate-spin text-muted-foreground' />
							<div className='text-center'>
								<p className='font-medium'>Đang tải lên...</p>
							</div>
						</div>
						{uploadProgress > 0 && uploadProgress < 100 && (
							<div className='w-full bg-muted rounded-full h-2 overflow-hidden'>
								<div
									className='bg-primary h-full transition-all duration-300'
									style={{ width: `${uploadProgress}%` }}
								/>
							</div>
						)}
					</>
				) : (
					<div
						{...getRootProps()}
						className={`flex flex-col items-center justify-center gap-4 rounded-lg border-2 border-dashed p-6 sm:p-10 cursor-pointer transition-colors ${
							isDragActive
								? "border-primary bg-primary/5"
								: "border-muted-foreground/25 hover:border-muted-foreground/50"
						}`}
					>
						<input {...getInputProps()} />
						<Upload className='h-10 w-10 text-muted-foreground' />
						<div className='text-center'>
							<p className='font-medium'>
								{isDragActive
									? "Thả file PDF vào đây"
									: "Kéo thả file PDF vào đây, hoặc nhấn để chọn file"}
							</p>
							<p className='mt-1 text-sm text-muted-foreground'>Chỉ chấp nhận file PDF (tối đa 10MB)</p>
						</div>
					</div>
				)}
			</CardContent>
		</Card>
	);
}
