import * as SliderPrimitive from "@radix-ui/react-slider";

import { cn } from "@/lib/utils";

function Slider({
	className,
	defaultValue,
	value,
	min = 0,
	max = 100,
	step = 1,
	...props
}: React.ComponentProps<typeof SliderPrimitive.Root>) {
	return (
		<SliderPrimitive.Root
			data-slot="slider"
			defaultValue={defaultValue}
			value={value}
			min={min}
			max={max}
			step={step}
			className={cn(
				"relative flex w-full touch-none select-none items-center data-disabled:opacity-50 data-disabled:cursor-not-allowed",
				className
			)}
			{...props}
		>
			<SliderPrimitive.Track
				data-slot="slider-track"
				className="relative h-1.5 w-full grow overflow-hidden rounded-full bg-primary/10"
			>
				<SliderPrimitive.Range
					data-slot="slider-range"
					className="absolute h-full bg-primary"
				/>
			</SliderPrimitive.Track>
			<SliderPrimitive.Thumb
				data-slot="slider-thumb"
				className="block size-4 rounded-full border border-primary/50 bg-background shadow-sm transition-[color,box-shadow] hover:border-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50"
			/>
			<SliderPrimitive.Thumb
				data-slot="slider-thumb"
				className="block size-4 rounded-full border border-primary/50 bg-background shadow-sm transition-[color,box-shadow] hover:border-primary focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring/50 disabled:pointer-events-none disabled:opacity-50"
			/>
		</SliderPrimitive.Root>
	);
}

export { Slider };
