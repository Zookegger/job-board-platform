import type { JobSearchParams } from "@/api/jobs";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { Slider } from "@/components/ui/slider";
import { useCategories } from "@/hooks/useCategories";
import { useDebounce } from "@/hooks/useDebounce";
import { usePublicJobSearch } from "@/hooks/usePublicJobs";
import { useAllSkillsPublic } from "@/hooks/useSkills";
import {
	EMPLOYMENT_TYPE_LABELS,
	EmploymentTypes,
	EXPERIENCE_LEVEL_LABELS,
	ExperienceLevels,
	LOCATION_TYPES_LABELS,
	LocationTypes,
} from "@/types/job";
import { Filter, Search, SlidersHorizontal, X } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { JobCardPublic } from "./JobCardPublic";

const INITIAL_FILTERS: JobSearchParams = { page: 0, size: 12 };

const SALARY_MAX = 100_000_000;
const SALARY_STEP = 5_000_000;

function toggleId(arr: number[] | undefined, id: number): number[] {
	if (!arr) return [id];
	return arr.includes(id) ? arr.filter((x) => x !== id) : [...arr, id];
}

function toggleString(arr: string[] | undefined, s: string): string[] {
	if (!arr) return [s];
	return arr.includes(s) ? arr.filter((x) => x !== s) : [...arr, s];
}

function formatVND(value: number) {
	return new Intl.NumberFormat("vi-VN", {
		style: "currency",
		currency: "VND",
		maximumFractionDigits: 0,
	}).format(value);
}

function filtersToParams(f: JobSearchParams): URLSearchParams {
	const p = new URLSearchParams();
	if (f.keyword) p.set("keyword", f.keyword);
	if (f.categoryIds?.length) p.set("categoryIds", f.categoryIds.join(","));
	if (f.locationTypes?.length) p.set("locationTypes", f.locationTypes.join(","));
	if (f.employmentTypes?.length) p.set("employmentTypes", f.employmentTypes.join(","));
	if (f.experienceLevels?.length) p.set("experienceLevels", f.experienceLevels.join(","));
	if (f.minSalary != null) p.set("minSalary", String(f.minSalary));
	if (f.maxSalary != null) p.set("maxSalary", String(f.maxSalary));
	if (f.skillIds?.length) p.set("skillIds", f.skillIds.join(","));
	if (f.page && f.page > 0) p.set("page", String(f.page));
	return p;
}

function paramsToFilters(params: URLSearchParams): JobSearchParams {
	const keyword = params.get("keyword") || undefined;
	const categoryIds = params.get("categoryIds");
	const locationTypes = params.get("locationTypes");
	const employmentTypes = params.get("employmentTypes");
	const experienceLevels = params.get("experienceLevels");
	const minSalary = params.get("minSalary");
	const maxSalary = params.get("maxSalary");
	const skillIds = params.get("skillIds");
	const page = params.get("page");
	return {
		keyword,
		categoryIds: categoryIds ? categoryIds.split(",").map(Number) : undefined,
		locationTypes: locationTypes ? locationTypes.split(",") : undefined,
		employmentTypes: employmentTypes ? employmentTypes.split(",") : undefined,
		experienceLevels: experienceLevels ? experienceLevels.split(",") : undefined,
		minSalary: minSalary ? Number(minSalary) : undefined,
		maxSalary: maxSalary ? Number(maxSalary) : undefined,
		skillIds: skillIds ? skillIds.split(",").map(Number) : undefined,
		page: page ? Number(page) : 0,
		size: 12,
	};
}

function countActiveFilters(f: JobSearchParams): number {
	let count = 0;
	if (f.keyword) count++;
	if (f.categoryIds?.length) count++;
	if (f.locationTypes?.length) count++;
	if (f.employmentTypes?.length) count++;
	if (f.experienceLevels?.length) count++;
	if (f.minSalary != null || f.maxSalary != null) count++;
	if (f.skillIds?.length) count++;
	return count;
}

interface FilterSidebarProps {
	filters: JobSearchParams;
	onChange: (f: JobSearchParams) => void;
	open: boolean;
	onToggle: () => void;
}

function FilterSidebar({ filters, onChange, open, onToggle }: FilterSidebarProps) {
	const { data: categories } = useCategories();
	const { data: skills } = useAllSkillsPublic();
	const [categorySearch, setCategorySearch] = useState("");

	const set = (patch: Partial<JobSearchParams>) => {
		onChange({ ...filters, ...patch, page: 0 });
	};

	const filteredCategories = useMemo(() => {
		if (!categorySearch || !categories) return categories;
		const q = categorySearch.toLowerCase();
		return categories.filter((c) => c.name.toLowerCase().includes(q));
	}, [categories, categorySearch]);

	const sliderValue: [number, number] = [
		filters.minSalary ?? 0,
		filters.maxSalary ?? SALARY_MAX,
	];

	if (!open) return null;

	return (
		<aside className="w-full lg:w-64 shrink-0 space-y-5 bg-background border rounded-lg p-4 h-fit lg:sticky lg:top-24">
			<div className="flex items-center justify-between">
				<span className="font-semibold text-sm flex items-center gap-1.5">
					<SlidersHorizontal className="h-4 w-4" />
					Bộ lọc
					{countActiveFilters(filters) > 0 && (
						<Badge variant="secondary" className="ml-1 text-xs px-1.5 py-0">
							{countActiveFilters(filters)}
						</Badge>
					)}
				</span>
				<Button
					variant="ghost"
					size="icon"
					className="h-7 w-7 lg:hidden"
					onClick={onToggle}
				>
					<X className="h-4 w-4" />
				</Button>
			</div>

			<div>
				<Label className="text-xs text-muted-foreground">Từ khóa</Label>
				<Input
					placeholder="Vị trí, kỹ năng..."
					value={filters.keyword ?? ""}
					onChange={(e) => set({ keyword: e.target.value || undefined })}
				/>
			</div>

			<Separator />

			<div>
				<p className="text-sm font-medium mb-1.5">Ngành nghề</p>
				<Input
					placeholder="Tìm ngành nghề..."
					className="mb-2 h-8 text-sm"
					value={categorySearch}
					onChange={(e) => setCategorySearch(e.target.value)}
				/>
				<div className="space-y-1 max-h-44 overflow-y-auto">
					{filteredCategories?.map((cat) => (
						<label
							key={cat.id}
							className="flex items-center gap-2 cursor-pointer text-sm py-0.5"
						>
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.categoryIds?.includes(cat.id) ?? false}
								onChange={() => set({ categoryIds: toggleId(filters.categoryIds, cat.id) })}
							/>
							{cat.name}
						</label>
					))}
					{filteredCategories?.length === 0 && (
						<p className="text-xs text-muted-foreground py-1">Không tìm thấy ngành nghề</p>
					)}
				</div>
			</div>

			<Separator />

			<div>
				<p className="text-sm font-medium mb-1.5">Địa điểm</p>
				<div className="space-y-1">
					{Object.entries(LocationTypes).map(([key, value]) => (
						<label
							key={key}
							className="flex items-center gap-2 cursor-pointer text-sm py-0.5"
						>
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.locationTypes?.includes(value) ?? false}
								onChange={() => set({ locationTypes: toggleString(filters.locationTypes, value) })}
							/>
							{LOCATION_TYPES_LABELS[value]}
						</label>
					))}
				</div>
			</div>

			<Separator />

			<div>
				<p className="text-sm font-medium mb-1.5">Hình thức</p>
				<div className="space-y-1">
					{Object.entries(EmploymentTypes).map(([key, value]) => (
						<label
							key={key}
							className="flex items-center gap-2 cursor-pointer text-sm py-0.5"
						>
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.employmentTypes?.includes(value) ?? false}
								onChange={() => set({ employmentTypes: toggleString(filters.employmentTypes, value) })}
							/>
							{EMPLOYMENT_TYPE_LABELS[value]}
						</label>
					))}
				</div>
			</div>

			<Separator />

			<div>
				<p className="text-sm font-medium mb-1.5">Cấp độ</p>
				<div className="space-y-1">
					{Object.entries(ExperienceLevels).map(([key, value]) => (
						<label
							key={key}
							className="flex items-center gap-2 cursor-pointer text-sm py-0.5"
						>
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.experienceLevels?.includes(value) ?? false}
								onChange={() => set({ experienceLevels: toggleString(filters.experienceLevels, value) })}
							/>
							{EXPERIENCE_LEVEL_LABELS[value]}
						</label>
					))}
				</div>
			</div>

			<Separator />

			<div>
				<p className="text-sm font-medium mb-1.5">Mức lương</p>
				<div className="px-1">
					<Slider
						value={sliderValue}
						min={0}
						max={SALARY_MAX}
						step={SALARY_STEP}
						onValueChange={([min, max]) =>
							set({
								minSalary: min > 0 ? min : undefined,
								maxSalary: max < SALARY_MAX ? max : undefined,
							})
						}
					/>
				</div>
				<div className="flex justify-between text-xs text-muted-foreground mt-2">
					<span>{filters.minSalary ? formatVND(filters.minSalary) : "Tối thiểu"}</span>
					<span>{filters.maxSalary ? formatVND(filters.maxSalary) : "Tối đa"}</span>
				</div>
			</div>

			<Separator />

			<div>
				<p className="text-sm font-medium mb-1.5">Kỹ năng</p>
				<div className="space-y-1 max-h-44 overflow-y-auto">
					{skills?.map((skill) => (
						<label
							key={skill.id}
							className="flex items-center gap-2 cursor-pointer text-sm py-0.5"
						>
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.skillIds?.includes(skill.id) ?? false}
								onChange={() => set({ skillIds: toggleId(filters.skillIds, skill.id) })}
							/>
							{skill.name}
						</label>
					))}
				</div>
			</div>

			<Button
				variant="outline"
				size="sm"
				className="w-full"
				onClick={() => onChange(INITIAL_FILTERS)}
			>
				Xóa bộ lọc
			</Button>
		</aside>
	);
}

export function JobListPage() {
	const [searchParams, setSearchParams] = useSearchParams();
	const [sidebarOpen, setSidebarOpen] = useState(true);

	const [filters, setFiltersState] = useState<JobSearchParams>(() =>
		paramsToFilters(searchParams)
	);

	useEffect(() => {
		if (sidebarOpen) return;
		const mq = window.matchMedia("(min-width: 1024px)");
		const handler = (e: MediaQueryListEvent | MediaQueryList) => {
			if (e.matches) setSidebarOpen(true);
		};
		handler(mq);
		mq.addEventListener("change", handler);
		return () => mq.removeEventListener("change", handler);
	}, [sidebarOpen]);

	const setFilters = (
		update: JobSearchParams | ((prev: JobSearchParams) => JobSearchParams)
	) => {
		setFiltersState(update);
		const resolved = typeof update === "function" ? update(filters) : update;
		const params = filtersToParams(resolved);
		setSearchParams(params.toString() ? `?${params.toString()}` : "", {
			replace: true,
		});
	};

	const debouncedFilters = useDebounce(
		{
			keyword: filters.keyword,
			categoryIds: filters.categoryIds,
			locationTypes: filters.locationTypes,
			employmentTypes: filters.employmentTypes,
			experienceLevels: filters.experienceLevels,
			minSalary: filters.minSalary,
			maxSalary: filters.maxSalary,
			skillIds: filters.skillIds,
		},
		300
	);

	const queryParams: JobSearchParams = {
		...debouncedFilters,
		page: filters.page,
		size: filters.size,
	};

	const { data, isLoading, isError, refetch } = usePublicJobSearch(queryParams);

	const jobs = data?.content ?? [];
	const totalPages = data?.totalPages ?? 0;
	const totalElements = data?.totalElements ?? 0;

	const setPage = (page: number) => {
		setFilters((prev) => ({ ...prev, page }));
		window.scrollTo({ top: 0, behavior: "smooth" });
	};

	const resetFilters = () => setFilters(INITIAL_FILTERS);

	const hasActiveFilters = countActiveFilters(filters) > 0;

	return (
		<div className='container mx-auto px-4 py-8'>
			<div className='mb-6 flex items-center justify-between'>
				<div>
					<h1 className='text-2xl font-bold'>Việc làm mới nhất</h1>
					{!isLoading && (
						<p className='mt-1 text-sm text-muted-foreground'>{totalElements} tin tuyển dụng đang mở</p>
					)}
				</div>
				<Button
					variant='outline'
					size='sm'
					className='lg:hidden'
					onClick={() => setSidebarOpen((o) => !o)}
				>
					<Filter className='h-4 w-4 mr-1' />
					Bộ lọc
				</Button>
			</div>

			<div className='flex flex-col lg:flex-row gap-6'>
				<FilterSidebar
					filters={filters}
					onChange={setFilters}
					open={sidebarOpen}
					onToggle={() => setSidebarOpen((o) => !o)}
				/>

				<main className='flex-1 min-w-0'>
					{isLoading && (
						<div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
							{Array.from({ length: 6 }).map((_, i) => (
								<Skeleton
									key={i}
									className='h-52 w-full rounded-lg'
								/>
							))}
						</div>
					)}

					{isError && (
						<div className='flex flex-col items-center justify-center py-20 text-muted-foreground gap-3'>
							<Search className='h-10 w-10' />
							<p className='text-lg'>Không thể tải danh sách việc làm</p>
							<p className='text-sm'>Vui lòng thử lại sau.</p>
							<Button
								variant='outline'
								size='sm'
								onClick={() => refetch()}
							>
								Thử lại
							</Button>
						</div>
					)}

					{!isLoading && !isError && jobs.length === 0 && (
						<div className='flex flex-col items-center justify-center py-20 text-muted-foreground gap-3'>
							<Search className='h-10 w-10' />
							<p className='text-lg'>Không tìm thấy việc làm phù hợp</p>
							<p className='text-sm'>
								{hasActiveFilters
									? "Thử thay đổi bộ lọc hoặc từ khóa tìm kiếm."
									: "Hiện chưa có tin tuyển dụng nào."}
							</p>
							{hasActiveFilters && (
								<Button
									variant='outline'
									size='sm'
									onClick={resetFilters}
								>
									<X className='h-4 w-4 mr-1' />
									Xóa bộ lọc
								</Button>
							)}
						</div>
					)}

					{!isLoading && jobs.length > 0 && (
						<>
							{hasActiveFilters && (
								<div className='flex items-center gap-2 mb-4 text-sm text-muted-foreground'>
									<span className='hidden sm:inline'>
										Đang lọc theo {countActiveFilters(filters)} tiêu chí
									</span>
									<Button
										variant='ghost'
										size='sm'
										className='h-7 text-xs'
										onClick={resetFilters}
									>
										<X className='h-3 w-3 mr-1' />
										Xóa bộ lọc
									</Button>
								</div>
							)}

							<div className='grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'>
								{jobs.map((job) => (
									<JobCardPublic
										key={job.id}
										job={job}
									/>
								))}
							</div>

							{totalPages > 1 && (
								<div className='mt-8 flex items-center justify-center gap-3'>
									<Button
										variant='outline'
										size='sm'
										disabled={(filters.page ?? 0) === 0}
										onClick={() => setPage((filters.page ?? 0) - 1)}
									>
										Trang trước
									</Button>
									<span className='text-sm text-muted-foreground'>
										Trang {(filters.page ?? 0) + 1} / {totalPages}
									</span>
									<Button
										variant='outline'
										size='sm'
										disabled={(filters.page ?? 0) >= totalPages - 1}
										onClick={() => setPage((filters.page ?? 0) + 1)}
									>
										Trang sau
									</Button>
								</div>
							)}
						</>
					)}
				</main>
			</div>
		</div>
	);
}
