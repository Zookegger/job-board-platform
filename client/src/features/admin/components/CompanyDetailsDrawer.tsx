import type { PendingCompany } from "@/api/admin";

type CompanyDetailsDrawerProps = {
  company: PendingCompany;
  isOpen: boolean;
  onClose: () => void;
  onApprove: () => void;
  onReject: () => void;
  onSuspend: () => void;
};

export default function CompanyDetailsDrawer({
  company,
  isOpen,
  onClose,
  onApprove,
  onReject,
  onSuspend,
}: CompanyDetailsDrawerProps) {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex justify-end bg-black/40">
      <div className="h-full w-full max-w-md bg-white p-6 shadow-lg">
        <div className="mb-4 flex items-center justify-between">
          <h2 className="text-xl font-bold">Chi tiết công ty</h2>
          <button type="button" onClick={onClose}>
            Đóng
          </button>
        </div>

        <div className="space-y-2 text-sm">
          <p><strong>Tên công ty:</strong> {company.companyName}</p>
          <p><strong>Email:</strong> {company.email}</p>
          <p><strong>Số điện thoại:</strong> {company.phone}</p>
          <p><strong>Mã số thuế:</strong> {company.taxCode}</p>
          <p><strong>Địa chỉ:</strong> {company.address}</p>
          <p><strong>Website:</strong> {company.website}</p>
          <p><strong>Mô tả:</strong> {company.description}</p>
          <p><strong>Ngày tạo:</strong> {company.createdAt}</p>
        </div>

        <div className="mt-6 flex flex-wrap gap-2">
          <button
            type="button"
            className="rounded bg-green-600 px-4 py-2 text-white"
            onClick={onApprove}
          >
            Duyệt
          </button>

          <button
            type="button"
            className="rounded bg-red-600 px-4 py-2 text-white"
            onClick={onReject}
          >
            Từ chối
          </button>

          <button
            type="button"
            className="rounded bg-yellow-500 px-4 py-2 text-white"
            onClick={onSuspend}
          >
            Tạm ngưng
          </button>
        </div>
      </div>
    </div>
  );
}