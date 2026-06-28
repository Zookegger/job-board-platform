import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Skeleton } from "@/components/ui/skeleton";
import { JobCardPublic } from "@/features/jobs/JobCardPublic";
import { useCategories } from "@/hooks/useCategories";
import { usePublicJobs } from "@/hooks/usePublicJobs";
import RouterRoutes from "@/utils/RouterRoutes";
import { ArrowRight, Briefcase, Building2, Search } from "lucide-react";
import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

function JobCardSkeleton() {
	return (
		<div className="border rounded-lg p-5 bg-card flex flex-col gap-3">
			<div className="flex items-start gap-3">
				<Skeleton className="size-12 rounded-lg shrink-0" />
				<div className="flex-1 space-y-2">
					<Skeleton className="h-4 w-3/4" />
					<Skeleton className="h-3 w-1/2" />
				</div>
			</div>
			<div className="flex gap-1.5">
				<Skeleton className="h-5 w-20 rounded-full" />
				<Skeleton className="h-5 w-24 rounded-full" />
			</div>
			<Skeleton className="h-4 w-28" />
			<Skeleton className="h-3 w-full" />
		</div>
	);
}

export default function HomePage() {
	const navigate = useNavigate();
	const [keyword, setKeyword] = useState("");

	const { data: jobsData, isLoading: jobsLoading } = usePublicJobs({ page: 0, size: 6, sort: "createdAt,desc" });
	const { data: categories } = useCategories();

	function handleSearch(e: React.FormEvent) {
		e.preventDefault();
		const trimmed = keyword.trim();
		if (trimmed) {
			navigate(`${RouterRoutes.JOBS}?keyword=${encodeURIComponent(trimmed)}`);
		} else {
			navigate(RouterRoutes.JOBS);
		}
	}

	return (
		<div>
			<section className="bg-gradient-to-br from-primary via-primary/90 to-primary/70 text-primary-foreground">
				<div className="mx-auto max-w-7xl px-4 py-20 md:py-28">
					<div className="mx-auto max-w-2xl text-center">
						<h1 className="text-3xl font-bold tracking-tight md:text-5xl">
							Tìm việc mơ ước của bạn
						</h1>
						<p className="mt-4 text-lg text-primary-foreground/80">
							Khám phá hàng ngàn cơ hội việc làm từ các công ty hàng đầu
						</p>

						<form onSubmit={handleSearch} className="mt-8 flex gap-2">
							<div className="relative flex-1">
								<Search className="absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground" />
								<input
									type="text"
									value={keyword}
									onChange={(e) => setKeyword(e.target.value)}
									placeholder="Nhập từ khóa, vị trí..."
									className="w-full rounded-lg bg-background py-2.5 pl-10 pr-4 text-sm text-foreground placeholder:text-muted-foreground focus:outline-none focus:ring-2 focus:ring-ring"
								/>
							</div>
							<Button type="submit" variant="secondary" size="lg" className="shrink-0">
								Tìm kiếm
							</Button>
						</form>
					</div>
				</div>
			</section>

			<section className="mx-auto max-w-7xl px-4 py-12 md:py-16">
				<div className="flex items-center justify-between mb-8">
					<div>
						<h2 className="text-2xl font-bold">Việc làm mới nhất</h2>
						<p className="mt-1 text-sm text-muted-foreground">
							Các cơ hội việc làm được cập nhật gần đây
						</p>
					</div>
					<Link
						to={RouterRoutes.JOBS}
						className="hidden sm:inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
					>
						Xem tất cả
						<ArrowRight className="size-4" />
					</Link>
				</div>

				{jobsLoading ? (
					<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
						{Array.from({ length: 6 }).map((_, i) => (
							<JobCardSkeleton key={i} />
						))}
					</div>
				) : !jobsData || jobsData.content.length === 0 ? null : (
					<div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
						{jobsData.content.map((job) => (
							<JobCardPublic key={job.id} job={job} />
						))}
					</div>
				)}

				<div className="mt-6 text-center sm:hidden">
					<Link
						to={RouterRoutes.JOBS}
						className="inline-flex items-center gap-1 text-sm font-medium text-primary hover:underline"
					>
						Xem tất cả việc làm
						<ArrowRight className="size-4" />
					</Link>
				</div>
			</section>

			{categories && categories.length > 0 && (
				<section className="bg-muted/50">
					<div className="mx-auto max-w-7xl px-4 py-12 md:py-16">
						<div className="flex items-center justify-between mb-6">
							<div>
								<h2 className="text-2xl font-bold">Ngành nghề phổ biến</h2>
								<p className="mt-1 text-sm text-muted-foreground">
									Duyệt việc làm theo ngành nghề
								</p>
							</div>
						</div>

						<div className="flex flex-wrap gap-3">
							{categories.map((cat) => (
								<Link key={cat.id} to={`${RouterRoutes.JOBS}?categoryIds=${cat.id}`} onClick={() => window.scrollTo({ top: 0, behavior: "instant"})}>
									<Badge
										variant="secondary"
										className="cursor-pointer px-4 py-4 text-sm hover:bg-primary hover:text-primary-foreground transition-colors"
									>
										{cat.name}
									</Badge>
								</Link>
							))}
						</div>
					</div>
				</section>
			)}

			<section className="mx-auto max-w-7xl px-4 py-12 md:py-16">
				<div className="rounded-xl bg-gradient-to-br from-primary/10 via-primary/5 to-background border p-8 md:p-12 flex flex-col md:flex-row items-center justify-between gap-6">
					<div className="flex items-start gap-4">
						<div className="hidden sm:flex size-12 shrink-0 items-center justify-center rounded-lg bg-primary/20">
							<Building2 className="size-6 text-primary" />
						</div>
						<div>
							<h3 className="text-xl font-bold">Bạn là nhà tuyển dụng?</h3>
							<p className="mt-1 text-sm text-muted-foreground">
								Đăng tin tuyển dụng và tiếp cận hàng ngàn ứng viên tiềm năng ngay hôm nay
							</p>
						</div>
					</div>
					<Link to={RouterRoutes.REGISTER}>
						<Button variant="primary" size="lg" className="shrink-0">
							<Briefcase className="size-4" />
							Đăng ký nhà tuyển dụng
						</Button>
					</Link>
				</div>
			</section>
		</div>
	);
}
