import { BaseDialog } from "@/components/shared/BaseDialog";
import { DataTable } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Sheet, SheetContent, SheetFooter, SheetHeader, SheetTitle } from "@/components/ui/sheet";
import { useApproveCompany, usePendingCompanies, useRejectCompany } from "@/hooks/useAdminCompanies";
import { CompanyStatus, type AdminPendingCompanyResponse } from "@/types/company";
import getErrorMessage from "@/utils/getErrorMessage";
import { Building2, CheckCircle2, ExternalLink, Mail, MapPin, Phone, RefreshCw, Search, XCircle } from "lucide-react";
import { useDeferredValue, useMemo, useState } from "react";
import { toast } from "sonner";

const DEFAULT_PAGE_SIZE = 10;

type TaxCodeFilter = "all" | "with-tax-code" | "missing-tax-code";
type ContactFilter = "all" | "with-contact" | "missing-contact";
type SortOption = "newest" | "oldest" | "name";

const sortConfig: Record<SortOption, { sortBy: "createdAt" | "companyName"; direction: "asc" | "desc" }> = {
	newest: { sortBy: "createdAt", direction: "desc" },
	oldest: { sortBy: "createdAt", direction: "asc" },
	name: { sortBy: "companyName", direction: "asc" },
};

function toBooleanFilter<T extends string>(value: T, trueValue: T, falseValue: T) {
	if (value === trueValue) return true;
	if (value === falseValue) return false;
	return undefined;
}

function formatDate(value: string | null) {
	if (!value) return "Chưa có";
	return new Intl.DateTimeFormat("vi-VN", {
		dateStyle: "medium",
		timeStyle: "short",
	}).format(new Date(value));
}

function fallbackText(value: string | null | undefined, fallback = "Chưa cập nhật") {
	return value?.trim() || fallback;
}

function PendingBadge() {
	return (
		<Badge
			variant='outline'
			className='border-warning/40 bg-warning/20 text-warning-foreground'
		>
			Đang chờ duyệt
		</Badge>
	);
}

function CompanyLogo({ company }: { company: AdminPendingCompanyResponse }) {
	return (
		<div className='flex size-11 shrink-0 items-center justify-center overflow-hidden rounded-lg border bg-muted'>
			{company.logoUrl ? (
				<img
					src={company.logoUrl}
					alt={company.companyName}
					className='h-full w-full object-cover'
				/>
			) : (
				<Building2 className='size-5 text-muted-foreground' />
			)}
		</div>
	);
}

export default function AdminCompaniesPage() {
	const [page, setPage] = useState(0);
	const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE);
	const [searchTerm, setSearchTerm] = useState("");
	const [taxCodeFilter, setTaxCodeFilter] = useState<TaxCodeFilter>("all");
	const [contactFilter, setContactFilter] = useState<ContactFilter>("all");
	const [sortOption, setSortOption] = useState<SortOption>("newest");
	const deferredSearch = useDeferredValue(searchTerm.trim());

	function handleSearchChange(keyword: string) {
		setSearchTerm(keyword);
		setPage(0);
	}

	function handleTaxCodeFilterChange(filter: TaxCodeFilter) {
		setTaxCodeFilter(filter);
		setPage(0);
	}

	function handleContactFilterChange(filter: ContactFilter) {
		setContactFilter(filter);
		setPage(0);
	}

	function handleSortChange(option: SortOption) {
		setSortOption(option);
		setPage(0);
	}

	const [approveDialog, setApproveDialog] = useState<{
		open: boolean;
		company: AdminPendingCompanyResponse | null;
	}>({ open: false, company: null });

	const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
	const [selectedCompanyToReject, setSelectedCompanyToReject] = useState<AdminPendingCompanyResponse | null>(null);
	const [rejectReason, setRejectReason] = useState("");

	const queryParams = useMemo(() => {
		const sort = sortConfig[sortOption];
		return {
			page,
			size: pageSize,
			keyword: deferredSearch,
			hasTaxCode: toBooleanFilter(taxCodeFilter, "with-tax-code", "missing-tax-code"),
			hasContact: toBooleanFilter(contactFilter, "with-contact", "missing-contact"),
			sortBy: sort.sortBy,
			direction: sort.direction,
		};
	}, [contactFilter, deferredSearch, page, pageSize, sortOption, taxCodeFilter]);

	const { data, isError, isFetching, isLoading, refetch, error } = usePendingCompanies(queryParams);
	const approveCompany = useApproveCompany();
	const rejectCompany = useRejectCompany();

	const companies = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;

	const actionPending = approveCompany.isPending || rejectCompany.isPending;

	const handleApprove = (company: AdminPendingCompanyResponse) => {
		setApproveDialog({ open: true, company });
	};

	const confirmApprove = () => {
		if (!approveDialog.company) return;

		approveCompany.mutate(approveDialog.company.id, {
			onSuccess: () => {
				toast.success(`Đã duyệt công ty "${approveDialog.company!.companyName}"`);
				setApproveDialog({ open: false, company: null });
			},
			onError: (mutationError) => toast.error(getErrorMessage(mutationError, "Không thể duyệt công ty")),
		});
	};

	const openRejectDialog = (company: AdminPendingCompanyResponse) => {
		setSelectedCompanyToReject(company);
		setRejectReason("");
		setRejectDialogOpen(true);
	};

	const handleReject = () => {
		if (!selectedCompanyToReject) return;

		if (!rejectReason.trim()) {
			toast.error("Vui lòng nhập lý do từ chối");
			return;
		}

		rejectCompany.mutate(
			{ companyId: selectedCompanyToReject.id, reason: rejectReason.trim() },
			{
				onSuccess: () => {
					toast.success("Đã từ chối công ty");
					setRejectDialogOpen(false);
				},
				onError: (mutationError) => toast.error(getErrorMessage(mutationError, "Không thể từ chối công ty")),
			},
		);
	};

	return (
		<div className='mx-auto flex w-full max-w-7xl flex-col gap-5'>
			<div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
				<div>
					<h1 className='text-2xl font-semibold text-foreground'>Công ty chờ phê duyệt</h1>
					<p className='mt-1 text-sm text-muted-foreground'>
						{data?.totalElements.toLocaleString("vi-VN")} hồ sơ đang cần ADMIN xem xét
					</p>
				</div>
				<Button
					variant='outline'
					onClick={() => refetch()}
					disabled={isFetching}
					className='w-fit'
				>
					<RefreshCw className={isFetching ? "animate-spin" : ""} />
					Làm mới
				</Button>
			</div>

			<div className='rounded-lg border bg-card p-4'>
				<div className='grid gap-3 md:grid-cols-[minmax(260px,1fr)_180px_180px_180px]'>
					<Input
						value={searchTerm}
						onChange={(event) => handleSearchChange(event.target.value)}
						placeholder='Tìm theo tên, email, MST, địa chỉ...'
						startIcon={<Search className='size-4' />}
						className='h-10 bg-background'
					/>

					<select
						value={taxCodeFilter}
						onChange={(event) => handleTaxCodeFilterChange(event.target.value as TaxCodeFilter)}
						className='h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
					>
						<option value='all'>Tất cả MST</option>
						<option value='with-tax-code'>Có MST</option>
						<option value='missing-tax-code'>Thiếu MST</option>
					</select>

					<select
						value={contactFilter}
						onChange={(event) => handleContactFilterChange(event.target.value as ContactFilter)}
						className='h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
					>
						<option value='all'>Tất cả liên hệ</option>
						<option value='with-contact'>Có liên hệ</option>
						<option value='missing-contact'>Thiếu liên hệ</option>
					</select>

					<select
						value={sortOption}
						onChange={(event) => handleSortChange(event.target.value as SortOption)}
						className='h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
					>
						<option value='newest'>Mới nhất</option>
						<option value='oldest'>Cũ nhất</option>
						<option value='name'>Tên A-Z</option>
					</select>
				</div>
			</div>

			<DataTable
				columns={[
					{
						key: "company",
						header: "Công ty",
						className: "align-top",
						render: (c) => (
							<div className='flex gap-3'>
								<CompanyLogo company={c} />
								<div className='min-w-0'>
									<div className='flex flex-wrap items-center gap-2'>
										<p className='font-medium text-foreground'>{c.companyName}</p>
										{c.status === CompanyStatus.PENDING && <PendingBadge />}
									</div>
									<div className='mt-1 flex items-center gap-1 text-xs text-muted-foreground'>
										<MapPin className='size-3.5' />
										<span className='line-clamp-1'>{fallbackText(c.address)}</span>
									</div>
									{c.website && (
										<a
											href={c.website}
											target='_blank'
											rel='noreferrer'
											className='mt-2 inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline'
										>
											Website <ExternalLink className='size-3' />
										</a>
									)}
								</div>
							</div>
						),
					},
					{
						key: "employer",
						header: "Người phụ trách",
						className: "align-top",
						render: (c) => (
							<>
								<p className='font-medium'>{fallbackText(c.employerName)}</p>
								<p className='mt-1 text-xs text-muted-foreground'>
									{fallbackText(c.roleInCompany, "Vai trò chưa cập nhật")}
								</p>
								<p className='mt-2 break-all text-xs text-muted-foreground'>
									{fallbackText(c.employerEmail)}
								</p>
							</>
						),
					},
					{
						key: "contact",
						header: "Liên hệ",
						className: "align-top",
						render: (c) => (
							<div className='space-y-2'>
								<div className='flex items-center gap-2 text-sm'>
									<Mail className='size-4 text-muted-foreground' />
									<span className='break-all'>{fallbackText(c.email)}</span>
								</div>
								<div className='flex items-center gap-2 text-sm'>
									<Phone className='size-4 text-muted-foreground' />
									<span>{fallbackText(c.phone || c.employerPhone)}</span>
								</div>
							</div>
						),
					},
					{
						key: "legal",
						header: "Thông tin pháp lý",
						className: "align-top",
						render: (c) => (
							<>
								<p className='font-medium'>{fallbackText(c.taxCode, "Thiếu MST")}</p>
								<p className='mt-1 line-clamp-2 max-w-55 text-xs text-muted-foreground'>
									{fallbackText(c.description, "Chưa có mô tả")}
								</p>
							</>
						),
					},
					{
						key: "createdAt",
						header: "Ngày đăng ký",
						className: "align-top text-sm text-muted-foreground",
						render: (c) => formatDate(c.createdAt),
					},
					{
						key: "actions",
						header: "Xử lý",
						className: "align-top",
						render: (c) => (
							<div className='flex flex-wrap gap-2'>
								<Button
									variant='success'
									size='sm'
									disabled={actionPending}
									onClick={() => handleApprove(c)}
								>
									<CheckCircle2 /> Duyệt
								</Button>
								<Button
									variant='destructive'
									size='sm'
									disabled={actionPending}
									onClick={() => openRejectDialog(c)}
								>
									<XCircle /> Từ chối
								</Button>
							</div>
						),
					},
				]}
				data={companies}
				isLoading={isLoading}
				isError={isError}
				error={error}
				onRetry={() => refetch()}
				emptyState={{
					icon: Building2,
					title: "Không có công ty chờ duyệt",
					subtitle: "Thay đổi bộ lọc hoặc tìm kiếm để xem kết quả khác.",
				}}
				pageResponse={data}
				pageable={{
					page,
					pageSize,
					totalPages,
					totalElements,
					onPageChange: setPage,
					onPageSizeChange: (newSize) => {
						setPageSize(newSize);
						setPage(0);
					},
					isFetching,
					label: "công ty",
				}}
				minWidth='min-w-[1040px]'
			/>

			<Sheet
				open={rejectDialogOpen}
				onOpenChange={(open) => {
					if (!open) {
						setSelectedCompanyToReject(null);
						setRejectReason("");
					}
					setRejectDialogOpen(open);
				}}
			>
				<SheetContent
					side='bottom'
					className='max-w-xl'
				>
					<SheetHeader>
						<SheetTitle>Từ chối hồ sơ công ty</SheetTitle>
						<p className='text-sm text-muted-foreground'>
							Nhập lý do từ chối cho công ty {selectedCompanyToReject?.companyName ?? ""}.
						</p>
					</SheetHeader>
					<div className='space-y-4 px-4'>
						<label className='block text-sm font-medium text-foreground'>Lý do từ chối</label>
						<textarea
							value={rejectReason}
							onChange={(event) => setRejectReason(event.target.value)}
							rows={6}
							placeholder='Nhập lý do từ chối'
							className='w-full rounded-md border border-input bg-background p-3 text-sm text-foreground outline-none transition focus:border-ring focus:ring-2 focus:ring-ring/50'
						/>
					</div>
					<SheetFooter>
						<div className='flex flex-col gap-3 sm:flex-row sm:justify-end'>
							<Button
								variant='secondary'
								onClick={() => setRejectDialogOpen(false)}
							>
								Hủy
							</Button>
							<Button
								variant='destructive'
								size='sm'
								onClick={handleReject}
								disabled={actionPending}
							>
								<XCircle />
								Xác nhận từ chối
							</Button>
						</div>
					</SheetFooter>
				</SheetContent>
			</Sheet>

			{/* Approve Confirm Dialog */}
			<BaseDialog
				isOpen={approveDialog.open}
				onClose={() => setApproveDialog({ open: false, company: null })}
				title='Xác nhận duyệt công ty'
				description={`Bạn có chắc muốn duyệt công ty "${approveDialog.company?.companyName ?? ""}"?`}
				footer={
					<div className='flex gap-3'>
						<Button
							variant='outline'
							onClick={() => setApproveDialog({ open: false, company: null })}
						>
							Hủy
						</Button>
						<Button
							variant='success'
							onClick={confirmApprove}
							disabled={actionPending}
						>
							<CheckCircle2 /> Xác nhận duyệt
						</Button>
					</div>
				}
			/>
		</div>
	);
}
