// components/filter-toolbar.tsx
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { cn } from "@/lib/utils";
import { RefreshCcw, Search } from "lucide-react";

export interface FilterOption {
	value: string;
	label: string;
}

export interface FilterSelectConfig {
	/** unique key, also used as React key */
	key: string;
	value: string;
	onValueChange: (value: string) => void;
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
	return (
		<div className={cn("flex flex-col gap-3 sm:flex-row sm:flex-wrap sm:items-center", className)}>
			{onSearchChange && (
				<Input
					value={searchValue}
					onChange={(event) => onSearchChange(event.target.value)}
					placeholder={searchPlaceholder}
					startIcon={<Search className='size-4' />}
					className='h-10 bg-background sm:min-w-55 sm:flex-1'
				/>
			)}

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
				>
					Xóa lọc
				</Button>
			)}

			{onRefetch && (
				<Button
					variant='outline'
					onClick={onRefetch}
					disabled={isFetching}
				>
					<RefreshCcw className={isFetching ? "animate-spin" : ""} />
					Tải lại
				</Button>
			)}
		</div>
	);
}
