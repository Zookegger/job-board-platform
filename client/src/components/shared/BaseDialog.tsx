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
}

export function BaseDialog({ isOpen, onClose, title, description, children, footer }: BaseDialogProps) {
	return (
		<Dialog
			open={isOpen}
			// Only fire onClose if the dialog is trying to close itself via native actions (Escape/Click outside)
			onOpenChange={(open) => {
				if (!open) onClose();
			}}
		>
			<DialogContent className='sm:max-w-106.25'>
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
