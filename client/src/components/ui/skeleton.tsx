import { cn } from "@/lib/utils"

function Skeleton({ className, ...props }: React.ComponentProps<"div">) {
	return (
		<div
			data-slot="skeleton"
			className={cn("animate-pulse rounded-md bg-muted", className)}
			{...props}
		/>
	)
}

function PDFSkeleton() {
	return (
		<div className='w-full max-w-130 bg-background border border-muted-foreground/20 rounded-[6px] p-6 sm:p-7 box-border'>
			<Skeleton className='h-2.5 w-[55%] mb-4.5' />
			<Skeleton className='h-1.75 mb-2.5' />
			<Skeleton className='h-1.75 w-[70%] mb-2.5' />
			<Skeleton className='h-1.75 mb-2.5' />
			<Skeleton className='h-1.75 w-[45%] mb-2.5' />
			<div className='mt-4 space-y-2.5'>
				<Skeleton className='h-1.75 w-[30%]' />
				<Skeleton className='h-1.75' />
				<Skeleton className='h-1.75 w-[70%]' />
				<Skeleton className='h-1.75' />
			</div>
			<div className='mt-4 space-y-2.5'>
				<Skeleton className='h-1.75 w-[30%]' />
				<Skeleton className='h-1.75' />
				<Skeleton className='h-1.75 w-[45%]' />
				<Skeleton className='h-1.75 w-[70%]' />
				<Skeleton className='h-1.75' />
			</div>
		</div>
	)
}

export { PDFSkeleton, Skeleton }

