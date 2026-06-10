import type { NavigateFunction } from "react-router-dom";

let navigateFn: NavigateFunction | null = null;

export function setNavigate(n: NavigateFunction) {
  navigateFn = n;
}

export function navigateTo(path: string, options?: { replace?: boolean }) {
  if (navigateFn) {
    navigateFn(path, options);
  } else {
    window.location.replace(path);
  }
}
