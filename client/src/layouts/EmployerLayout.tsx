import { DashboardSidebar } from "@/components/layout/DashboardSidebar";
import { Toaster } from "@/components/ui/sonner";
import { Briefcase, Building2, LayoutDashboard, Menu, PlusCircle, Settings, Users } from "lucide-react";
import { useState } from "react";
import { Outlet } from "react-router-dom";
import { Button } from "@/components/ui/button";

const navItems = [
	{ to: "/employer/dashboard", label: "Bảng điều khiển", icon: LayoutDashboard },
	{ to: "/employer/jobs", label: "Việc làm của tôi", icon: Briefcase },
	{ to: "/employer/jobs/new", label: "Đăng tin tuyển dụng", icon: PlusCircle },
	{ to: "/employer/applications", label: "Đơn ứng tuyển", icon: Users },
	{ to: "/employer/company", label: "Công ty", icon: Building2 },
	{ to: "/employer/settings", label: "Cài đặt", icon: Settings },
];

export default function EmployerLayout() {
	const [sidebarOpen, setSidebarOpen] = useState(false);

	return (
		<div className='flex h-screen'>
			<DashboardSidebar
				navItems={navItems}
				isOpen={sidebarOpen}
				onClose={() => setSidebarOpen(false)}
			/>
			<div className='flex flex-1 flex-col'>
				<header className='flex lg:hidden h-16 items-center justify-between border-b bg-card px-4'>
					<Button
						variant='ghost'
						size='icon'
						onClick={() => setSidebarOpen(true)}
					>
						<Menu className='h-5 w-5' />
					</Button>
					<span className='text-lg font-bold text-primary'>JobBoard</span>
					<div className='w-9' />
				</header>
				<main className='flex-1 overflow-y-auto bg-gray-50 p-4 md:p-6'>
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
