import { DashboardSidebar } from "@/components/layout/DashboardSidebar";
import { Toaster } from "@/components/ui/sonner";
import { BarChart3, Briefcase, Building2, LayoutDashboard, Settings, Users } from "lucide-react";
import { Outlet } from "react-router-dom";

const navItems = [
	{ to: "/admin/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
	{ to: "/admin/users", label: "Người dùng", icon: Users },
	{ to: "/admin/companies", label: "Công ty", icon: Building2 },
	{ to: "/admin/jobs", label: "Việc làm", icon: Briefcase },
	{ to: "/admin/reports", label: "Báo cáo", icon: BarChart3 },
	{ to: "/admin/settings", label: "Cài đặt", icon: Settings },
];

export default function AdminLayout() {
	return (
		<div className='flex h-screen'>
			<DashboardSidebar navItems={navItems} />
			<div className='flex flex-1 flex-col'>
				<main className='flex-1 overflow-y-auto bg-gray-50 p-6'>
					<Outlet />
				</main>
			</div>
			<Toaster
				richColors
				position='top-right'
			/>
		</div>
	);
}
