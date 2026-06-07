import { cn } from "@/lib/utils";
import { Input as InputPrimitive } from "@base-ui/react/input";
import * as React from "react";

interface InputProps extends React.ComponentProps<typeof InputPrimitive> {
	startIcon?: React.ReactNode;
	endIcon?: React.ReactNode;
}

const Input = React.forwardRef<HTMLInputElement, InputProps>(
	({ className, type, startIcon, endIcon, ...props }, ref) => {
		return (
			<div
				className={cn(
					"flex h-fit w-full min-w-0 items-center rounded-lg border border-input bg-transparent px-2.5 py-2 transition-colors",
					"focus-within:border-ring focus-within:ring-3 focus-within:ring-ring/50",
					"has-disabled:pointer-events-none has-disabled:cursor-not-allowed has-disabled:bg-input/50 has-disabled:opacity-50",
					"aria-invalid:border-destructive aria-invalid:ring-3 aria-invalid:ring-destructive/20",
					"dark:bg-input/30",
					className,
				)}
			>
				{startIcon && (
					<div className='mr-2 flex items-center justify-center shrink-0 text-muted-foreground'>
						{startIcon}
					</div>
				)}

				<InputPrimitive
					ref={ref}
					type={type}
					data-slot='input'
					className='w-full flex-1 self-stretch h-auto bg-transparent py-0 text-base outline-none placeholder:text-muted-foreground md:text-sm'
					{...props}
				/>

				{endIcon && (
					<div className='ml-2 flex items-center justify-center shrink-0 text-muted-foreground'>
						{endIcon}
					</div>
				)}
			</div>
		);
	},
);

Input.displayName = "Input";

export { Input };
