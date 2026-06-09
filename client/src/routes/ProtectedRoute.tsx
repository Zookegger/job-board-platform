import { LoadingBackdrop } from "@/components/shared/LoadingBackdrop";
import type { UserRole } from "@/types/auth";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

export default function ProtectedRoute({
	allowedRoles,
}: {
	allowedRoles: UserRole[];
	children?: React.ReactNode;
	fallback?: React.ReactNode;
}) {
	const { isAuthenticated, user, isLoading } = useAuth();
	const location = useLocation();

	if (isLoading) {
		return <LoadingBackdrop />;
	}

	if (!isAuthenticated) {
		return (
			<Navigate
				to='/login'
				state={{ from: location }}
				replace
			/>
		);
	}

	const hasAccess = allowedRoles.includes(user?.role as UserRole);

	if (!hasAccess) {
		return (
			<Navigate
				to='/unauthorized'
				state={{ from: location }}
				replace
			/>
		);
	}

	return <Outlet />;
}
