import { useAuth } from "@/hooks/useAuth";
import { UserRole } from "@/types/auth";
import { Navigate } from "react-router-dom";
import CandidateProfile from "./components/CandidateProfile";
import EmployerProfile from "./components/EmployerProfile";

export default function ProfilePage() {
	const { user, isAuthenticated } = useAuth();

	if (!isAuthenticated) {
		return (
			<Navigate
				to='/login'
				replace
			/>
		);
	}

	if (user && user.role == UserRole.CANDIDATE) return <CandidateProfile />;
	if (user && user.role == UserRole.EMPLOYER) return <EmployerProfile />;
}
