import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
} from "@/components/ui/dialog";
import type { ReactNode } from "react";

export interface BaseDialogProps {
	/** Controls whether the modal is visible */
	isOpen: boolean;
	/** Callback fired when the user clicks the 'X', presses Escape, or clicks the overlay */
	onClose: () => void;
	/** The main heading of the modal */
	title: string;
	/** Optional subtitle/instructions */
	description?: string;
	/** The actual content (forms, inputs, text) injected into the modal */
	children?: ReactNode;
	/** Optional buttons injected into the bottom right corner */
	footer?: ReactNode;
	/** Whether the dialog should be modal (i.e. disable closing on Escape/Click outside) */
	modal?: boolean;
	/** The size of the dialog */
	size?: "sm" | "md" | "lg" | "xl" | "2xl" | "3xl";
}

export function BaseDialog({ isOpen, onClose, title, description, children, footer, modal, size }: BaseDialogProps) {
	const sizeClass = size ? `max-w-${size}` : "max-w-md";

	return (
		<Dialog
			modal={modal}
			open={isOpen}
			// Only fire onClose if the dialog is trying to close itself via native actions (Escape/Click outside)
			onOpenChange={(open) => {
				if (!open) onClose();
			}}
		>
			<DialogContent className={sizeClass + " p-3 sm:p-6"}>
				<DialogHeader>
					<DialogTitle>{title}</DialogTitle>
					{description && <DialogDescription>{description}</DialogDescription>}
				</DialogHeader>
				
				{children}

				{footer && <DialogFooter>{footer}</DialogFooter>}
			</DialogContent>
		</Dialog>
	);
}
