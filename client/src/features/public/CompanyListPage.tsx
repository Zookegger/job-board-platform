import { useDeferredValue, useState } from "react";

import { CompanyCard, CompanyCardSkeleton } from "@/components/shared/CompanyCard";
import { Input } from "@/components/ui/input";
import { usePublicCompanies } from "@/hooks/usePublicCompany";
import { Search, X } from "lucide-react";

export default function CompanyListPage() {
	const [page, setPage] = useState(0);
	const [searchInput, setSearchInput] = useState("");
	const deferredKeyword = useDeferredValue(searchInput.trim());

	const { data: companiesPage, isLoading, isFetching } = usePublicCompanies({ keyword: deferredKeyword, page, size: 12 });

	const companies = companiesPage?.content || [];
	const totalPages = companiesPage?.totalPages || 0;

	function handleClear() {
		setSearchInput("");
		setPage(0);
	}

	return (
		<div className='min-h-screen bg-gray-50 text-gray-800'>
			<section className='bg-linear-to-br from-blue-600 to-gray-900 px-5 py-12 text-white'>
				<div className='mx-auto max-w-280 text-center'>
					<h1 className='mb-3 text-3xl font-bold'>Danh sách công ty</h1>
					<p className='mb-6 text-gray-200'>Khám phá các công ty đang tuyển dụng trên nền tảng</p>
					<div className='mx-auto max-w-xl'>
						<Input
							value={searchInput}
							onChange={(e) => {
								setSearchInput(e.target.value);
								setPage(0);
							}}
							placeholder='Tìm kiếm công ty...'
							startIcon={<Search className='size-4' />}
							endIcon={
								searchInput ? (
									<button
										onClick={handleClear}
										className='-mr-1 cursor-pointer rounded-sm p-0.5 text-gray-400 hover:text-gray-600'
										aria-label='Xoá tìm kiếm'
									>
										<X className='size-4' />
									</button>
								) : undefined
							}
							className='border-0 bg-white/95 text-gray-900 placeholder:text-gray-400 focus-within:ring-2 focus-within:ring-white/50 h-10'
						/>
					</div>
				</div>
			</section>

			<main className='mx-auto max-w-280 px-5 py-8'>
				{isFetching && !isLoading && companies.length > 0 && (
					<p className='mb-4 text-sm text-gray-400'>Đang tìm kiếm...</p>
				)}

				{isLoading ? (
					<div className='grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3'>
						{Array.from({ length: 6 }).map((_, i) => (
							<CompanyCardSkeleton key={i} />
						))}
					</div>
				) : companies.length === 0 ? (
					<div className='rounded-2xl bg-white py-20 text-center text-gray-400 shadow-sm'>
						Không tìm thấy công ty nào.
					</div>
				) : (
					<>
						<div className='grid grid-cols-1 gap-6 md:grid-cols-2 xl:grid-cols-3'>
							{companies.map((company) => (
								<CompanyCard
									key={company.slug}
									company={company}
								/>
							))}
						</div>

						{totalPages > 1 && (
							<div className='mt-8 flex items-center justify-center gap-4'>
								<button
									disabled={page === 0}
									onClick={() => setPage((p) => p - 1)}
									className='rounded-lg bg-blue-600 px-3.5 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300'
								>
									Trước
								</button>
								<span className='text-sm text-gray-600'>
									Trang {page + 1} / {totalPages}
								</span>
								<button
									disabled={page + 1 >= totalPages}
									onClick={() => setPage((p) => p + 1)}
									className='rounded-lg bg-blue-600 px-3.5 py-2 text-sm font-medium text-white hover:bg-blue-700 disabled:cursor-not-allowed disabled:bg-gray-300'
								>
									Sau
								</button>
							</div>
						)}
					</>
				)}
			</main>
		</div>
	);
}
