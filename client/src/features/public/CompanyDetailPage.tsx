import { useState } from "react";
import { useParams } from "react-router-dom";

import { JobCard } from "@/components/shared/JobCard";
import { usePublicCompany, usePublicCompanyJobs } from "@/hooks/usePublicCompany";

export default function CompanyDetailPage() {
	const { slug } = useParams<{ slug: string }>();
	const [page, setPage] = useState(0);
	const pageSize = 6;

	const { data: company, isLoading: companyLoading, error: companyError } = usePublicCompany(slug || "");
	const { data: jobsPage, isLoading: jobsLoading } = usePublicCompanyJobs(slug || "", page, pageSize);

	const jobs = jobsPage?.content || [];
	const totalPages = jobsPage?.totalPages || 0;

	if (companyLoading) {
		return (
			<div className='flex min-h-[70vh] items-center justify-center text-lg text-gray-500'>
				Đang tải thông tin công ty...
			</div>
		);
	}

	if (companyError || !company) {
		return (
			<div className='flex min-h-[70vh] items-center justify-center text-lg text-red-600'>
				{(companyError as Error)?.message || "Không tìm thấy công ty."}
			</div>
		);
	}

	return (
		<div className='min-h-screen bg-gray-50 text-gray-800'>
			{/* Hero */}
			<section className='relative h-65 bg-linear-to-br from-blue-600 to-gray-900'>
				<div className='flex h-full items-end'>
					<div className='mx-auto flex w-full max-w-280 items-center gap-6 px-5 pb-8.5 pt-10 text-white'>
						<div className='flex h-27.5 w-27.5 shrink-0 items-center justify-center overflow-hidden rounded-2xl bg-white text-4xl font-bold text-blue-600 shadow-xl'>
							{company.logoUrl ? (
								<img
									src={company.logoUrl}
									alt={company.name}
									className='h-full w-full object-cover'
								/>
							) : (
								<span>{company.name.charAt(0).toUpperCase()}</span>
							)}
						</div>
						<div>
							<h1 className='mb-2 text-3xl font-bold'>{company.name}</h1>
							<p className='mb-3 text-gray-200'>{company.address || "Chưa cập nhật địa chỉ"}</p>
							<div className='flex flex-wrap gap-2.5'>
								<span className='rounded-full bg-white/18 px-3 py-1.5 text-sm'>
									{company.totalOpenJobs} việc đang tuyển
								</span>
								{company.categories && company.categories.length > 0 && (
									<span className='rounded-full bg-white/18 px-3 py-1.5 text-sm'>
										{company.categories.map((c) => c.name).join(", ")}
									</span>
								)}
								{company.website && (
									<span className='rounded-full bg-white/18 px-3 py-1.5 text-sm'>Có website</span>
								)}
							</div>
						</div>
					</div>
				</div>
			</section>

			{/* Content */}
			<main className='mx-auto grid w-full max-w-280 grid-cols-1 gap-6 px-5 pb-12 pt-7 lg:grid-cols-[1fr_340px]'>
				{/* Left column */}
				<section className='space-y-6'>
					{/* Description */}
					<div className='rounded-2xl bg-white p-6 shadow-sm'>
						<h2 className='mb-4 text-xl font-bold text-gray-900'>Giới thiệu công ty</h2>
						<p className='leading-relaxed text-gray-600'>
							{company.description || "Công ty hiện chưa cập nhật thông tin giới thiệu."}
						</p>
					</div>

					{/* Jobs */}
					<div className='rounded-2xl bg-white p-6 shadow-sm'>
						<div className='mb-5 flex items-center justify-between'>
							<h2 className='text-xl font-bold text-gray-900'>Việc làm đang tuyển</h2>
							<span className='text-sm text-gray-500'>{jobs.length} tin tuyển dụng</span>
						</div>

						{jobsLoading ? (
							<div className='py-8 text-center text-gray-400'>Đang tải...</div>
						) : jobs.length === 0 ? (
							<div className='rounded-xl bg-gray-50 py-8 text-center text-gray-400'>
								Công ty hiện chưa có tin tuyển dụng công khai.
							</div>
						) : (
							<div className='space-y-3.5'>
								{jobs.map((job) => (
									<JobCard
										key={job.id}
										job={job}
									/>
								))}
							</div>
						)}

						{totalPages > 1 && (
							<div className='mt-6 flex items-center justify-center gap-4'>
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
					</div>
				</section>

				{/* Sidebar */}
				<aside className='space-y-6'>
					<div className='rounded-2xl bg-white p-6 shadow-sm'>
						<h2 className='mb-4 text-xl font-bold text-gray-900'>Thông tin liên hệ</h2>
						<div className='space-y-4'>
							<div>
								<strong className='mb-1 block text-sm text-gray-900'>Email</strong>
								<p className='break-all text-sm text-gray-600'>{company.email || "Chưa cập nhật"}</p>
							</div>
							<div>
								<strong className='mb-1 block text-sm text-gray-900'>Số điện thoại</strong>
								<p className='text-sm text-gray-600'>{company.phone || "Chưa cập nhật"}</p>
							</div>
							<div>
								<strong className='mb-1 block text-sm text-gray-900'>Website</strong>
								{company.website ? (
									<a
										href={company.website}
										target='_blank'
										rel='noreferrer'
										className='break-all text-sm text-blue-600 hover:underline'
									>
										{company.website}
									</a>
								) : (
									<p className='text-sm text-gray-600'>Chưa cập nhật</p>
								)}
							</div>
							<div>
								<strong className='mb-1 block text-sm text-gray-900'>Địa chỉ</strong>
								<p className='text-sm text-gray-600'>{company.address || "Chưa cập nhật"}</p>
							</div>
							<div>
								<strong className='mb-1 block text-sm text-gray-900'>Mã số thuế</strong>
								<p className='text-sm text-gray-600'>{company.taxCode || "Chưa cập nhật"}</p>
							</div>
						</div>
					</div>
				</aside>
			</main>
		</div>
	);
}
