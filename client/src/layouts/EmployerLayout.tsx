import { DashboardSidebar } from "@/components/layout/DashboardSidebar";
import { Button } from "@/components/ui/button";
import { Toaster } from "@/components/ui/sonner";
import RouterRoutes from "@/utils/RouterRoutes";
import { Briefcase, Building2, LayoutDashboard, Menu } from "lucide-react";
import { useState } from "react";
import { Outlet } from "react-router-dom";

const navItems = [
	{ to: RouterRoutes.EMPLOYER_DASHBOARD, label: "Bảng điều khiển", icon: LayoutDashboard },
	{ to: RouterRoutes.EMPLOYER_JOBS, label: "Việc làm của tôi", icon: Briefcase },
	{ to: RouterRoutes.EMPLOYER_COMPANY, label: "Công ty", icon: Building2 },
];

export default function EmployerLayout() {
	const [sidebarOpen, setSidebarOpen] = useState(false);

	return (
		<div className='flex h-dvh w-dvw'>
			<DashboardSidebar
				navItems={navItems}
				isOpen={sidebarOpen}
				onClose={() => setSidebarOpen(false)}
			/>
			<div className='flex flex-1 flex-col w-dvw'>
				<header className='grid lg:hidden h-16 grid-cols-[1fr_auto_1fr] items-center border-b bg-card px-4'>
					<Button
						variant='ghost'
						size='icon'
						className='justify-self-start'
						onClick={() => setSidebarOpen(true)}
					>
						<Menu className='h-5 w-5' />
					</Button>
					<span className='text-center text-lg font-bold text-primary'>JobBoard</span>
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
