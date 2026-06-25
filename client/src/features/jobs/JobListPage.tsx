import { useDeferredValue, useState } from "react";
import { Link } from "react-router-dom";
import { Briefcase, ChevronLeft, ChevronRight, Filter, X } from "lucide-react";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Separator } from "@/components/ui/separator";
import { Skeleton } from "@/components/ui/skeleton";
import { useAllSkillsPublic } from "@/hooks/useSkills";
import { useCategories } from "@/hooks/useCategories";
import { usePublicJobSearch } from "@/hooks/usePublicJobs";
import {
	EMPLOYMENT_TYPE_LABELS,
	EXPERIENCE_LEVEL_LABELS,
	LOCATION_TYPES_LABELS,
} from "@/types/job";
import type { JobListResponse } from "@/types/job";
import type { JobSearchParams } from "@/api/jobs";
import RouterRoutes from "@/utils/RouterRoutes";

const EMPLOYMENT_TYPES = ["FULL_TIME", "PART_TIME", "CONTRACT", "INTERNSHIP"] as const;
const EXPERIENCE_LEVELS = ["INTERN", "JUNIOR", "MID", "SENIOR", "LEAD"] as const;
const LOCATION_TYPES = ["ONSITE", "REMOTE", "HYBRID"] as const;

const INITIAL_FILTERS: JobSearchParams = { page: 0, size: 12 };

function formatSalary(min: number | null, max: number | null, currency: string | null) {
	if (!min && !max) return "Thỏa thuận";
	const fmt = (n: number) => new Intl.NumberFormat("vi-VN").format(n);
	const cur = currency ?? "VND";
	if (min && max) return `${fmt(min)} – ${fmt(max)} ${cur}`;
	if (min) return `Từ ${fmt(min)} ${cur}`;
	return `Đến ${fmt(max!)} ${cur}`;
}

function JobCard({ job }: { job: JobListResponse }) {
	return (
		<Card className="hover:shadow-md transition-shadow">
			<CardContent className="p-5">
				<div className="flex items-start justify-between gap-3">
					<div className="flex-1 min-w-0">
						<Link
							to={RouterRoutes.JOB_DETAIL(job.slug)}
							className="text-base font-semibold hover:text-primary line-clamp-2"
						>
							{job.title}
						</Link>
						<p className="mt-1 text-sm text-muted-foreground flex items-center gap-1">
							<Briefcase className="h-3.5 w-3.5 shrink-0" />
							{job.companyName}
						</p>
					</div>
				</div>

				<div className="mt-3 flex flex-wrap gap-2">
					<Badge variant="secondary">{EMPLOYMENT_TYPE_LABELS[job.employmentType as keyof typeof EMPLOYMENT_TYPE_LABELS]}</Badge>
					<Badge variant="secondary">{LOCATION_TYPES_LABELS[job.locationTypes as keyof typeof LOCATION_TYPES_LABELS]}</Badge>
					<Badge variant="outline">{EXPERIENCE_LEVEL_LABELS[job.experienceLevel as keyof typeof EXPERIENCE_LEVEL_LABELS]}</Badge>
				</div>

				<div className="mt-3 flex items-center justify-between text-sm">
					<span className="font-medium text-green-700">
						{formatSalary(job.salaryMin, job.salaryMax, job.currency)}
					</span>
					{job.numberOfOpenings && (
						<span className="text-muted-foreground">{job.numberOfOpenings} vị trí</span>
					)}
				</div>
			</CardContent>
		</Card>
	);
}

function toggleId(arr: number[] | undefined, id: number): number[] {
	if (!arr) return [id];
	return arr.includes(id) ? arr.filter((x) => x !== id) : [...arr, id];
}

function toggleString(arr: string[] | undefined, s: string): string[] {
	if (!arr) return [s];
	return arr.includes(s) ? arr.filter((x) => x !== s) : [...arr, s];
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

	const set = (patch: Partial<JobSearchParams>) => {
		onChange({ ...filters, ...patch, page: 0 });
	};

	if (!open) return null;

	return (
		<aside className="w-full lg:w-64 shrink-0 space-y-5 bg-background border rounded-lg p-4">
			<div className="flex items-center justify-between">
				<span className="font-semibold text-sm flex items-center gap-1.5">
					<Filter className="h-4 w-4" />
					Bộ lọc
				</span>
				<Button variant="ghost" size="icon" className="h-7 w-7 lg:hidden" onClick={onToggle}>
					<X className="h-4 w-4" />
				</Button>
			</div>

			{/* Keyword */}
			<div>
				<Label className="text-xs text-muted-foreground">Từ khóa</Label>
				<Input
					placeholder="Vị trí, kỹ năng..."
					value={filters.keyword ?? ""}
					onChange={(e) => set({ keyword: e.target.value || undefined })}
				/>
			</div>

			<Separator />

			{/* Categories */}
			<div>
				<p className="text-sm font-medium mb-1.5">Ngành nghề</p>
				<div className="space-y-1 max-h-44 overflow-y-auto">
					{categories?.map((cat) => (
						<label key={cat.id} className="flex items-center gap-2 cursor-pointer text-sm py-0.5">
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.categoryIds?.includes(cat.id) ?? false}
								onChange={() => set({ categoryIds: toggleId(filters.categoryIds, cat.id) })}
							/>
							{cat.name}
						</label>
					))}
				</div>
			</div>

			<Separator />

			{/* Location types */}
			<div>
				<p className="text-sm font-medium mb-1.5">Địa điểm</p>
				<div className="space-y-1">
					{LOCATION_TYPES.map((lt) => (
						<label key={lt} className="flex items-center gap-2 cursor-pointer text-sm py-0.5">
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.locationTypes?.includes(lt) ?? false}
								onChange={() => set({ locationTypes: toggleString(filters.locationTypes, lt) })}
							/>
							{LOCATION_TYPES_LABELS[lt]}
						</label>
					))}
				</div>
			</div>

			<Separator />

			{/* Employment types */}
			<div>
				<p className="text-sm font-medium mb-1.5">Hình thức</p>
				<div className="space-y-1">
					{EMPLOYMENT_TYPES.map((et) => (
						<label key={et} className="flex items-center gap-2 cursor-pointer text-sm py-0.5">
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.employmentTypes?.includes(et) ?? false}
								onChange={() => set({ employmentTypes: toggleString(filters.employmentTypes, et) })}
							/>
							{EMPLOYMENT_TYPE_LABELS[et]}
						</label>
					))}
				</div>
			</div>

			<Separator />

			{/* Experience levels */}
			<div>
				<p className="text-sm font-medium mb-1.5">Cấp độ</p>
				<div className="space-y-1">
					{EXPERIENCE_LEVELS.map((el) => (
						<label key={el} className="flex items-center gap-2 cursor-pointer text-sm py-0.5">
							<input
								type="checkbox"
								className="size-4 accent-primary"
								checked={filters.experienceLevels?.includes(el) ?? false}
								onChange={() => set({ experienceLevels: toggleString(filters.experienceLevels, el) })}
							/>
							{EXPERIENCE_LEVEL_LABELS[el]}
						</label>
					))}
				</div>
			</div>

			<Separator />

			{/* Salary range */}
			<div>
				<p className="text-sm font-medium mb-1.5">Mức lương</p>
				<div className="flex gap-2">
					<Input
						placeholder="Tối thiểu"
						type="number"
						min={0}
						value={filters.minSalary ?? ""}
						onChange={(e) => set({ minSalary: e.target.value ? Number(e.target.value) : undefined })}
					/>
					<Input
						placeholder="Tối đa"
						type="number"
						min={0}
						value={filters.maxSalary ?? ""}
						onChange={(e) => set({ maxSalary: e.target.value ? Number(e.target.value) : undefined })}
					/>
				</div>
			</div>

			<Separator />

			{/* Skills */}
			<div>
				<p className="text-sm font-medium mb-1.5">Kỹ năng</p>
				<div className="space-y-1 max-h-44 overflow-y-auto">
					{skills?.map((skill) => (
						<label key={skill.id} className="flex items-center gap-2 cursor-pointer text-sm py-0.5">
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
	const [filters, setFilters] = useState<JobSearchParams>(INITIAL_FILTERS);
	const deferredFilters = useDeferredValue(filters);
	const [sidebarOpen, setSidebarOpen] = useState(false);

	const { data, isLoading, isError } = usePublicJobSearch(deferredFilters);

	const jobs = data?.content ?? [];
	const totalPages = data?.totalPages ?? 0;
	const totalElements = data?.totalElements ?? 0;

	const setPage = (page: number) => {
		setFilters((prev) => ({ ...prev, page }));
	};

	return (
		<div className="container mx-auto px-4 py-8">
			<div className="mb-6 flex items-center justify-between">
				<div>
					<h1 className="text-2xl font-bold">Việc làm mới nhất</h1>
					{!isLoading && (
						<p className="mt-1 text-sm text-muted-foreground">
							{totalElements} tin tuyển dụng đang mở
						</p>
					)}
				</div>
				<Button
					variant="outline"
					size="sm"
					className="lg:hidden"
					onClick={() => setSidebarOpen((o) => !o)}
				>
					<Filter className="h-4 w-4 mr-1" />
					Bộ lọc
				</Button>
			</div>

			<div className="flex flex-col lg:flex-row gap-6">
				<FilterSidebar
					filters={deferredFilters}
					onChange={setFilters}
					open={sidebarOpen}
					onToggle={() => setSidebarOpen((o) => !o)}
				/>

				<main className="flex-1 min-w-0">
					{isLoading && (
						<div className="space-y-4">
							{Array.from({ length: 6 }).map((_, i) => (
								<Skeleton key={i} className="h-36 w-full rounded-lg" />
							))}
						</div>
					)}

					{isError && (
						<div className="text-center py-16 text-muted-foreground">
							Không thể tải danh sách việc làm. Vui lòng thử lại.
						</div>
					)}

					{!isLoading && !isError && jobs.length === 0 && (
						<div className="text-center py-16 text-muted-foreground">
							Hiện chưa có tin tuyển dụng nào phù hợp.
						</div>
					)}

					{!isLoading && jobs.length > 0 && (
						<>
							<div className="space-y-4">
								{jobs.map((job) => (
									<JobCard key={job.id} job={job} />
								))}
							</div>

							{totalPages > 1 && (
								<div className="mt-8 flex items-center justify-center gap-3">
									<Button
										variant="outline"
										size="sm"
										disabled={(deferredFilters.page ?? 0) === 0}
										onClick={() => setPage((deferredFilters.page ?? 0) - 1)}
									>
										<ChevronLeft className="h-4 w-4" />
										Trước
									</Button>
									<span className="text-sm text-muted-foreground">
										Trang {(deferredFilters.page ?? 0) + 1} / {totalPages}
									</span>
									<Button
										variant="outline"
										size="sm"
										disabled={(deferredFilters.page ?? 0) >= totalPages - 1}
										onClick={() => setPage((deferredFilters.page ?? 0) + 1)}
									>
										Tiếp
										<ChevronRight className="h-4 w-4" />
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
