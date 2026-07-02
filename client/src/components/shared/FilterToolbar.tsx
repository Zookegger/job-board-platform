import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { cn } from "@/lib/utils";
import { Filter, RefreshCcw, Search } from "lucide-react";
import { useState } from "react";

export interface FilterOption {
	value: string;
	label: string;
}

export interface FilterSelectConfig {
	/** unique key, also used as React key */
	key: string;
	value: string;
	onValueChange?(value: string): void;
	placeholder: string;
	options: FilterOption[];
	className?: string;
}

export interface FilterToolbarProps {
	searchValue?: string;
	onSearchChange?: (value: string) => void;
	searchPlaceholder?: string;
	selects?: FilterSelectConfig[];
	onReset?: () => void;
	resetDisabled?: boolean;
	onRefetch?: () => void;
	isFetching?: boolean;
	className?: string;
}

export function FilterToolbar({
	searchValue,
	onSearchChange,
	searchPlaceholder = "Tìm kiếm...",
	selects = [],
	onReset,
	resetDisabled,
	onRefetch,
	isFetching,
	className,
}: FilterToolbarProps) {
	// State to track mobile filter visibility
	const [isFiltersOpen, setIsFiltersOpen] = useState(false);

	const hasFilters = selects.length > 0 || onReset || onRefetch;

	return (
		<div className={cn("flex flex-col gap-3", className)}>
			{/* Top Row: Search Input + Mobile Filter Toggle */}
			<div className='flex items-center gap-2'>
				{onSearchChange && (
					<Input
						value={searchValue}
						onChange={(event) => onSearchChange(event.target.value)}
						placeholder={searchPlaceholder}
						startIcon={<Search className='size-4' />}
						className='h-10 bg-background flex-1 sm:min-w-55'
					/>
				)}

				{hasFilters && (
					<Button
						variant={isFiltersOpen ? "secondary" : "outline"}
						size='icon'
						onClick={() => setIsFiltersOpen(!isFiltersOpen)}
						className='sm:hidden shrink-0'
						aria-label='Toggle filters'
					>
						<Filter className='size-4' />
					</Button>
				)}
			</div>

			{/* Filters Container */}
			<div
				className={cn(
					"gap-3 sm:flex-row sm:flex-wrap sm:items-center",
					isFiltersOpen ? "flex flex-col" : "hidden sm:flex",
				)}
			>
				{selects.map((select) => (
					<Select
						key={select.key}
						value={select.value}
						onValueChange={select.onValueChange}
					>
						<SelectTrigger className={cn("w-full sm:w-48", select.className)}>
							<SelectValue placeholder={select.placeholder} />
						</SelectTrigger>
						<SelectContent>
							{select.options.map((option) => (
								<SelectItem
									key={option.value}
									value={option.value}
								>
									{option.label}
								</SelectItem>
							))}
						</SelectContent>
					</Select>
				))}

				{onReset && (
					<Button
						variant='outline'
						onClick={onReset}
						disabled={resetDisabled}
						className='w-full sm:w-auto' // Full width on mobile, auto on desktop
					>
						Xóa lọc
					</Button>
				)}

				{onRefetch && (
					<Button
						variant='outline'
						onClick={onRefetch}
						disabled={isFetching}
						className='w-full sm:w-auto'
					>
						<RefreshCcw className={isFetching ? "animate-spin" : ""} />
						Tải lại
					</Button>
				)}
			</div>
		</div>
	);
}
