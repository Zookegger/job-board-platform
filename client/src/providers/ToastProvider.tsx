import { createContext, useContext, type ReactNode } from "react"
import { toast as sonnerToast } from "sonner"

type ToastOptions = {
	position?: "top-left" | "top-right" | "bottom-left" | "bottom-right" | "top-center" | "bottom-center"
	duration?: number
}

type ToastContextType = {
	success: (message: string, opts?: ToastOptions) => string | number
	error: (message: string, opts?: ToastOptions) => string | number
	info: (message: string, opts?: ToastOptions) => string | number
}

const ToastContext = createContext<ToastContextType | null>(null)

export function ToastProvider({
	children,
	defaultPosition = "top-right",
}: {
	children: ReactNode
	defaultPosition?: ToastOptions["position"]
}) {
	const toast: ToastContextType = {
		success: (message, opts) =>
			sonnerToast.success(message, {
				position: opts?.position ?? defaultPosition,
				duration: opts?.duration,
			}),
		error: (message, opts) =>
			sonnerToast.error(message, {
				position: opts?.position ?? defaultPosition,
				duration: opts?.duration,
			}),
		info: (message, opts) =>
			sonnerToast(message, {
				position: opts?.position ?? defaultPosition,
				duration: opts?.duration,
			}),
	}

	return <ToastContext.Provider value={toast}>{children}</ToastContext.Provider>
}

export function useToast() {
	const ctx = useContext(ToastContext)
	if (!ctx) throw new Error("useToast must be used within ToastProvider")
	return ctx
}
