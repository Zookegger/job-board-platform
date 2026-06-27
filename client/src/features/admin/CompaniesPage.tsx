import { DataTable } from "@/components/shared/DataTable";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import CompanyApprovalModal from "@/features/admin/components/CompanyApprovalModal";
import {
	useAllCompanies,
	usePendingCompanies,
	useUnsuspendCompany,
} from "@/hooks/useAdminCompanies";
import {
	CompanyStatus,
	type AdminCompanyListResponse,
	type AdminPendingCompanyResponse,
	type CompanyResponse,
} from "@/types/company";
import getErrorMessage from "@/utils/getErrorMessage";
import {
	AlertTriangle,
	Building2,
	CheckCircle2,
	Clock,
	ExternalLink,
	Mail,
	MapPin,
	Phone,
	RefreshCw,
	RotateCcw,
	Search,
	XCircle,
} from "lucide-react";
import { useCallback, useDeferredValue, useMemo, useState } from "react";
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

const STATUS_FILTER_OPTIONS = [
	{ value: "", label: "Tất cả trạng thái" },
	{ value: "PENDING", label: "Chờ duyệt" },
	{ value: "APPROVED", label: "Đã duyệt" },
	{ value: "REJECTED", label: "Từ chối" },
	{ value: "SUSPENDED", label: "Tạm ngưng" },
] as const;

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

function StatusBadge({ status }: { status: CompanyStatus }) {
	const styles: Record<CompanyStatus, string> = {
		PENDING: "border-warning/50 bg-warning/15 text-warning font-medium shadow-sm",
		APPROVED: "border-success/50 bg-success/15 text-success font-medium shadow-sm",
		REJECTED: "border-destructive/50 bg-destructive/15 text-destructive font-medium shadow-sm",
		SUSPENDED: "border-muted-foreground/30 bg-muted text-muted-foreground font-medium shadow-sm",
	};
	const labels: Record<CompanyStatus, string> = {
		PENDING: "Chờ duyệt",
		APPROVED: "Đã duyệt",
		REJECTED: "Từ chối",
		SUSPENDED: "Tạm ngưng",
	};
	return (
		<Badge
			variant='outline'
			className={styles[status]}
		>
			{labels[status]}
		</Badge>
	);
}

function CompanyLogo({ company }: { company: { logoUrl: string | null; companyName: string } }) {
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
	const [activeTab, setActiveTab] = useState("pending");

	// ── Pending tab state ──
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

	// ── All tab state ──
	const [allPage, setAllPage] = useState(0);
	const [allPageSize, setAllPageSize] = useState(DEFAULT_PAGE_SIZE);
	const [allSearchTerm, setAllSearchTerm] = useState("");
	const [allStatusFilter, setAllStatusFilter] = useState("");
	const allDeferredSearch = useDeferredValue(allSearchTerm.trim());

	// ── Shared dialog state ──
	const [approvalAction, setApprovalAction] = useState<{
		company: AdminPendingCompanyResponse | CompanyResponse;
		action: "approve" | "reject" | "suspend";
	} | null>(null);

	// ── Queries ──
	const pendingQueryParams = useMemo(() => {
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

	const allQueryParams = useMemo(
		() => ({
			page: allPage,
			size: allPageSize,
			keyword: allDeferredSearch,
			status: allStatusFilter || undefined,
		}),
		[allDeferredSearch, allPage, allPageSize, allStatusFilter],
	);

	const {
		data: pendingData,
		isError: pendingIsError,
		isFetching: pendingIsFetching,
		isLoading: pendingIsLoading,
		refetch: pendingRefetch,
		error: pendingError,
	} = usePendingCompanies(pendingQueryParams);
	const {
		data: allData,
		isError: allIsError,
		isFetching: allIsFetching,
		isLoading: allIsLoading,
		refetch: allRefetch,
		error: allError,
	} = useAllCompanies(allQueryParams);

	const unsuspendCompany = useUnsuspendCompany();

	const pendingCompanies = pendingData?.content ?? [];
	const pendingTotalElements = pendingData?.totalElements ?? 0;
	const pendingTotalPages = pendingData?.totalPages ?? 0;

	const allCompanies = allData?.content ?? [];
	const allTotalElements = allData?.totalElements ?? 0;
	const allTotalPages = allData?.totalPages ?? 0;

	const actionPending = unsuspendCompany.isPending;

	// ── Handlers ──
	function openApprovalAction(company: AdminPendingCompanyResponse | CompanyResponse, action: "approve" | "reject" | "suspend") {
		setApprovalAction({ company, action });
	}

	const handleUnsuspend = useCallback(
		(company: CompanyResponse) => {
			unsuspendCompany.mutate(company.id, {
				onSuccess: () => {
					toast.success(`Đã mở tạm ngưng công ty "${company.companyName}"`);
				},
				onError: (mutationError) =>
					toast.error(getErrorMessage(mutationError, "Không thể mở tạm ngưng công ty")),
			});
		},
		[unsuspendCompany],
	);

	const pendingColumns = useMemo(
		() => [
			{
				key: "company",
				header: "Công ty",
				className: "align-top",
				render: (c: AdminPendingCompanyResponse) => (
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
				render: (c: AdminPendingCompanyResponse) => (
					<>
						<p className='font-medium'>{fallbackText(c.employerName)}</p>
						<p className='mt-1 text-xs text-muted-foreground'>
							{fallbackText(c.roleInCompany, "Vai trò chưa cập nhật")}
						</p>
						<p className='mt-2 break-all text-xs text-muted-foreground'>{fallbackText(c.employerEmail)}</p>
					</>
				),
			},
			{
				key: "contact",
				header: "Liên hệ",
				className: "align-top",
				render: (c: AdminPendingCompanyResponse) => (
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
				render: (c: AdminPendingCompanyResponse) => (
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
				render: (c: AdminPendingCompanyResponse) => formatDate(c.createdAt),
			},
			{
				key: "actions",
				header: "Xử lý",
				className: "align-top",
				render: (c: AdminPendingCompanyResponse) => (
					<div className='flex flex-wrap gap-2'>
						<Button
							variant='success'
							size='sm'
							disabled={actionPending}
							onClick={() => openApprovalAction(c, "approve")}
						>
							<CheckCircle2 /> Duyệt
						</Button>
						<Button
							variant='destructive'
							size='sm'
							disabled={actionPending}
							onClick={() => openApprovalAction(c, "reject")}
						>
							<XCircle /> Từ chối
						</Button>
					</div>
				),
			},
		],
		[actionPending],
	);

	const allColumns = useMemo(
		() => [
			{
				key: "company",
				header: "Công ty",
				className: "align-top",
				render: (c: AdminCompanyListResponse) => (
					<div className='flex gap-3'>
						<CompanyLogo company={c} />
						<div className='min-w-0'>
							<div className='flex flex-wrap items-center gap-2'>
								<p className='font-medium text-foreground'>{c.companyName}</p>
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
				key: "contact",
				header: "Liên hệ",
				className: "align-top",
				render: (c: AdminCompanyListResponse) => (
					<div className='space-y-2'>
						<div className='flex items-center gap-2 text-sm'>
							<Mail className='size-4 text-muted-foreground' />
							<span className='break-all'>{fallbackText(c.email)}</span>
						</div>
						<div className='flex items-center gap-2 text-sm'>
							<Phone className='size-4 text-muted-foreground' />
							<span>{fallbackText(c.phone)}</span>
						</div>
					</div>
				),
			},
			{
				key: "taxCode",
				header: "Mã số thuế",
				className: "align-top text-sm text-muted-foreground",
				render: (c: AdminCompanyListResponse) => fallbackText(c.taxCode, "Thiếu MST"),
			},
			{
				key: "status",
				header: "Trạng thái",
				className: "align-top",
				render: (c: AdminCompanyListResponse) => <StatusBadge status={c.status} />,
			},
			{
				key: "createdAt",
				header: "Ngày tạo",
				className: "align-top text-sm text-muted-foreground",
				render: (c: AdminCompanyListResponse) => formatDate(c.createdAt),
			},
			{
				key: "actions",
				header: "Thao tác",
				className: "align-top",
				render: (c: AdminCompanyListResponse) => (
					<div className='flex flex-wrap gap-2'>
						{c.status === CompanyStatus.PENDING && (
							<>
								<Button
									variant='success'
									size='sm'
									disabled={actionPending}
									onClick={() => openApprovalAction(c, "approve")}
								>
									<CheckCircle2 /> Duyệt
								</Button>
								<Button
									variant='destructive'
									size='sm'
									disabled={actionPending}
									onClick={() => openApprovalAction(c, "reject")}
								>
									<XCircle /> Từ chối
								</Button>
							</>
						)}
						{c.status === CompanyStatus.APPROVED && (
							<Button
								variant='secondary'
								size='sm'
								disabled={actionPending}
								onClick={() => openApprovalAction(c, "suspend")}
							>
								<AlertTriangle /> Tạm ngưng
							</Button>
						)}
						{c.status === CompanyStatus.SUSPENDED && (
							<Button
								variant='outline'
								size='sm'
								disabled={actionPending}
								onClick={() => handleUnsuspend(c)}
							>
								<RotateCcw /> Mở tạm ngưng
							</Button>
						)}
						{c.status !== CompanyStatus.PENDING &&
							c.status !== CompanyStatus.APPROVED &&
							c.status !== CompanyStatus.SUSPENDED && (
								<span className='text-xs text-muted-foreground'>—</span>
							)}
					</div>
				),
			},
		],
		[actionPending, handleUnsuspend],
	);

	return (
		<Tabs
			value={activeTab}
			onValueChange={setActiveTab}
			className='mx-auto flex w-full max-w-7xl flex-col gap-5'
		>
			<div className='flex flex-col gap-3 md:flex-row md:items-start md:justify-between'>
				<div>
					<h1 className='text-2xl font-semibold text-foreground'>Quản lý công ty</h1>
					<p className='mt-1 text-sm text-muted-foreground'>
						{activeTab === "pending"
							? `${pendingTotalElements.toLocaleString("vi-VN")} hồ sơ đang cần xem xét`
							: `Tổng số ${allTotalElements.toLocaleString("vi-VN")} công ty`}
					</p>
				</div>
				<Button
					variant='outline'
					onClick={() => (activeTab === "pending" ? pendingRefetch() : allRefetch())}
					disabled={activeTab === "pending" ? pendingIsFetching : allIsFetching}
					className='w-fit'
				>
					<RefreshCw
						className={
							activeTab === "pending"
								? pendingIsFetching
									? "animate-spin"
									: ""
								: allIsFetching
									? "animate-spin"
									: ""
						}
					/>
					Làm mới
				</Button>
			</div>

			<TabsList className='w-fit'>
				<TabsTrigger
					value='pending'
					className='flex items-center gap-2'
				>
					<Clock className='size-4' /> Chờ duyệt
				</TabsTrigger>
				<TabsTrigger
					value='all'
					className='flex items-center gap-2'
				>
					<Building2 className='size-4' /> Tất cả
				</TabsTrigger>
			</TabsList>

			<TabsContent
				value='pending'
				className='mt-0 flex flex-col gap-5'
			>
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
					columns={pendingColumns}
					data={pendingCompanies}
					isLoading={pendingIsLoading}
					isError={pendingIsError}
					error={pendingError}
					onRetry={() => pendingRefetch()}
					emptyState={{
						icon: Building2,
						title: "Không có công ty chờ duyệt",
						subtitle: "Thay đổi bộ lọc hoặc tìm kiếm để xem kết quả khác.",
					}}
					pageResponse={pendingData}
					pageable={{
						page,
						pageSize,
						totalPages: pendingTotalPages,
						totalElements: pendingTotalElements,
						onPageChange: setPage,
						onPageSizeChange: (newSize) => {
							setPageSize(newSize);
							setPage(0);
						},
						isFetching: pendingIsFetching,
						label: "công ty",
					}}
					minWidth='min-w-[1040px]'
				/>
			</TabsContent>

			<TabsContent
				value='all'
				className='mt-0 flex flex-col gap-5'
			>
				<div className='rounded-lg border bg-card p-4'>
					<div className='grid gap-3 md:grid-cols-[minmax(260px,1fr)_180px]'>
						<Input
							value={allSearchTerm}
							onChange={(event) => {
								setAllSearchTerm(event.target.value);
								setAllPage(0);
							}}
							placeholder='Tìm theo tên, email, MST, địa chỉ...'
							startIcon={<Search className='size-4' />}
							className='h-10 bg-background'
						/>

						<select
							value={allStatusFilter}
							onChange={(event) => {
								setAllStatusFilter(event.target.value);
								setAllPage(0);
							}}
							className='h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50'
						>
							{STATUS_FILTER_OPTIONS.map((opt) => (
								<option
									key={opt.value}
									value={opt.value}
								>
									{opt.label}
								</option>
							))}
						</select>
					</div>
				</div>

				<DataTable
					columns={allColumns}
					data={allCompanies}
					isLoading={allIsLoading}
					isError={allIsError}
					error={allError}
					onRetry={() => allRefetch()}
					emptyState={{
						icon: Building2,
						title: "Không có công ty",
						subtitle: "Thay đổi bộ lọc hoặc tìm kiếm để xem kết quả khác.",
					}}
					pageResponse={allData}
					pageable={{
						page: allPage,
						pageSize: allPageSize,
						totalPages: allTotalPages,
						totalElements: allTotalElements,
						onPageChange: setAllPage,
						onPageSizeChange: (newSize) => {
							setAllPageSize(newSize);
							setAllPage(0);
						},
						isFetching: allIsFetching,
						label: "công ty",
					}}
					minWidth='min-w-[900px]'
				/>
			</TabsContent>

			{approvalAction && (
				<CompanyApprovalModal
					company={approvalAction.company}
					action={approvalAction.action}
					isOpen
					onClose={() => setApprovalAction(null)}
				/>
			)}
		</Tabs>
	);
}
