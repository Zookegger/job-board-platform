import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Skeleton } from "@/components/ui/skeleton";
import {
	useApproveCompany,
	usePendingCompanies,
	useRejectCompany,
} from "@/hooks/useAdminCompanies";
import { CompanyStatus, type AdminPendingCompanyResponse } from "@/types/company";
import getErrorMessage from "@/utils/getErrorMessage";
import {
	Building2,
	CheckCircle2,
	ChevronLeft,
	ChevronRight,
	ExternalLink,
	Mail,
	MapPin,
	Phone,
	RefreshCw,
	Search,
	XCircle,
} from "lucide-react";
import { useDeferredValue, useEffect, useMemo, useState } from "react";
import { toast } from "sonner";

const PAGE_SIZE = 10;

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
			variant="outline"
			className="border-warning/40 bg-warning/20 text-warning-foreground"
		>
			Đang chờ duyệt
		</Badge>
	);
}

function CompanyLogo({ company }: { company: AdminPendingCompanyResponse }) {
	return (
		<div className="flex size-11 shrink-0 items-center justify-center overflow-hidden rounded-lg border bg-muted">
			{company.logoUrl ? (
				<img
					src={company.logoUrl}
					alt={company.companyName}
					className="h-full w-full object-cover"
				/>
			) : (
				<Building2 className="size-5 text-muted-foreground" />
			)}
		</div>
	);
}

function TableSkeleton() {
	return (
		<>
			{Array.from({ length: 5 }).map((_, index) => (
				<tr key={index} className="border-b">
					<td className="px-4 py-4">
						<div className="flex items-center gap-3">
							<Skeleton className="size-11 rounded-lg" />
							<div className="space-y-2">
								<Skeleton className="h-4 w-44" />
								<Skeleton className="h-3 w-28" />
							</div>
						</div>
					</td>
					<td className="px-4 py-4">
						<Skeleton className="h-4 w-36" />
					</td>
					<td className="px-4 py-4">
						<Skeleton className="h-4 w-40" />
					</td>
					<td className="px-4 py-4">
						<Skeleton className="h-4 w-32" />
					</td>
					<td className="px-4 py-4">
						<Skeleton className="h-4 w-28" />
					</td>
					<td className="px-4 py-4">
						<Skeleton className="h-8 w-32" />
					</td>
				</tr>
			))}
		</>
	);
}

export default function AdminCompaniesPage() {
	const [page, setPage] = useState(0);
	const [searchTerm, setSearchTerm] = useState("");
	const [taxCodeFilter, setTaxCodeFilter] = useState<TaxCodeFilter>("all");
	const [contactFilter, setContactFilter] = useState<ContactFilter>("all");
	const [sortOption, setSortOption] = useState<SortOption>("newest");
	const deferredSearch = useDeferredValue(searchTerm.trim());

	useEffect(() => {
		setPage(0);
	}, [deferredSearch, taxCodeFilter, contactFilter, sortOption]);

	const queryParams = useMemo(() => {
		const sort = sortConfig[sortOption];
		return {
			page,
			size: PAGE_SIZE,
			keyword: deferredSearch,
			hasTaxCode: toBooleanFilter(taxCodeFilter, "with-tax-code", "missing-tax-code"),
			hasContact: toBooleanFilter(contactFilter, "with-contact", "missing-contact"),
			sortBy: sort.sortBy,
			direction: sort.direction,
		};
	}, [contactFilter, deferredSearch, page, sortOption, taxCodeFilter]);

	const { data, isError, isFetching, isLoading, refetch, error } = usePendingCompanies(queryParams);
	const approveCompany = useApproveCompany();
	const rejectCompany = useRejectCompany();

	const companies = data?.content ?? [];
	const totalElements = data?.totalElements ?? 0;
	const totalPages = data?.totalPages ?? 0;
	const currentPageLabel = totalPages > 0 ? page + 1 : 0;
	const actionPending = approveCompany.isPending || rejectCompany.isPending;

	const handleApprove = (company: AdminPendingCompanyResponse) => {
		if (!window.confirm(`Duyệt công ty "${company.companyName}"?`)) return;

		approveCompany.mutate(company.id, {
			onSuccess: () => toast.success("Đã duyệt công ty"),
			onError: (mutationError) => toast.error(getErrorMessage(mutationError, "Không thể duyệt công ty")),
		});
	};

	const handleReject = (company: AdminPendingCompanyResponse) => {
		const reason = window.prompt(`Lý do từ chối "${company.companyName}"`);
		if (!reason?.trim()) return;

		rejectCompany.mutate(
			{ companyId: company.id, reason: reason.trim() },
			{
				onSuccess: () => toast.success("Đã từ chối công ty"),
				onError: (mutationError) => toast.error(getErrorMessage(mutationError, "Không thể từ chối công ty")),
			},
		);
	};

	return (
		<div className="mx-auto flex w-full max-w-7xl flex-col gap-5">
			<div className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
				<div>
					<h1 className="text-2xl font-semibold text-foreground">Công ty chờ phê duyệt</h1>
					<p className="mt-1 text-sm text-muted-foreground">
						{totalElements.toLocaleString("vi-VN")} hồ sơ đang cần ADMIN xem xét
					</p>
				</div>
				<Button
					variant="outline"
					onClick={() => refetch()}
					disabled={isFetching}
					className="w-fit"
				>
					<RefreshCw className={isFetching ? "animate-spin" : ""} />
					Làm mới
				</Button>
			</div>

			<div className="rounded-lg border bg-card p-4">
				<div className="grid gap-3 md:grid-cols-[minmax(260px,1fr)_180px_180px_180px]">
					<Input
						value={searchTerm}
						onChange={(event) => setSearchTerm(event.target.value)}
						placeholder="Tìm theo tên, email, MST, địa chỉ..."
						startIcon={<Search className="size-4" />}
						className="h-10 bg-background"
					/>

					<select
						value={taxCodeFilter}
						onChange={(event) => setTaxCodeFilter(event.target.value as TaxCodeFilter)}
						className="h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50"
					>
						<option value="all">Tất cả MST</option>
						<option value="with-tax-code">Có MST</option>
						<option value="missing-tax-code">Thiếu MST</option>
					</select>

					<select
						value={contactFilter}
						onChange={(event) => setContactFilter(event.target.value as ContactFilter)}
						className="h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50"
					>
						<option value="all">Tất cả liên hệ</option>
						<option value="with-contact">Có liên hệ</option>
						<option value="missing-contact">Thiếu liên hệ</option>
					</select>

					<select
						value={sortOption}
						onChange={(event) => setSortOption(event.target.value as SortOption)}
						className="h-10 rounded-md border border-input bg-background px-3 text-sm outline-none transition focus:border-ring focus:ring-3 focus:ring-ring/50"
					>
						<option value="newest">Mới nhất</option>
						<option value="oldest">Cũ nhất</option>
						<option value="name">Tên A-Z</option>
					</select>
				</div>
			</div>

			<div className="overflow-hidden rounded-lg border bg-card">
				<div className="overflow-x-auto">
					<table className="w-full min-w-[1040px] border-collapse text-left text-sm">
						<thead className="border-b bg-muted/60 text-xs uppercase text-muted-foreground">
							<tr>
								<th className="px-4 py-3 font-medium">Công ty</th>
								<th className="px-4 py-3 font-medium">Người phụ trách</th>
								<th className="px-4 py-3 font-medium">Liên hệ</th>
								<th className="px-4 py-3 font-medium">Thông tin pháp lý</th>
								<th className="px-4 py-3 font-medium">Ngày đăng ký</th>
								<th className="px-4 py-3 font-medium">Xử lý</th>
							</tr>
						</thead>
						<tbody>
							{isLoading ? (
								<TableSkeleton />
							) : isError ? (
								<tr>
									<td colSpan={6} className="px-4 py-12 text-center">
										<p className="font-medium text-destructive">
											{getErrorMessage(error, "Không thể tải danh sách công ty")}
										</p>
										<Button
											variant="outline"
											className="mt-3"
											onClick={() => refetch()}
										>
											<RefreshCw />
											Thử lại
										</Button>
									</td>
								</tr>
							) : companies.length === 0 ? (
								<tr>
									<td colSpan={6} className="px-4 py-14 text-center">
										<div className="mx-auto flex size-12 items-center justify-center rounded-lg bg-muted">
											<Building2 className="size-6 text-muted-foreground" />
										</div>
										<p className="mt-3 font-medium">Không có công ty chờ duyệt</p>
										<p className="mt-1 text-sm text-muted-foreground">
											Thay đổi bộ lọc hoặc tìm kiếm để xem kết quả khác.
										</p>
									</td>
								</tr>
							) : (
								companies.map((company) => (
									<tr key={company.id} className="border-b last:border-0 hover:bg-muted/30">
										<td className="px-4 py-4 align-top">
											<div className="flex gap-3">
												<CompanyLogo company={company} />
												<div className="min-w-0">
													<div className="flex flex-wrap items-center gap-2">
														<p className="font-medium text-foreground">{company.companyName}</p>
														{company.status === CompanyStatus.PENDING && <PendingBadge />}
													</div>
													<div className="mt-1 flex items-center gap-1 text-xs text-muted-foreground">
														<MapPin className="size-3.5" />
														<span className="line-clamp-1">{fallbackText(company.address)}</span>
													</div>
													{company.website && (
														<a
															href={company.website}
															target="_blank"
															rel="noreferrer"
															className="mt-2 inline-flex items-center gap-1 text-xs font-medium text-primary hover:underline"
														>
															Website
															<ExternalLink className="size-3" />
														</a>
													)}
												</div>
											</div>
										</td>

										<td className="px-4 py-4 align-top">
											<p className="font-medium">{fallbackText(company.employerName)}</p>
											<p className="mt-1 text-xs text-muted-foreground">
												{fallbackText(company.roleInCompany, "Vai trò chưa cập nhật")}
											</p>
											<p className="mt-2 break-all text-xs text-muted-foreground">
												{fallbackText(company.employerEmail)}
											</p>
										</td>

										<td className="px-4 py-4 align-top">
											<div className="space-y-2">
												<div className="flex items-center gap-2 text-sm">
													<Mail className="size-4 text-muted-foreground" />
													<span className="break-all">{fallbackText(company.email)}</span>
												</div>
												<div className="flex items-center gap-2 text-sm">
													<Phone className="size-4 text-muted-foreground" />
													<span>{fallbackText(company.phone || company.employerPhone)}</span>
												</div>
											</div>
										</td>

										<td className="px-4 py-4 align-top">
											<p className="font-medium">{fallbackText(company.taxCode, "Thiếu MST")}</p>
											<p className="mt-1 line-clamp-2 max-w-[220px] text-xs text-muted-foreground">
												{fallbackText(company.description, "Chưa có mô tả")}
											</p>
										</td>

										<td className="px-4 py-4 align-top text-sm text-muted-foreground">
											{formatDate(company.createdAt)}
										</td>

										<td className="px-4 py-4 align-top">
											<div className="flex flex-wrap gap-2">
												<Button
													variant="success"
													size="sm"
													disabled={actionPending}
													onClick={() => handleApprove(company)}
												>
													<CheckCircle2 />
													Duyệt
												</Button>
												<Button
													variant="destructive"
													size="sm"
													disabled={actionPending}
													onClick={() => handleReject(company)}
												>
													<XCircle />
													Từ chối
												</Button>
											</div>
										</td>
									</tr>
								))
							)}
						</tbody>
					</table>
				</div>

				<div className="flex flex-col gap-3 border-t px-4 py-3 text-sm text-muted-foreground md:flex-row md:items-center md:justify-between">
					<span>
						Trang {currentPageLabel} / {totalPages} · {totalElements.toLocaleString("vi-VN")} công ty
					</span>
					<div className="flex items-center gap-2">
						<Button
							variant="outline"
							size="sm"
							disabled={page === 0 || isFetching}
							onClick={() => setPage((current) => Math.max(current - 1, 0))}
						>
							<ChevronLeft />
							Trước
						</Button>
						<Button
							variant="outline"
							size="sm"
							disabled={totalPages === 0 || page >= totalPages - 1 || isFetching}
							onClick={() => setPage((current) => current + 1)}
						>
							Sau
							<ChevronRight />
						</Button>
					</div>
				</div>
			</div>
		</div>
	);
}
