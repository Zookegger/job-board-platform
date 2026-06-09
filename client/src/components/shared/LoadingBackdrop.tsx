import { cn } from "@/lib/utils";
import { LoaderCircle } from "lucide-react";

interface LoadingBackdropProps extends React.ComponentPropsWithoutRef<"div"> {
	isLoading?: boolean;
	variant?: "fullscreen" | "container";
	spinnerSize?: number;
	label?: string;
}

function LoadingBackdrop({
	isLoading = true,
	variant = "fullscreen",
	spinnerSize = 24,
	label = "Đang tải...",
	className,
	children,
	...props
}: LoadingBackdropProps) {
	if (!isLoading) return <>{children}</>;

	const backdrop = (
		<div
			data-slot='loading-backdrop'
			role='status'
			aria-label={label}
			className={cn(
				variant === "fullscreen" ? "fixed inset-0 z-50" : "absolute inset-0",
				"flex items-center justify-center bg-background/80 backdrop-blur-xs transition-all",
				className,
			)}
			{...props}
		>
			<LoaderCircle
				className='animate-spin text-muted-foreground'
				size={spinnerSize}
			/>
			<span className='sr-only'>{label}</span>
		</div>
	);

	if (!children) return backdrop;

	return (
		<div className='relative'>
			<div className='pointer-events-none opacity-50'>{children}</div>
			{backdrop}
		</div>
	);
}

export { LoadingBackdrop };
