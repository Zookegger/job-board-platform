import { createBrowserRouter, Navigate } from 'react-router-dom'
import ProtectedRoute from './ProtectedRoute'
import { PublicLayout } from '@/layouts/PublicLayout'
import { CandidateLayout } from '@/layouts/CandidateLayout'
import EmployerLayout from '@/layouts/EmployerLayout'
import AdminLayout from '@/layouts/AdminLayout'
import { UserRole } from '@/types/auth'
import RouterRoutes from '@/utils/RouterRoutes'

import HomePage from '@/features/home/HomePage'
import { LoginPage } from '@/features/auth/LoginPage'
import RegisterPage from '@/features/auth/RegisterPage'
import { ForgotPasswordPage } from '@/features/auth/ForgotPasswordPage'
import { JobListPage } from '@/features/jobs/JobListPage'
import { JobDetailPage } from '@/features/jobs/JobDetailPage'
import NotFoundPage from '@/features/home/NotFoundPage'

import CandidateProfilePage from '@/features/candidate/ProfilePage'
import CandidateApplicationsPage from '@/features/candidate/ApplicationsPage'
import CandidateSavedJobsPage from '@/features/candidate/SavedJobsPage'
import CandidateSettingsPage from '@/features/candidate/SettingsPage'

import EmployerDashboardPage from '@/features/employer/DashboardPage'
import EmployerJobsPage from '@/features/employer/JobsPage'
import EmployerCreateJobPage from '@/features/employer/CreateJobPage'
import EmployerJobDetailPage from '@/features/employer/JobDetailPage'
import EmployerApplicationsPage from '@/features/employer/ApplicationsPage'
import EmployerCompanyPage from '@/features/employer/CompanyPage'
import EmployerSettingsPage from '@/features/employer/SettingsPage'

import AdminDashboardPage from '@/features/admin/DashboardPage'
import AdminUsersPage from '@/features/admin/UsersPage'
import AdminCompaniesPage from '@/features/admin/CompaniesPage'
import AdminJobsPage from '@/features/admin/JobsPage'
import AdminReportsPage from '@/features/admin/ReportsPage'
import AdminSettingsPage from '@/features/admin/SettingsPage'

export const router = createBrowserRouter([
  // Public routes
  {
    element: <PublicLayout />,
    children: [
      { index: true, element: <HomePage /> },
      { path: RouterRoutes.LOGIN, element: <LoginPage /> },
      { path: RouterRoutes.REGISTER, element: <RegisterPage /> },
      { path: RouterRoutes.FORGOT_PASSWORD, element: <ForgotPasswordPage /> },
      { path: RouterRoutes.JOBS, element: <JobListPage /> },
      { path: RouterRoutes.JOB_DETAIL(':id'), element: <JobDetailPage /> },
      { path: 'unauthorized', element: <NotFoundPage /> },
    ],
  },

  // Candidate routes (role-protected)
  {
    element: <ProtectedRoute allowedRoles={[UserRole.CANDIDATE]} />,
    children: [
      {
        element: <CandidateLayout />,
        children: [
          { index: true, element: <Navigate to={RouterRoutes.CANDIDATE_PROFILE} replace /> },
          { path: 'candidate/profile', element: <CandidateProfilePage /> },
          { path: 'candidate/applications', element: <CandidateApplicationsPage /> },
          { path: 'candidate/saved-jobs', element: <CandidateSavedJobsPage /> },
          { path: 'candidate/settings', element: <CandidateSettingsPage /> },
        ],
      },
    ],
  },

  // Employer routes (role-protected)
  {
    element: <ProtectedRoute allowedRoles={[UserRole.EMPLOYER]} />,
    children: [
      {
        element: <EmployerLayout />,
        children: [
          { index: true, element: <Navigate to={RouterRoutes.EMPLOYER_DASHBOARD} replace /> },
          { path: 'employer/dashboard', element: <EmployerDashboardPage /> },
          { path: 'employer/jobs', element: <EmployerJobsPage /> },
          { path: 'employer/jobs/new', element: <EmployerCreateJobPage /> },
          { path: 'employer/jobs/:id', element: <EmployerJobDetailPage /> },
          { path: 'employer/applications', element: <EmployerApplicationsPage /> },
          { path: 'employer/company', element: <EmployerCompanyPage /> },
          { path: 'employer/settings', element: <EmployerSettingsPage /> },
        ],
      },
    ],
  },

  // Admin routes (role-protected)
  {
    element: <ProtectedRoute allowedRoles={[UserRole.ADMIN]} />,
    children: [
      {
        element: <AdminLayout />,
        children: [
          { index: true, element: <Navigate to={RouterRoutes.ADMIN_DASHBOARD} replace /> },
          { path: 'admin/dashboard', element: <AdminDashboardPage /> },
          { path: 'admin/users', element: <AdminUsersPage /> },
          { path: 'admin/companies', element: <AdminCompaniesPage /> },
          { path: 'admin/jobs', element: <AdminJobsPage /> },
          { path: 'admin/reports', element: <AdminReportsPage /> },
          { path: 'admin/settings', element: <AdminSettingsPage /> },
        ],
      },
    ],
  },

  // 404
  { path: '*', element: <NotFoundPage /> },
])
