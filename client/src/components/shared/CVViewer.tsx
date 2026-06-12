import {
	ChevronLeft,
	ChevronRight,
	Eye,
	FileText,
	Loader2,
	Pencil,
	RotateCcw,
	Trash2,
	Upload,
	X,
	ZoomIn,
	ZoomOut,
} from "lucide-react";
import { useEffect, useMemo, useRef, useState } from "react";
import { Document, Page, pdfjs } from "react-pdf";
import "react-pdf/dist/Page/TextLayer.css";

import CVUploader from "@/components/shared/CVUploader";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import { useDeleteResume, usePreviewCV, useResume, useUpdateResume } from "@/hooks/useProfile";
import { useToast } from "@/providers/ToastProvider";
import getErrorMessage from "@/utils/getErrorMessage";

pdfjs.GlobalWorkerOptions.workerSrc = new URL("pdfjs-dist/build/pdf.worker.min.mjs", import.meta.url).toString();

function formatFileSize(bytes: number): string {
	if (bytes < 1024) return `${bytes} B`;
	if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
	return `${(bytes / (1024 * 1024)).toFixed(1)} MB`;
}

function ZoomControls({ zoom, setZoom }: { zoom: number; setZoom: React.Dispatch<React.SetStateAction<number>> }) {
	return (
		<div className='flex items-center gap-1 bg-background p-1 rounded-md border shadow-sm mb-4'>
			<Button
				variant='ghost'
				size='icon-xs'
				onClick={() => setZoom((z) => Math.max(0.5, z - 0.25))}
				disabled={zoom <= 0.5}
			>
				<ZoomOut className='h-4 w-4' />
			</Button>
			<span className='text-xs font-medium w-10 sm:w-12 text-center'>{Math.round(zoom * 100)}%</span>

			<Button
				variant='ghost'
				size='icon-xs'
				onClick={() => setZoom((z) => Math.min(3, z + 0.25))}
				disabled={zoom >= 3}
			>
				<ZoomIn className='h-4 w-4' />
			</Button>
			<div className='w-px h-4 bg-border mx-1' />
			<Button
				variant='ghost'
				size='icon-xs'
				onClick={() => setZoom(1)}
				disabled={zoom === 1}
			>
				<RotateCcw className='h-3.5 w-3.5' />
			</Button>
		</div>
	);
}

export default function CVViewer() {
	const { data: resume, isLoading } = useResume();
	const deleteResume = useDeleteResume();
	const updateResume = useUpdateResume();
	const toast = useToast();

	const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
	const [isEditingTitle, setIsEditingTitle] = useState(false);
	const [editTitle, setEditTitle] = useState("");
	const [showReplace, setShowReplace] = useState(false);

	const { data: pdfData, isFetching: isPdfLoading, isError: isPdfError } = usePreviewCV(!!resume);

	const pdfUrl = useMemo(() => {
		if (!pdfData) return null;
		return URL.createObjectURL(pdfData);
	}, [pdfData]);

	useEffect(() => {
		return () => {
			if (pdfUrl) {
				URL.revokeObjectURL(pdfUrl);
			}
		};
	}, [pdfUrl]);

	const [numPages, setNumPages] = useState<number>();
	const [pageNumber, setPageNumber] = useState(1);

	async function handleDelete() {
		try {
			await deleteResume.mutateAsync();
			toast.success("Đã xoá CV");
			setShowDeleteConfirm(false);
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
	}

	function handleStartEditTitle() {
		setEditTitle(resume?.title ?? "");
		setIsEditingTitle(true);
	}

	async function handleSaveTitle() {
		if (!editTitle.trim()) return;
		try {
			await updateResume.mutateAsync({ title: editTitle.trim() });
			toast.success("Đã cập nhật tên CV");
			setIsEditingTitle(false);
		} catch (error) {
			toast.error(getErrorMessage(error));
		}
	}

	function onDocumentLoadSuccess({ numPages }: { numPages: number }) {
		setNumPages(numPages);
	}

	const [zoom, setZoom] = useState(1);
	const containerRef = useRef<HTMLDivElement>(null);
	const [containerWidth, setContainerWidth] = useState(600); // Fallback width

	useEffect(() => {
		const container = containerRef.current;
		if (!container) return;

		const observer = new ResizeObserver((entries) => {
			setContainerWidth(entries[0].contentRect.width - 32);
		});
		observer.observe(container);

		return () => {
			observer.disconnect();
		};
	}, []);

	if (isLoading) {
		return (
			<Card>
				<CardHeader>
					<CardTitle>Sơ yếu lý lịch / CV</CardTitle>
				</CardHeader>
				<CardContent>
					<Skeleton className='h-40 w-full' />
				</CardContent>
			</Card>
		);
	}

	if (!resume) {
		return <CVUploader />;
	}

	return (
		<div className='space-y-4'>
			<div className='flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 rounded-lg border p-4'>
				<div className='flex items-center gap-3 min-w-0 w-full sm:w-auto'>
					<FileText className='h-8 w-8 shrink-0 text-primary' />
					<div className='min-w-0 space-y-1 flex-1'>
						{isEditingTitle ? (
							<div className='flex items-center gap-2'>
								<Input
									value={editTitle}
									onChange={(e) => setEditTitle(e.target.value)}
									className='h-8'
									autoFocus
								/>
								<Button
									size='icon-xs'
									variant='ghost'
									onClick={handleSaveTitle}
									disabled={updateResume.isPending}
								>
									{updateResume.isPending ? (
										<Loader2 className='h-4 w-4 animate-spin' />
									) : (
										<Upload className='h-4 w-4' />
									)}
								</Button>
								<Button
									size='icon-xs'
									variant='ghost'
									onClick={() => setIsEditingTitle(false)}
								>
									<X className='h-4 w-4' />
								</Button>
							</div>
						) : (
							<div className='flex items-center gap-2'>
								<p className='font-medium truncate'>{resume.title || resume.originalFileName}</p>
								<button
									type='button'
									onClick={handleStartEditTitle}
									className='shrink-0 text-muted-foreground hover:text-foreground'
								>
									<Pencil className='h-3.5 w-3.5' />
								</button>
							</div>
						)}
						<p className='text-sm text-muted-foreground'>
							{formatFileSize(resume.fileSize)} &middot; {resume.originalFileName}
						</p>
					</div>
				</div>

				<div className='flex items-center gap-2 shrink-0 w-full sm:w-auto'>
					{showDeleteConfirm ? (
						<div className='flex flex-col sm:flex-row items-start sm:items-center gap-2 w-full sm:w-auto'>
							<span className='text-sm text-destructive whitespace-nowrap'>Xác nhận xoá?</span>
							<div className='flex gap-2 w-full sm:w-auto'>
								<Button
									variant='destructive'
									size='sm'
									onClick={handleDelete}
									disabled={deleteResume.isPending}
									className='hover:text-white flex-1 sm:flex-initial'
								>
									{deleteResume.isPending ? (
										<Loader2 className='h-4 w-4 animate-spin' />
									) : (
										<Trash2 className='h-4 w-4' />
									)}
									Xoá
								</Button>
								<Button
									variant='outline'
									size='sm'
									onClick={() => setShowDeleteConfirm(false)}
									className='flex-1 sm:flex-initial'
								>
									Huỷ
								</Button>
							</div>
						</div>
					) : (
						<>
							{pdfUrl && (
								<Button
									variant='outline'
									size='sm'
									onClick={() => {
										window.open(pdfUrl, "_blank", "noopener,noreferrer");
									}}
									className='flex-1 sm:flex-initial'
								>
									<Eye className='h-4 w-4' />
									<span className='hidden sm:inline'>Xem</span>
									<span className='sm:hidden'>Xem</span>
								</Button>
							)}
							<Button
								variant='outline'
								size='sm'
								onClick={() => setShowReplace(!showReplace)}
								className='flex-1 sm:flex-initial'
							>
								<Upload className='h-4 w-4' />
								<span className='hidden sm:inline'>Thay thế</span>
								<span className='sm:hidden'>Thay</span>
							</Button>
							<Button
								variant='outline'
								size='sm'
								onClick={() => setShowDeleteConfirm(true)}
								className='flex-1 sm:flex-initial'
							>
								<Trash2 className='h-4 w-4' />
								<span className='hidden sm:inline'>Xoá</span>
								<span className='sm:hidden'>Xoá</span>
							</Button>
						</>
					)}
				</div>
			</div>

			{showReplace && <CVUploader />}

			{isPdfError ? (
				<p className='text-sm text-destructive'>Không thể tải PDF</p>
			) : pdfData ? (
				<div className='flex flex-col items-center'>
					<ZoomControls
						zoom={zoom}
						setZoom={setZoom}
					/>

					<div
						className='overflow-auto max-h-150 border rounded shadow-sm bg-background w-full flex justify-start md:justify-center'
						ref={containerRef}
					>
						<Document
							file={pdfUrl}
							onLoadSuccess={onDocumentLoadSuccess}
							loading={<Skeleton className='h-96 w-full' />}
							error={<p className='text-sm text-destructive'>Không thể hiển thị PDF</p>}
						>
							<Page
								key={`page-${pageNumber}-${Math.round(zoom * 100)}`}
								pageNumber={pageNumber}
								width={containerWidth}
								scale={zoom}
								devicePixelRatio={window.devicePixelRatio || 1}
								renderAnnotationLayer={false}
								renderTextLayer={false}
							/>
						</Document>
					</div>

					{numPages && numPages > 1 && (
						<div className='flex items-center justify-center gap-2 sm:gap-4 mt-4'>
							<Button
								variant='outline'
								size='sm'
								disabled={pageNumber <= 1}
								onClick={() => setPageNumber((p) => Math.max(p - 1, 1))}
							>
								<ChevronLeft className='h-4 w-4' />
								<span className='hidden sm:inline'>Trang trước</span>
							</Button>
							<span className='text-sm text-muted-foreground'>
								{pageNumber} / {numPages}
							</span>
							<Button
								variant='outline'
								size='sm'
								disabled={pageNumber >= numPages}
								onClick={() => setPageNumber((p) => Math.min(p + 1, numPages))}
							>
								<span className='hidden sm:inline'>Trang sau</span>
								<ChevronRight className='h-4 w-4' />
							</Button>
						</div>
					)}
				</div>
			) : isPdfLoading ? (
				<Skeleton className='h-96 w-full' />
			) : null}
		</div>
	);
}
