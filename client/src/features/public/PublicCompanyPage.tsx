import { useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";

import {
  getPublicCompanyDetail,
  getPublicCompanyJobs,
} from "@/api/publicCompany";

import type {
  PublicCompany,
  PublicCompanyJob,
} from "@/api/publicCompany";

import "./PublicCompanyPage.css";

const PublicCompanyPage = () => {
  const { companyId } = useParams<{ companyId: string }>();

  const [company, setCompany] = useState<PublicCompany | null>(null);
  const [jobs, setJobs] = useState<PublicCompanyJob[]>([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  useEffect(() => {
    if (!companyId) return;

    const fetchCompanyData = async () => {
      try {
        setLoading(true);
        setError("");

        const [companyData, jobsData] = await Promise.all([
          getPublicCompanyDetail(companyId),
          getPublicCompanyJobs(companyId, page, 6),
        ]);

        setCompany(companyData);
        setJobs(jobsData.content || []);
        setTotalPages(jobsData.totalPages || 0);
      } catch (err) {
        console.error(err);
        setError("Không thể tải thông tin công ty.");
      } finally {
        setLoading(false);
      }
    };

    fetchCompanyData();
  }, [companyId, page]);

  const formatDate = (date?: string) => {
    if (!date) return "";
    return new Date(date).toLocaleDateString("vi-VN");
  };

  const renderStatus = (status?: string) => {
    if (status === "ACTIVE") return "Đang tuyển";
    if (status === "EXPIRED") return "Đã hết hạn";
    if (status === "PENDING_APPROVAL") return "Chờ duyệt";
    if (status === "DRAFT") return "Bản nháp";
    if (status === "REJECTED") return "Bị từ chối";
    return status || "Không rõ";
  };

  if (loading) {
    return (
      <div className="public-company-state">
        Đang tải thông tin công ty...
      </div>
    );
  }

  if (error || !company) {
    return (
      <div className="public-company-state public-company-error">
        {error || "Không tìm thấy công ty."}
      </div>
    );
  }

  return (
    <div className="public-company-page">
      <section className="company-hero">
        <div className="company-hero-overlay">
          <div className="company-hero-content">
            <div className="company-logo">
              {company.logoUrl ? (
                <img src={company.logoUrl} alt={company.companyName} />
              ) : (
                <span>{company.companyName.charAt(0).toUpperCase()}</span>
              )}
            </div>

            <div>
              <h1>{company.companyName}</h1>
              <p>{company.address || "Chưa cập nhật địa chỉ"}</p>

              <div className="company-badges">
                <span>{company.totalOpenJobs} việc đang tuyển</span>
                {company.website && <span>Có website</span>}
              </div>
            </div>
          </div>
        </div>
      </section>

      <main className="company-container">
        <section className="company-main">
          <div className="company-card">
            <h2>Giới thiệu công ty</h2>
            <p>
              {company.description ||
                "Công ty hiện chưa cập nhật thông tin giới thiệu."}
            </p>
          </div>

          <div className="company-card">
            <div className="section-title-row">
              <h2>Việc làm đang tuyển</h2>
              <span>{jobs.length} tin tuyển dụng</span>
            </div>

            {jobs.length === 0 ? (
              <div className="empty-box">
                Công ty hiện chưa có tin tuyển dụng công khai.
              </div>
            ) : (
              <div className="job-list">
                {jobs.map((job) => (
                  <div className="job-card" key={job.id}>
                    <div className="job-content">
                      <h3>{job.title}</h3>

                      <div className="job-meta">
                        {job.location && <span>{job.location}</span>}
                        <span>{renderStatus(job.status)}</span>
                        {job.createdAt && (
                          <span>Đăng ngày {formatDate(job.createdAt)}</span>
                        )}
                      </div>
                    </div>

                    <Link to={`/jobs/${job.id}`} className="job-detail-btn">
                      Xem chi tiết
                    </Link>
                  </div>
                ))}
              </div>
            )}

            {totalPages > 1 && (
              <div className="pagination">
                <button
                  disabled={page === 0}
                  onClick={() => setPage((prev) => prev - 1)}
                >
                  Trước
                </button>

                <span>
                  Trang {page + 1} / {totalPages}
                </span>

                <button
                  disabled={page + 1 >= totalPages}
                  onClick={() => setPage((prev) => prev + 1)}
                >
                  Sau
                </button>
              </div>
            )}
          </div>
        </section>

        <aside className="company-sidebar">
          <div className="company-card">
            <h2>Thông tin liên hệ</h2>

            <div className="contact-list">
              <div>
                <strong>Email</strong>
                <p>{company.email || "Chưa cập nhật"}</p>
              </div>

              <div>
                <strong>Số điện thoại</strong>
                <p>{company.phone || "Chưa cập nhật"}</p>
              </div>

              <div>
                <strong>Website</strong>
                {company.website ? (
                  <p>
                    <a href={company.website} target="_blank" rel="noreferrer">
                      {company.website}
                    </a>
                  </p>
                ) : (
                  <p>Chưa cập nhật</p>
                )}
              </div>

              <div>
                <strong>Địa chỉ</strong>
                <p>{company.address || "Chưa cập nhật"}</p>
              </div>

              <div>
                <strong>Mã số thuế</strong>
                <p>{company.taxCode || "Chưa cập nhật"}</p>
              </div>
            </div>
          </div>
        </aside>
      </main>
    </div>
  );
};

export default PublicCompanyPage;