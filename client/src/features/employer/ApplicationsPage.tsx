import CandidateTable from "./components/CandidateTable";

export default function EmployerApplicationsPage() {
	return (
		<div className='mx-auto flex w-full max-w-7xl flex-col gap-6 p-4 md:p-6'>
			<div className='flex flex-col gap-1'>
				<h1 className='text-3xl font-bold tracking-tight text-foreground'>Quản lý ứng viên</h1>
				<p className='text-sm font-medium text-muted-foreground'>
					Xem và cập nhật trạng thái hồ sơ ứng tuyển của các ứng viên
				</p>
			</div>
			<CandidateTable />
		</div>
	);
}

