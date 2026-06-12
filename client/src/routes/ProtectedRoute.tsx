import { LoadingBackdrop } from "@/components/shared/LoadingBackdrop";
import AdminLayout from "@/layouts/AdminLayout";
import { CandidateLayout } from "@/layouts/CandidateLayout";
import EmployerLayout from "@/layouts/EmployerLayout";
import { UserRole } from "@/types/auth";
import { Navigate, Outlet, useLocation } from "react-router-dom";
import { useAuth } from "../hooks/useAuth";

const ROLE_LAYOUTS: Partial<Record<UserRole, React.ElementType>> = {
	[UserRole.CANDIDATE]: CandidateLayout,
	[UserRole.EMPLOYER]: EmployerLayout,
	[UserRole.ADMIN]: AdminLayout,
};

export default function ProtectedRoute({ allowedRoles }: { allowedRoles: UserRole[] }) {
	const { isAuthenticated, user, isLoading } = useAuth();
	const location = useLocation();

	if (isLoading) return <LoadingBackdrop />;

	if (!isAuthenticated) {
		return (
			<Navigate
				to='/login'
				state={{ from: location }}
				replace
			/>
		);
	}

	const hasAccess = !!user?.role && allowedRoles.includes(user.role);

	if (!hasAccess) {
		return (
			<Navigate
				to='/unauthorized'
				state={{ from: location }}
				replace
			/>
		);
	}

	const Layout = user?.role ? ROLE_LAYOUTS[user.role] : null;

	return Layout ? (
		<Layout>
			<Outlet />
		</Layout>
	) : (
		<Outlet />
	);
}
