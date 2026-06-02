package com.yoedu.job_board_platform.models;

/**
 * Trạng thái của một job posting, giúp quản lý vòng đời của bài đăng công việc.
 * - <strong>Draft:</strong> Bản nháp, chưa gửi duyệt hoặc chưa công khai. Người
 * dùng có thể chỉnh sửa và lưu lại.
 * - <strong>Pending Approval:</strong> Đang chờ phê duyệt. Bài đăng đã được gửi
 * đi và đang chờ quản trị viên xem xét. Trong trạng thái này, bài đăng không
 * hiển thị với ứng viên.
 * - <strong>Active:</strong> Đang hoạt động, hiển thị cho ứng viên. Bài đăng đã
 * được phê duyệt và có thể nhận đơn ứng tuyển.
 * - <strong>Expired:</strong> Đã hết hạn đăng tuyển. Bài đăng đã hết hạn và
 * không còn hiển thị với ứng viên, nhưng vẫn có thể được quản trị viên xem lại.
 * - <strong>Rejected:</strong> Bị từ chối phê duyệt. Bài đăng đã được gửi đi
 * nhưng bị quản trị viên từ chối. Người dùng có thể chỉnh sửa và gửi lại để phê
 * duyệt.
 */
public enum JobStatus {
    DRAFT,
    PENDING_APPROVAL,
    ACTIVE,
    EXPIRED,
    REJECTED
}
