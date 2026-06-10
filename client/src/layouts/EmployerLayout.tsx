import { DashboardSidebar } from "@/components/layout/DashboardSidebar";
import { Toaster } from "@/components/ui/sonner";
import { Briefcase, Building2, LayoutDashboard, PlusCircle, Settings, Users } from "lucide-react";
import { Outlet } from "react-router-dom";

const navItems = [
	{ to: "/employer/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
	{ to: "/employer/jobs", label: "Việc làm của tôi", icon: Briefcase },
	{ to: "/employer/jobs/new", label: "Đăng tin tuyển dụng", icon: PlusCircle },
	{ to: "/employer/applications", label: "Đơn ứng tuyển", icon: Users },
	{ to: "/employer/company", label: "Công ty", icon: Building2 },
	{ to: "/employer/settings", label: "Cài đặt", icon: Settings },
];

export default function EmployerLayout() {
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
