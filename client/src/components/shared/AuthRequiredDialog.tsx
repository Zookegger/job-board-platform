import RouterRoutes from "@/utils/RouterRoutes";
import { LogIn, UserPlus } from "lucide-react";
import { useNavigate } from "react-router-dom";
import { BaseDialog } from "./BaseDialog";

export interface AuthRequiredDialogProps {
	isOpen: boolean;
	onClose: () => void;
	message?: string;
}

export function AuthRequiredDialog({ isOpen, onClose, message }: AuthRequiredDialogProps) {
	const navigate = useNavigate();

	const handleAction = (route: string) => {
		onClose();
		navigate(route);
	};

	return (
		<BaseDialog
			isOpen={isOpen}
			onClose={onClose}
			title='Yêu cầu đăng nhập'
			description={message ?? "Bạn cần có tài khoản để thực hiện thao tác này."}
			size='md'
		>
			<div className='grid grid-cols-2 gap-4 pb-4 h-48 sm:h-56'>
				<button
					onClick={() => handleAction(RouterRoutes.LOGIN)}
					className='group relative flex flex-col items-center justify-center gap-3 overflow-hidden rounded-xl border-2 border-slate-200 bg-slate-50 transition-all duration-75 hover:scale-[1.03] hover:border-blue-500 hover:bg-blue-50/50 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-500 focus:ring-offset-2'
				>
					<LogIn className='h-10 w-10 text-slate-400 transition-colors duration-75 group-hover:text-blue-500' />
					<div className='flex flex-col items-center'>
						<span className='text-lg font-bold tracking-wide text-slate-700 transition-colors group-hover:text-blue-700'>
							Đăng nhập
						</span>
						<span className='text-xs font-medium text-slate-500 opacity-70 group-hover:opacity-100'>
							Tôi đã có tài khoản
						</span>
					</div>
				</button>

				<button
					onClick={() => handleAction(RouterRoutes.REGISTER)}
					className='group relative flex flex-col items-center justify-center gap-3 overflow-hidden rounded-xl border-2 border-slate-200 bg-slate-50 transition-all duration-75 hover:scale-[1.03] hover:border-orange-500 hover:bg-orange-50/50 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:ring-offset-2'
				>
					<UserPlus className='h-10 w-10 text-slate-400 transition-colors duration-75 group-hover:text-orange-500' />
					<div className='flex flex-col items-center'>
						<span className='text-lg font-bold tracking-wide text-slate-700 transition-colors group-hover:text-orange-700'>
							Đăng ký
						</span>
						<span className='text-xs font-medium text-slate-500 opacity-70 group-hover:opacity-100'>
							Tham gia ngay
						</span>
					</div>
				</button>
			</div>
		</BaseDialog>
	);
}
