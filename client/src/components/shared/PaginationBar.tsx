import { useMemo } from "react";
import { Button } from "@/components/ui/button";
import { ChevronLeft, ChevronRight } from "lucide-react";

export interface PaginationConfig {
	page: number;
	pageSize?: number;
	totalPages: number;
	totalElements?: number;
	isFetching: boolean;
	onPageChange: (page: number) => void;
	onPageSizeChange?: (size: number) => void;
	label?: string;
}

export function PaginationBar({
	page,
	totalPages,
	isFetching,
	onPageChange,
}: PaginationConfig) {
	const pageNumbers = useMemo(() => {
		const items: (number | "ellipsis")[] = [];
		if (totalPages <= 1) return items;

		const start = Math.max(2, page - 1);
		const end = Math.min(totalPages - 1, page + 1);

		items.push(1);
		if (start > 2) items.push("ellipsis");
		for (let i = start; i <= end; i++) items.push(i);
		if (end < totalPages - 1) items.push("ellipsis");
		items.push(totalPages);

		return items;
	}, [page, totalPages]);

	if (totalPages <= 1) return null;

	return (
		<div className='flex items-center justify-center gap-1 py-4'>
			<Button
				variant='outline'
				size='sm'
				className='h-8 w-8 p-0'
				disabled={page === 0 || isFetching}
				onClick={() => onPageChange(page - 1)}
			>
				<ChevronLeft className='h-4 w-4' />
			</Button>

			{pageNumbers.map((item, index) =>
				item === "ellipsis" ? (
					<span
						key={`e-${index}`}
						className='flex h-8 w-8 items-center justify-center text-sm text-muted-foreground'
					>
						...
					</span>
				) : (
					<Button
						key={item}
						variant={item === page + 1 ? "default" : "outline"}
						size='sm'
						className='h-8 w-8 p-0'
						disabled={isFetching}
						onClick={() => onPageChange(item - 1)}
					>
						{item}
					</Button>
				),
			)}

			<Button
				variant='outline'
				size='sm'
				className='h-8 w-8 p-0'
				disabled={page >= totalPages - 1 || isFetching}
				onClick={() => onPageChange(page + 1)}
			>
				<ChevronRight className='h-4 w-4' />
			</Button>
		</div>
	);
}
