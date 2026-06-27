import type { AdminPendingCompanyResponse } from "@/types/company";
type CompanyTableProps = {
	companies: AdminPendingCompanyResponse[];
	currentPage: number;
	totalPages: number;
	onPageChange: (page: number) => void;
	onViewDetails: (company: AdminPendingCompanyResponse) => void;
};

export default function CompanyTable({
	companies,
	currentPage,
	totalPages,
	onPageChange,
	onViewDetails,
}: CompanyTableProps) {
	return (
		<div className='space-y-4'>
			<div className='overflow-x-auto rounded border bg-white'>
				<table className='w-full border-collapse text-sm'>
					<thead>
						<tr className='border-b bg-gray-100'>
							<th className='p-3 text-left'>Tên công ty</th>
							<th className='p-3 text-left'>Email</th>
							<th className='p-3 text-left'>Số điện thoại</th>
							<th className='p-3 text-left'>Mã số thuế</th>
							<th className='p-3 text-left'>Thao tác</th>
						</tr>
					</thead>

					<tbody>
						{companies.map((company) => (
							<tr
								key={company.id}
								className='border-b'
							>
								<td className='p-3'>{company.companyName}</td>
								<td className='p-3'>{company.email}</td>
								<td className='p-3'>{company.phone}</td>
								<td className='p-3'>{company.taxCode}</td>
								<td className='p-3'>
									<button
										type='button'
										className='rounded bg-blue-600 px-3 py-1 text-white'
										onClick={() => onViewDetails(company)}
									>
										Xem chi tiết
									</button>
								</td>
							</tr>
						))}
					</tbody>
				</table>
			</div>

			<div className='flex items-center justify-end gap-2'>
				<button
					type='button'
					className='rounded border px-3 py-1 disabled:opacity-50'
					disabled={currentPage <= 0}
					onClick={() => onPageChange(currentPage - 1)}
				>
					Trước
				</button>

				<span className='text-sm'>
					Trang {currentPage + 1} / {totalPages}
				</span>

				<button
					type='button'
					className='rounded border px-3 py-1 disabled:opacity-50'
					disabled={currentPage >= totalPages - 1}
					onClick={() => onPageChange(currentPage + 1)}
				>
					Sau
				</button>
			</div>
		</div>
	);
}
