import { cva, type VariantProps } from "class-variance-authority";
import { Avatar } from "radix-ui";

import { cn } from "@/lib/utils";

const avatarVariants = cva(
	"rounded-full overflow-hidden flex items-center justify-center bg-gray-200 text-gray-600 font-medium shrink-0",
	{
		variants: {
			size: {
				sm: "size-8 text-xs",
				md: "size-10 text-sm",
				lg: "size-16 text-lg",
				xl: "size-20 text-xl",
			},
		},
		defaultVariants: {
			size: "md",
		},
	},
);

type UserAvatarProps = {
	fullName: string;
	avatarUrl?: string | null;
	className?: string;
	fill?: boolean;
} & VariantProps<typeof avatarVariants>;

export default function UserAvatar({ fullName, avatarUrl, size, fill, className }: UserAvatarProps) {
	return (
		// <Avatar.Root className={cn(avatarVariants({ size }), fill && "h-full aspect-square", className)}>
		<Avatar.Root className={cn(fill ? "h-full aspect-square" : avatarVariants({ size }), className)}>
			<Avatar.Image
				src={avatarUrl ?? undefined}
				alt={fullName}
				className='size-full object-fill'
			/>
			<Avatar.Fallback className='flex size-full items-center justify-center bg-gray-200 text-gray-600'>
				{fullName?.charAt(0)?.toUpperCase()}
			</Avatar.Fallback>
		</Avatar.Root>
	);
}
