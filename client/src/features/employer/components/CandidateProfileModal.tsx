import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import {
	Dialog,
	DialogClose,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import type { CandidateApplicationListResponse } from "@/types/application";
import { ExternalLink, FileX, Loader2, User, X } from "lucide-react";
import { useState } from "react";
import { UpdateStatusDialog } from "./UpdateStatusDialog";

function normalizeResumeUrl(raw: string | null): string | null {
	if (!raw) return null;
	const normalized = raw.replace(/\\/g, "/");
	if (normalized.startsWith("/uploads/")) return normalized;
	const idx = normalized.indexOf("uploads/");
	if (idx !== -1) return "/" + normalized.substring(idx);
	return normalized;
}

interface CvPreviewModalProps {
	candidate?: CandidateApplicationListResponse;
	resumeUrl: string | null;
	open: boolean;
	onClose: () => void;
}

function EmbeddedResumeViewer({ url }: { url: string }) {
	const [loading, setLoading] = useState(true);
	const [loadError, setLoadError] = useState(false);

	const resolvedUrl = normalizeResumeUrl(url);

	if (!resolvedUrl) {
		return (
			<div className='flex flex-col items-center justify-center gap-3 py-16 text-muted-foreground'>
				<FileX className='size-12 opacity-40' />
				<p className='text-sm'>Ứng viên chưa tải lên CV.</p>
			</div>
		);
	}
	return (
		<div className='flex flex-col gap-3 h-full'>
			<div className='mb-3 flex items-center justify-between'>
				<p className='text-xs text-muted-foreground'>Nhúng trực tiếp — file PDF sẽ hiển thị bên dưới.</p>
				<div className='flex gap-2'>
					<Button
						variant='ghost'
						size='sm'
						asChild
					>
						<a
							href={resolvedUrl}
							target='_blank'
							rel='noopener noreferrer'
						>
							<ExternalLink className='mr-1.5 size-3.5' />
							Mở tab mới
						</a>
					</Button>
					<Button
						variant='outline'
						size='sm'
						asChild
					>
						<a
							href={resolvedUrl}
							target='_blank'
							rel='noopener noreferrer'
						>
							<ExternalLink className='mr-1.5 size-3.5' />
							Tải xuống để xem
						</a>
					</Button>
				</div>
			</div>

			<div className='h-[50vh] md:flex-1 rounded-md border bg-muted/30'>
				{loading && !loadError && (
					<div className='absolute inset-0 flex flex-col items-center justify-center gap-2 text-muted-foreground'>
						<Loader2 className='size-6 animate-spin' />
						<p className='text-sm'>Đang tải tài liệu...</p>
					</div>
				)}

				{loadError && (
					<div className='absolute inset-0 flex flex-col items-center justify-center gap-2 text-muted-foreground'>
						<FileX className='size-10 opacity-40' />
						<p className='text-sm font-medium'>Không thể hiển thị tài liệu.</p>
					</div>
				)}

				<iframe
					key={resolvedUrl}
					src={resolvedUrl}
					className='h-full w-full'
					allow='fullscreen'
					onLoad={() => setLoading(false)}
					onError={() => {
						setLoading(false);
						setLoadError(true);
					}}
					style={{ display: loadError ? "none" : "block" }}
				/>
			</div>
		</div>
	);
}

export function CandidateProfileModal({ candidate, resumeUrl, open, onClose }: CvPreviewModalProps) {
	const [dialogOpen, setDialogOpen] = useState(false);

	if (!candidate) return null;

	const hasCoverLetter = !!candidate.coverLetter;

	return (
		<>
			<Dialog
				open={open}
				onOpenChange={(open) => {
					if (!open) onClose();
				}}
				modal
				
			>
				<DialogContent
					className='max-w-7xl h-[95vh] md:h-[90vh] md:overflow-hidden overflow-auto'
					showCloseButton={false}
				>
					<DialogHeader className='sticky top-0 bg-background z-50'>
						<DialogTitle>CV của {candidate.candidateName}</DialogTitle>
						<DialogDescription>
							Xem trước thông tin ứng viên. Bạn có thể mở CV trong tab mới hoặc tải xuống.
						</DialogDescription>
						<DialogClose asChild>
							<Button
								variant='ghost'
								size='icon-sm'
								className='absolute top-4 right-4'
							>
								<X />
								<span className='sr-only'>Close</span>
							</Button>
						</DialogClose>
					</DialogHeader>

					<div className='grid grid-cols-1 not-md:grid-rows-[auto_1fr] flex-1 gap-0 md:grid-cols-[auto_1fr] '>
						{/* Left: Profile info */}
						<div className='flex md:flex-col flex-row gap-5 border-b not-md:mb-4 px-4 py-4 md:border-r'>
							<div className='not-md:flex-2'>
								<div className='flex items-center gap-3'>
									<div className='flex size-14 shrink-0 items-center justify-center overflow-hidden rounded-full border bg-muted/50'>
										{candidate.candidateAvatarUrl ? (
											<img
												src={candidate.candidateAvatarUrl}
												alt={candidate.candidateName}
												className='h-full w-full object-cover'
											/>
										) : (
											<User className='size-6 text-muted-foreground/70' />
										)}
									</div>
									<div className='min-w-0'>
										<h3 className='truncate text-lg font-semibold'>{candidate.candidateName}</h3>
									</div>
								</div>
								<div className='space-y-1.5'>
									<div className='flex items-center gap-2 text-sm'>
										<span className='text-muted-foreground'>Email:</span>
										<span className='truncate'>{candidate.candidateEmail}</span>
									</div>
									{candidate.candidatePhone && (
										<div className='flex items-center gap-2 text-sm'>
											<span className='text-muted-foreground'>SĐT:</span>
											<span>{candidate.candidatePhone}</span>
										</div>
									)}
								</div>
							</div>

							{candidate.skills?.length > 0 && (
								<div className='not-md:flex-1'>
									<h4 className='mb-2 text-xs font-medium uppercase tracking-wide text-muted-foreground'>
										Kỹ năng
									</h4>
									<div className='flex flex-wrap gap-1.5'>
										{candidate.skills.map((skill) => (
											<Badge
												key={skill.skillId}
												variant='secondary'
											>
												{skill.skillName}
											</Badge>
										))}
									</div>
								</div>
							)}
						</div>

						{/* Right: Tabs */}
						<div className='flex flex-1 h-full flex-col px-4 py-4 overflow-y-auto'>
							<Tabs
								defaultValue='cv'
								className='flex-1 not-md:h-full'
							>
								<TabsList>
									<TabsTrigger value='cv'>CV</TabsTrigger>
									{hasCoverLetter && <TabsTrigger value='coverLetter'>Thư xin việc</TabsTrigger>}
								</TabsList>

								<TabsContent
									value='cv'
									className='mt-4'
								>
									<EmbeddedResumeViewer url={resumeUrl ?? ""} />
								</TabsContent>

								{hasCoverLetter && (
									<TabsContent
										value='coverLetter'
										className='mt-4'
									>
										<div className='rounded-md border bg-muted/20 p-4'>
											<p className='whitespace-pre-wrap text-sm leading-relaxed text-foreground'>
												{candidate.coverLetter}
											</p>
										</div>
									</TabsContent>
								)}
							</Tabs>
						</div>
					</div>
					<DialogFooter>
						<Button
							variant='secondary'
							onClick={() => setDialogOpen(true)}
						>
							Cập nhật trạng thái
						</Button>
					</DialogFooter>
				</DialogContent>
			</Dialog>

			<UpdateStatusDialog
				application={candidate}
				open={dialogOpen}
				onOpenChange={setDialogOpen}
			/>
		</>
	);
}
