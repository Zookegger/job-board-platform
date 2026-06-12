import { Building2, Loader2, Trash2, Upload, X } from "lucide-react";
import { useCallback, useRef, useState } from "react";

import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { useUploadCompanyLogo } from "@/hooks/useProfile";
import { useToast } from "@/providers/ToastProvider";
import getErrorMessage from "@/utils/getErrorMessage";

const ACCEPTED_TYPES = ["image/png", "image/jpeg", "image/svg+xml", "image/webp"];
const MAX_SIZE_BYTES = 2 * 1024 * 1024; // 2 MB

function formatBytes(bytes: number) {
	if (bytes < 1024) return `${bytes} B`;
	if (bytes < 1024 * 1024) return `${(bytes / 1024).toFixed(1)} KB`;
	return `${(bytes / (1024 * 1024)).toFixed(2)} MB`;
}

function getInitials(name: string) {
	return name
		.split(" ")
		.filter(Boolean)
		.slice(0, 2)
		.map((w) => w[0].toUpperCase())
		.join("");
}

type UploadState = "empty" | "preview" | "saved";

interface CompanyLogoUploadProps {
	companyName: string;
	currentLogoUrl?: string | null;
	apiBaseUrl?: string;
}

export default function CompanyLogoUpload({
	companyName,
	currentLogoUrl,
	apiBaseUrl = "http://localhost:8080",
}: CompanyLogoUploadProps) {
	const toast = useToast();
	const uploadLogo = useUploadCompanyLogo();
	const fileInputRef = useRef<HTMLInputElement>(null);

	const [state, setState] = useState<UploadState>(currentLogoUrl ? "saved" : "empty");
	const [previewSrc, setPreviewSrc] = useState<string | null>(null);
	const [selectedFile, setSelectedFile] = useState<File | null>(null);
	const [savedLogoUrl, setSavedLogoUrl] = useState<string | null>(currentLogoUrl ?? null);
	const [validationError, setValidationError] = useState<string | null>(null);
	const [isDragging, setIsDragging] = useState(false);
	const [showRemoveConfirm, setShowRemoveConfirm] = useState(false);

	const validate = (file: File): string | null => {
		if (!ACCEPTED_TYPES.includes(file.type))
			return "Định dạng không hợp lệ. Chỉ chấp nhận PNG, JPG, SVG, WEBP.";
		if (file.size > MAX_SIZE_BYTES)
			return `File quá lớn (${formatBytes(file.size)}). Dung lượng tối đa: 2 MB.`;
		return null;
	};

	const processFile = (file: File) => {
		const error = validate(file);
		if (error) {
			setValidationError(error);
			setPreviewSrc(null);
			setSelectedFile(null);
			setState(savedLogoUrl ? "saved" : "empty");
			return;
		}
		setValidationError(null);
		setSelectedFile(file);
		const reader = new FileReader();
		reader.onload = (e) => {
			setPreviewSrc(e.target?.result as string);
			setState("preview");
		};
		reader.readAsDataURL(file);
	};

	const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
		const file = e.target.files?.[0];
		if (file) processFile(file);
		// Reset so same file can be re-selected
		e.target.value = "";
	};

	const handleDrop = useCallback((e: React.DragEvent<HTMLDivElement>) => {
		e.preventDefault();
		setIsDragging(false);
		const file = e.dataTransfer.files?.[0];
		if (file) processFile(file);
	}, []);

	const handleDragOver = (e: React.DragEvent<HTMLDivElement>) => {
		e.preventDefault();
		setIsDragging(true);
	};

	const handleDragLeave = () => setIsDragging(false);

	const handleSave = async () => {
		if (!selectedFile) return;
		try {
			const url = await uploadLogo.mutateAsync(selectedFile);
			setSavedLogoUrl(url);
			setState("saved");
			setPreviewSrc(null);
			setSelectedFile(null);
			toast.success("Logo saved successfully");
		} catch (err) {
			toast.error(getErrorMessage(err));
		}
	};

	const handleCancel = () => {
		setPreviewSrc(null);
		setSelectedFile(null);
		setValidationError(null);
		setState(savedLogoUrl ? "saved" : "empty");
	};

	const handleRemove = () => {
		setSavedLogoUrl(null);
		setState("empty");
		setShowRemoveConfirm(false);
		toast.success("Logo đã được xóa");
	};

	const resolveLogoUrl = (url: string) =>
		url.startsWith("/uploads/") ? `${apiBaseUrl}${url}` : url;

	const isUploading = uploadLogo.isPending;

	return (
		<Card>
			<CardHeader>
				<CardTitle>Logo công ty</CardTitle>
			</CardHeader>
			<CardContent className="space-y-6">
				<div className="flex flex-col items-center gap-6 sm:flex-row sm:items-start">
					{/* Logo display area */}
					<div className="flex-shrink-0">
						<div className="relative flex h-32 w-32 items-center justify-center overflow-hidden rounded-2xl bg-muted ring-1 ring-foreground/10">
							{state === "preview" && previewSrc ? (
								<img
									src={previewSrc}
									alt="Logo preview"
									className="h-full w-full object-contain p-2"
								/>
							) : state === "saved" && savedLogoUrl ? (
								<img
									src={resolveLogoUrl(savedLogoUrl)}
									alt={`${companyName} logo`}
									className="h-full w-full object-contain p-2"
								/>
							) : (
								<div className="flex flex-col items-center gap-1 text-muted-foreground">
									<Building2 className="h-8 w-8" />
									<span className="text-sm font-semibold tracking-wide">
										{getInitials(companyName)}
									</span>
								</div>
							)}

							{/* Uploading overlay */}
							{isUploading && (
								<div className="absolute inset-0 flex items-center justify-center rounded-2xl bg-black/50">
									<Loader2 className="h-6 w-6 animate-spin text-white" />
								</div>
							)}
						</div>

						{/* File info under logo */}
						{state === "preview" && selectedFile && (
							<p className="mt-2 max-w-[128px] truncate text-center text-xs text-muted-foreground">
								{selectedFile.name}
								<br />
								<span className="text-xs">{formatBytes(selectedFile.size)}</span>
							</p>
						)}
					</div>

					{/* Right side: drop zone or actions */}
					<div className="flex w-full flex-col gap-3">
						{/* Drop zone — always shown when not uploading */}
						{!isUploading && (
							<div
								onDrop={handleDrop}
								onDragOver={handleDragOver}
								onDragLeave={handleDragLeave}
								onClick={() => fileInputRef.current?.click()}
								className={[
									"flex cursor-pointer flex-col items-center justify-center gap-2 rounded-xl border-2 border-dashed px-4 py-6 text-center transition-colors",
									isDragging
										? "border-primary bg-primary/5"
										: "border-border hover:border-primary/60 hover:bg-muted/40",
								].join(" ")}
							>
								<Upload className="h-5 w-5 text-muted-foreground" />
								<p className="text-sm font-medium">
									Drag your logo here, or{" "}
									<span className="text-primary underline underline-offset-2">click to browse</span>
								</p>
								<p className="text-xs text-muted-foreground">
									PNG, JPG, SVG or WEBP · Max 2 MB
								</p>
								<p className="text-xs text-muted-foreground">
									Recommended: 400×400 px or larger
								</p>
							</div>
						)}

						<input
							ref={fileInputRef}
							type="file"
							accept="image/png,image/jpeg,image/svg+xml,image/webp"
							className="hidden"
							onChange={handleFileChange}
						/>

						{/* Validation error */}
						{validationError && (
							<div className="flex items-start gap-2 rounded-lg bg-destructive/10 px-3 py-2 text-sm text-destructive">
								<X className="mt-0.5 h-4 w-4 flex-shrink-0" />
								{validationError}
							</div>
						)}

						{/* Action buttons */}
						{state === "preview" && (
							<div className="flex gap-2">
								<Button
									onClick={handleSave}
									disabled={isUploading}
									className="flex-1"
								>
									{isUploading ? (
										<>
											<Loader2 className="mr-2 h-4 w-4 animate-spin" />
											Đang lưu...
										</>
									) : (
										"Upload Logo"
									)}
								</Button>
								<Button
									variant="outline"
									onClick={handleCancel}
									disabled={isUploading}
								>
									Huỷ
								</Button>
							</div>
						)}

						{state === "saved" && (
							<div className="flex gap-2">
								<Button
									variant="outline"
									onClick={() => fileInputRef.current?.click()}
									className="flex-1"
								>
									<Upload className="mr-2 h-4 w-4" />
									Thay đổi logo
								</Button>
								<Button
									variant="outline"
									onClick={() => setShowRemoveConfirm(true)}
									className="text-destructive hover:text-destructive"
								>
									<Trash2 className="h-4 w-4" />
								</Button>
							</div>
						)}

						{state === "empty" && !validationError && (
							<Button
								variant="outline"
								onClick={() => fileInputRef.current?.click()}
								className="w-full"
							>
								<Upload className="mr-2 h-4 w-4" />
								Upload Logo
							</Button>
						)}
					</div>
				</div>

				{/* Remove confirmation */}
				{showRemoveConfirm && (
					<div className="rounded-xl border border-destructive/30 bg-destructive/5 p-4">
						<p className="mb-3 text-sm font-medium text-destructive">
							Remove logo? This can&apos;t be undone.
						</p>
						<div className="flex gap-2">
							<Button
								variant="destructive"
								size="sm"
								onClick={handleRemove}
							>
								Xác nhận xóa
							</Button>
							<Button
								variant="outline"
								size="sm"
								onClick={() => setShowRemoveConfirm(false)}
							>
								Huỷ
							</Button>
						</div>
					</div>
				)}
			</CardContent>
		</Card>
	);
}
