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

import CandidateApplicationDetailPage from "@/features/candidate/ApplicationDetailPage";
import CandidateApplicationsPage from "@/features/candidate/ApplicationsPage";
import NotificationsPage from "@/features/shared/NotificationsPage";


import EmployerCompanyPage from "@/features/employer/CompanyPage";
import EmployerDashboardPage from "@/features/employer/DashboardPage";
import EmployerJobDetailPage from "@/features/employer/JobDetailPage";
import EmployerJobsPage from "@/features/employer/JobsPage";

import AdminCompaniesPage from "@/features/admin/CompaniesPage";
import AdminDashboardPage from "@/features/admin/DashboardPage";
import AdminStatisticsPage from "@/features/admin/StatisticsPage";
import AdminJobsPage from "@/features/admin/JobsPage";
import AdminReportsPage from "@/features/admin/ReportsPage";
import AdminSkillPage from "@/features/admin/SkillsPage";
import AdminUsersPage from "@/features/admin/UsersPage";
import UnauthorizedPage from "@/features/home/UnauthorizedPage";
import ProfilePage from "@/features/profile/ProfilePage";
import PublicCompanyPage from "@/features/public/CompanyDetailPage";
import CompanyListPage from "@/features/public/CompanyListPage";
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
					{ path: RouterRoutes.JOB_DETAIL(":slug"), element: <JobDetailPage /> },
					{ path: RouterRoutes.COMPANIES, element: <CompanyListPage /> },
					{ path: RouterRoutes.COMPANY_DETAIL(":slug"), element: <PublicCompanyPage /> },
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
					{
						path: RouterRoutes.NOTIFICATIONS,
						element: <NotificationsPage />,
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
							{ path: RouterRoutes.CANDIDATE_APPLICATION_DETAIL(":id"), element: <CandidateApplicationDetailPage /> },
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
							{ path: RouterRoutes.EMPLOYER_JOB_DETAIL(":slug"), element: <EmployerJobDetailPage /> },
							{ path: RouterRoutes.EMPLOYER_COMPANY, element: <EmployerCompanyPage /> },
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
							{ path: RouterRoutes.ADMIN_STATISTICS, element: <AdminStatisticsPage /> },
							{ path: RouterRoutes.ADMIN_USERS, element: <AdminUsersPage /> },
							{ path: RouterRoutes.ADMIN_COMPANIES, element: <AdminCompaniesPage /> },
							{ path: RouterRoutes.ADMIN_JOBS, element: <AdminJobsPage /> },
							{ path: RouterRoutes.ADMIN_REPORTS, element: <AdminReportsPage /> },
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
