import { BriefcaseBusiness as Briefcase, MapPin } from "lucide-react";
import { Link } from "react-router-dom";

import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardFooter } from "@/components/ui/card";
import { Skeleton } from "@/components/ui/skeleton";
import { cn } from "@/lib/utils";

export interface CompanyCardData {
	name: string;
	slug: string;
	logoUrl?: string;
	description?: string;
	address?: string;
	website?: string;
	totalOpenJobs?: number;
	categories?: Array<{ id: number; name: string }>;
}

interface CompanyCardProps {
	company: CompanyCardData;
}

const COLORS = [
	"from-blue-50 to-blue-100 dark:from-blue-900/20 dark:to-blue-800/20",
	"from-indigo-50 to-indigo-100 dark:from-indigo-900/20 dark:to-indigo-800/20",
	"from-emerald-50 to-emerald-100 dark:from-emerald-900/20 dark:to-emerald-800/20",
	"from-amber-50 to-amber-100 dark:from-amber-900/20 dark:to-amber-800/20",
	"from-rose-50 to-rose-100 dark:from-rose-900/20 dark:to-rose-800/20",
	"from-cyan-50 to-cyan-100 dark:from-cyan-900/20 dark:to-cyan-800/20",
	"from-violet-50 to-violet-100 dark:from-violet-900/20 dark:to-violet-800/20",
	"from-teal-50 to-teal-100 dark:from-teal-900/20 dark:to-teal-800/20",
];

const LETTER_COLORS = [
	"text-blue-500 dark:text-blue-400",
	"text-indigo-500 dark:text-indigo-400",
	"text-emerald-500 dark:text-emerald-400",
	"text-amber-500 dark:text-amber-400",
	"text-rose-500 dark:text-rose-400",
	"text-cyan-500 dark:text-cyan-400",
	"text-violet-500 dark:text-violet-400",
	"text-teal-500 dark:text-teal-400",
];

function getColorIndex(name: string): number {
	let hash = 0;
	for (let i = 0; i < name.length; i++) {
		hash = name.charCodeAt(i) + ((hash << 5) - hash);
	}
	return Math.abs(hash) % COLORS.length;
}

export function CompanyCard({ company }: CompanyCardProps) {
	const colorIndex = getColorIndex(company.name);
	const hasOpenJobs = company.totalOpenJobs !== undefined && company.totalOpenJobs > 0;
	const categories = company.categories ?? [];

	return (
		<Link
			to={`/companies/${company.slug}`}
			className='group block rounded-xl focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2'
		>
			<Card className='flex flex-col overflow-hidden transition-all duration-300 hover:-translate-y-0.5 hover:border-primary/50 hover:shadow-md'>
				<div className='flex flex-row px-4'>
					<div
						className={cn(
							"flex w-22 min-h-22 shrink-0 items-center justify-center bg-linear-to-br sm:w-30 sm:min-h-30",
							COLORS[colorIndex],
						)}
					>
						{company.logoUrl ? (
							<img
								src={company.logoUrl}
								alt={`${company.name} logo`}
								className='block h-12 w-12 shrink-0 object-contain drop-shadow-sm transition-transform duration-500 ease-out group-hover:scale-110 sm:h-16 sm:w-16'
							/>
						) : (
							<span
								className={cn(
									"select-none text-2xl font-bold tracking-tight drop-shadow-sm sm:text-3xl",
									LETTER_COLORS[colorIndex],
								)}
								aria-hidden='true'
							>
								{company.name.charAt(0).toUpperCase()}
							</span>
						)}
					</div>

					<CardContent className='flex min-w-0 flex-1 flex-col gap-1.5 p-4'>
						<div className='flex items-start justify-between gap-2'>
							<h3 className='truncate text-base font-semibold text-foreground transition-colors group-hover:text-primary'>
								{company.name}
							</h3>

							{hasOpenJobs && (
								<Badge
									variant='secondary'
									className='shrink-0 gap-1 rounded-full px-2 py-0.5 text-xs'
								>
									<Briefcase className='size-5' />
									{company.totalOpenJobs}
									<span className='sr-only'>open positions</span>
								</Badge>
							)}
						</div>

						{company.description && (
							<p className='line-clamp-2 text-sm leading-relaxed text-muted-foreground'>
								{company.description}
							</p>
						)}

						{company.address && (
							<div className='mt-auto flex items-center gap-1 pt-1 text-xs text-muted-foreground/80'>
								<MapPin className='size-3.5 shrink-0' />
								<span className='truncate'>{company.address}</span>
							</div>
						)}
					</CardContent>
				</div>

				{/* Slider Footer */}
				{categories.length > 0 && (
					<CardFooter className='flex items-center gap-1.5 overflow-x-auto border-t bg-muted/30 px-4 py-2.5 scrollbar-none [&::-webkit-scrollbar]:hidden'>
						{categories.map((cat) => (
							<Badge
								key={cat.id}
								variant='outline'
								className='shrink-0 rounded-full bg-background px-2.5 py-0.5 text-xs font-normal text-muted-foreground'
							>
								{cat.name}
							</Badge>
						))}
					</CardFooter>
				)}
			</Card>
		</Link>
	);
}

export function CompanyCardSkeleton() {
	return (
		<Card className='flex flex-col overflow-hidden'>
			<div className='flex flex-row'>
				<Skeleton className='w-22 min-h-22 shrink-0 rounded-none sm:w-30 sm:min-h-30' />
				<CardContent className='flex flex-1 flex-col gap-3 p-4'>
					<div className='flex items-center justify-between gap-2'>
						<Skeleton className='h-4 w-2/3' />
						<Skeleton className='h-5 w-12 rounded-full' />
					</div>
					<Skeleton className='h-3 w-full' />
					<Skeleton className='h-3 w-2/5' />
				</CardContent>
			</div>
			{/* Skeleton Footer aligned with the slider layout */}
			<CardFooter className='flex gap-1.5 overflow-hidden border-t bg-muted/30 px-4 py-2.5'>
				<Skeleton className='h-5 w-20 shrink-0 rounded-full' />
				<Skeleton className='h-5 w-16 shrink-0 rounded-full' />
				<Skeleton className='h-5 w-24 shrink-0 rounded-full' />
			</CardFooter>
		</Card>
	);
}
