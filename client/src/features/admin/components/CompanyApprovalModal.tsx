import { useState } from "react";
import {
  approveCompany,
  rejectCompany,
  suspendCompany,
} from "@/api/admin";
import type { PendingCompany } from "@/api/admin";

type ModalAction = "approve" | "reject" | "suspend";

type CompanyApprovalModalProps = {
  company: PendingCompany;
  action: ModalAction;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
};

export default function CompanyApprovalModal({
  company,
  action,
  isOpen,
  onClose,
  onSuccess,
}: CompanyApprovalModalProps) {
  const [reason, setReason] = useState("");
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const title =
    action === "approve"
      ? "Duyệt công ty"
      : action === "reject"
        ? "Từ chối công ty"
        : "Tạm ngưng công ty";

  const handleConfirm = async () => {
    try {
      setLoading(true);

      if (action === "approve") {
        await approveCompany(company.id);
      }

      if (action === "reject") {
        await rejectCompany(company.id, {
          rejectionReason: reason,
        });
      }

      if (action === "suspend") {
        await suspendCompany(company.id, {
          suspensionReason: reason,
        });
      }

      onSuccess();
    } catch (error) {
      console.error(error);
      alert("Có lỗi xảy ra khi xử lý công ty");
    } finally {
      setLoading(false);
    }
  };

  const needReason = action === "reject" || action === "suspend";

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50">
      <div className="w-full max-w-md rounded bg-white p-6 shadow-lg">
        <h2 className="mb-4 text-xl font-bold">{title}</h2>

        <p className="mb-4 text-sm">
          Công ty: <strong>{company.companyName}</strong>
        </p>

        {needReason && (
          <textarea
            className="mb-4 w-full rounded border p-2"
            rows={4}
            placeholder="Nhập lý do..."
            value={reason}
            onChange={(e) => setReason(e.target.value)}
          />
        )}

        <div className="flex justify-end gap-2">
          <button
            type="button"
            className="rounded border px-4 py-2"
            onClick={onClose}
            disabled={loading}
          >
            Hủy
          </button>

          <button
            type="button"
            className="rounded bg-blue-600 px-4 py-2 text-white disabled:opacity-50"
            onClick={handleConfirm}
            disabled={loading || (needReason && !reason.trim())}
          >
            {loading ? "Đang xử lý..." : "Xác nhận"}
          </button>
        </div>
      </div>
    </div>
  );
}