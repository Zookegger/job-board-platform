import { useSyncExternalStore } from "react";

const breakpoints = {
	sm: "(min-width: 640px)",
	md: "(min-width: 768px)",
	lg: "(min-width: 1024px)",
	xl: "(min-width: 1280px)",
	"2xl": "(min-width: 1536px)",
} as const;

export type Breakpoint = keyof typeof breakpoints;

export function useMediaQuery(breakpoint: Breakpoint): boolean {
	const query = breakpoints[breakpoint];

	return useSyncExternalStore(
		(callback) => {
			const matchMedia = window.matchMedia(query);

			matchMedia.addEventListener("change", callback);
			return () => matchMedia.removeEventListener("change", callback);
		},
		() => window.matchMedia(query).matches,
		() => false,
	);
}
