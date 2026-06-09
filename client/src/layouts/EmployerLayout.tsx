import { Outlet } from 'react-router-dom'
import { DashboardSidebar } from '@/components/layout/DashboardSidebar'
import { DashboardTopbar } from '@/components/layout/DashboardTopbar'
import { Toaster } from '@/components/ui/sonner'
import {
  LayoutDashboard,
  Briefcase,
  Users,
  Building2,
  Settings,
  PlusCircle,
} from 'lucide-react'

const navItems = [
  { to: '/employer/dashboard', label: 'Bảng điều khiển', icon: LayoutDashboard },
  { to: '/employer/jobs', label: 'Việc làm của tôi', icon: Briefcase },
  { to: '/employer/jobs/new', label: 'Đăng tin tuyển dụng', icon: PlusCircle },
  { to: '/employer/applications', label: 'Đơn ứng tuyển', icon: Users },
  { to: '/employer/company', label: 'Công ty', icon: Building2 },
  { to: '/employer/settings', label: 'Cài đặt', icon: Settings },
]

export default function EmployerLayout() {
  return (
    <div className="flex h-screen">
      <DashboardSidebar navItems={navItems} />
      <div className="flex flex-1 flex-col">
        <DashboardTopbar />
        <main className="flex-1 overflow-y-auto bg-gray-50 p-6">
          <Outlet />
        </main>
      </div>
      <Toaster richColors position="top-right" />
    </div>
  )
}
