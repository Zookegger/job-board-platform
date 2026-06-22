import { PublicLayout } from "@/layouts/PublicLayout";
import { setNavigate } from "@/lib/navigate";
import { UserRole } from "@/types/auth";
import RouterRoutes from "@/utils/RouterRoutes";
import { useEffect } from "react";
import { createBrowserRouter, Navigate, Outlet, useNavigate } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";

import { ForgotPasswordPage } from "@/features/auth/ForgotPasswordPage";
import { LoginPage } from "@/features/auth/LoginPage";
import RegisterPage from "@/features/auth/RegisterPage";
import HomePage from "@/features/home/HomePage";
import NotFoundPage from "@/features/home/NotFoundPage";
import { JobDetailPage } from "@/features/jobs/JobDetailPage";
import { JobListPage } from "@/features/jobs/JobListPage";

import CandidateApplicationsPage from "@/features/candidate/ApplicationsPage";
import CandidateSavedJobsPage from "@/features/candidate/SavedJobsPage";
import CandidateSettingsPage from "@/features/candidate/SettingsPage";

import EmployerApplicationsPage from "@/features/employer/ApplicationsPage";
import EmployerCompanyPage from "@/features/employer/CompanyPage";
import EmployerCompanyStatusPage from "@/features/employer/CompanyStatusPage";
import EmployerCreateJobPage from "@/features/employer/CreateJobPage";
import EmployerDashboardPage from "@/features/employer/DashboardPage";
import EmployerJobDetailPage from "@/features/employer/JobDetailPage";
import EmployerJobsPage from "@/features/employer/JobsPage";
import EmployerSettingsPage from "@/features/employer/SettingsPage";

import AdminCompaniesPage from "@/features/admin/CompaniesPage";
import AdminDashboardPage from "@/features/admin/DashboardPage";
import AdminJobsPage from "@/features/admin/JobsPage";
import AdminReportsPage from "@/features/admin/ReportsPage";
import AdminSettingsPage from "@/features/admin/SettingsPage";
import AdminSkillPage from "@/features/admin/SkillsPage";
import AdminUsersPage from "@/features/admin/UsersPage";
import UnauthorizedPage from "@/features/home/UnauthorizedPage";
import ProfilePage from "@/features/profile/ProfilePage";
import PublicCompanyPage from "@/features/public/PublicCompanyPage";
// eslint-disable-next-line react-refresh/only-export-components
function RouterInit() {
	const navigate = useNavigate();
	useEffect(() => {
		setNavigate(navigate);
	}, [navigate]);
	return <Outlet />;
}

export const router = createBrowserRouter([
	{
		element: <RouterInit />,
		children: [
			// Public routes
			{
				element: <PublicLayout />,
				children: [
					{ index: true, element: <HomePage /> },
					{ path: RouterRoutes.LOGIN, element: <LoginPage /> },
					{ path: RouterRoutes.REGISTER, element: <RegisterPage /> },
					{ path: RouterRoutes.FORGOT_PASSWORD, element: <ForgotPasswordPage /> },
					{ path: RouterRoutes.JOBS, element: <JobListPage /> },
					{ path: RouterRoutes.JOB_DETAIL(":id"), element: <JobDetailPage /> },
					{ path: RouterRoutes.UNAUTHORIZED, element: <UnauthorizedPage /> },
				],
			},
			// Shared authenticated routes (accessible by both candidates and employers)
			{
				element: <ProtectedRoute allowedRoles={[UserRole.CANDIDATE, UserRole.EMPLOYER]}></ProtectedRoute>,
				children: [
					{
						path: RouterRoutes.PROFILE,
						element: <ProfilePage />,
					},
				],
			},

			// Candidate routes (role-protected)
			{
				element: <ProtectedRoute allowedRoles={[UserRole.CANDIDATE]} />,
				children: [
					{
						children: [
							{ path: RouterRoutes.CANDIDATE_APPLICATIONS, element: <CandidateApplicationsPage /> },
							{ path: RouterRoutes.CANDIDATE_SAVED_JOBS, element: <CandidateSavedJobsPage /> },
							{ path: RouterRoutes.CANDIDATE_SETTINGS, element: <CandidateSettingsPage /> },
						],
					},
				],
			},

			// Employer routes (role-protected)
			{
				element: <ProtectedRoute allowedRoles={[UserRole.EMPLOYER]} />,
				children: [
					{
						children: [
							{
								index: true,
								element: (
									<Navigate
										to={RouterRoutes.EMPLOYER_DASHBOARD}
										replace
									/>
								),
							},
							{ path: RouterRoutes.EMPLOYER_DASHBOARD, element: <EmployerDashboardPage /> },
							{ path: RouterRoutes.EMPLOYER_JOBS, element: <EmployerJobsPage /> },
							{ path: RouterRoutes.EMPLOYER_CREATE_JOB, element: <EmployerCreateJobPage /> },
							{ path: RouterRoutes.EMPLOYER_JOB_DETAIL(":id"), element: <EmployerJobDetailPage /> },
							{ path: RouterRoutes.EMPLOYER_APPLICATIONS, element: <EmployerApplicationsPage /> },
							{ path: RouterRoutes.EMPLOYER_COMPANY, element: <EmployerCompanyPage /> },
							{ path: RouterRoutes.EMPLOYER_COMPANY_STATUS, element: <EmployerCompanyStatusPage /> },
							{ path: RouterRoutes.EMPLOYER_SETTINGS, element: <EmployerSettingsPage /> },
						],
					},
				],
			},

			// Admin routes (role-protected)
			{
				element: <ProtectedRoute allowedRoles={[UserRole.ADMIN]} />,
				children: [
					{
						children: [
							{
								index: true,
								element: (
									<Navigate
										to={RouterRoutes.ADMIN_DASHBOARD}
										replace
									/>
								),
							},
							{ path: RouterRoutes.ADMIN_DASHBOARD, element: <AdminDashboardPage /> },
							{ path: RouterRoutes.ADMIN_USERS, element: <AdminUsersPage /> },
							{ path: RouterRoutes.ADMIN_COMPANIES, element: <AdminCompaniesPage /> },
							{ path: RouterRoutes.ADMIN_JOBS, element: <AdminJobsPage /> },
							{ path: RouterRoutes.ADMIN_REPORTS, element: <AdminReportsPage /> },
							{ path: RouterRoutes.ADMIN_SETTINGS, element: <AdminSettingsPage /> },
							{ path: RouterRoutes.ADMIN_SKILLS, element: <AdminSkillPage /> },
						],
					},
				],
			},

			{
				path: "/companies/:companyId",
				element: <PublicCompanyPage />,
			},

			// 404
			{ path: "*", element: <NotFoundPage /> },
		],
	},
]);
