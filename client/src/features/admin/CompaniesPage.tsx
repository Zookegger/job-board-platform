'use client';

import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import { getPendingCompanies } from '@/api/admin';
import type { PendingCompany } from '@/api/admin';
import { Card } from '@/components/ui/card';
import CompanyTable from './components/CompanyTable';
import CompanyDetailsDrawer from './components/CompanyDetailsDrawer';
import CompanyApprovalModal from './components/CompanyApprovalModal';
import { Tabs, TabsContent, TabsList, TabsTrigger } from '@/components/ui/tabs';
import { Skeleton } from '@/components/ui/skeleton';

type ModalAction = 'approve' | 'reject' | 'suspend' | null;

export default function AdminCompaniesPage() {
  const [currentPage, setCurrentPage] = useState(0);
  const [pageSize, setPageSize] = useState(20);
  const [selectedCompany, setSelectedCompany] = useState<PendingCompany | null>(null);
  const [showDetailsDrawer, setShowDetailsDrawer] = useState(false);
  const [modalAction, setModalAction] = useState<ModalAction>(null);
  const [showModal, setShowModal] = useState(false);

  // Fetch pending companies
  const { data, isLoading, error, refetch } = useQuery({
    queryKey: ['pendingCompanies', currentPage, pageSize],
    queryFn: () => getPendingCompanies(currentPage, pageSize),
  });

  const handleViewDetails = (company: PendingCompany) => {
    setSelectedCompany(company);
    setShowDetailsDrawer(true);
  };

  const handleAction = (action: ModalAction) => {
    setModalAction(action);
    setShowModal(true);
  };

  const handleCloseModal = () => {
    setShowModal(false);
    setModalAction(null);
  };

  const handleActionSuccess = () => {
    handleCloseModal();
    setShowDetailsDrawer(false);
    refetch();
  };

  const companies = data?.data?.content || [];
  const totalPages = data?.data?.totalPages || 1;
  const totalElements = data?.data?.totalElements || 0;

  return (
    <div className="min-h-[80vh] flex flex-col gap-6 p-6 bg-gray-50">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Quản lý Phê duyệt Công ty</h1>
        <p className="text-gray-600 mt-2">Quản lý, phê duyệt, từ chối và tạm ngưng công ty đăng ký</p>
      </div>

      {/* Stats */}
      <div className="grid grid-cols-1 md:grid-cols-4 gap-4">
        <StatsCard label="Chờ Duyệt" value={totalElements} variant="pending" />
        <StatsCard label="Đã Duyệt" value={0} variant="approved" />
        <StatsCard label="Từ Chối" value={0} variant="rejected" />
        <StatsCard label="Tạm Ngưng" value={0} variant="suspended" />
      </div>

      {/* Main Content */}
      <Card className="flex-1 p-6 shadow-sm">
        <Tabs defaultValue="pending" className="w-full">
          <TabsList className="grid w-full grid-cols-1 mb-4">
            <TabsTrigger value="pending">
              Công ty Chờ Duyệt ({totalElements})
            </TabsTrigger>
          </TabsList>

          <TabsContent value="pending" className="mt-0">
            {isLoading ? (
              <div className="space-y-4">
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
                <Skeleton className="h-12 w-full" />
              </div>
            ) : error ? (
              <div className="text-center py-12">
                <p className="text-red-600">Lỗi khi tải dữ liệu: {(error as Error).message}</p>
              </div>
            ) : companies.length === 0 ? (
              <div className="text-center py-12">
                <p className="text-gray-500">Không có công ty nào chờ duyệt</p>
              </div>
            ) : (
              <CompanyTable
                companies={companies}
                currentPage={currentPage}
                totalPages={totalPages}
                onPageChange={setCurrentPage}
                onViewDetails={handleViewDetails}
              />
            )}
          </TabsContent>
        </Tabs>
      </Card>

      {/* Details Drawer */}
      {selectedCompany && (
        <CompanyDetailsDrawer
          company={selectedCompany}
          isOpen={showDetailsDrawer}
          onClose={() => setShowDetailsDrawer(false)}
          onApprove={() => handleAction('approve')}
          onReject={() => handleAction('reject')}
          onSuspend={() => handleAction('suspend')}
        />
      )}

      {/* Action Modal */}
      {selectedCompany && modalAction && (
        <CompanyApprovalModal
          company={selectedCompany}
          action={modalAction}
          isOpen={showModal}
          onClose={handleCloseModal}
          onSuccess={handleActionSuccess}
        />
      )}
    </div>
  );
}

interface StatsCardProps {
  label: string;
  value: number;
  variant: 'pending' | 'approved' | 'rejected' | 'suspended';
}

function StatsCard({ label, value, variant }: StatsCardProps) {
  const colors = {
    pending: 'bg-blue-50 text-blue-900 border-blue-200',
    approved: 'bg-green-50 text-green-900 border-green-200',
    rejected: 'bg-red-50 text-red-900 border-red-200',
    suspended: 'bg-yellow-50 text-yellow-900 border-yellow-200',
  };

  return (
    <Card className={`p-4 border ${colors[variant]}`}>
      <p className="text-sm font-medium opacity-75">{label}</p>
      <p className="text-2xl font-bold mt-1">{value}</p>
    </Card>
  );
}

