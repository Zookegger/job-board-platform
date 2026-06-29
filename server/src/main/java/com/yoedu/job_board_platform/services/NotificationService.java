package com.yoedu.job_board_platform.services;

import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

/**
 * Service quản lý logic nghiệp vụ liên quan đến thông báo (Notification) trong hệ thống.
 */
public interface NotificationService {

    /**
     * Tạo một thông báo mới cho người dùng.
     *
     * @param userId    ID của người dùng nhận thông báo
     * @param type      Loại thông báo (ví dụ: SYSTEM, TRANSACTION, ALERT)
     * @param entityId  ID của đối tượng liên quan trực tiếp đến thông báo (nếu có)
     * @param message   Nội dung chi tiết của thông báo
     */
    void createNotification(UUID userId, NotificationStatus type, UUID entityId, String message);

    /**
     * Lấy danh sách thông báo của một người dùng theo dạng phân trang.
     *
     * @param userId   ID của người dùng cần lấy thông báo
     * @param pageable Cấu hình phân trang và sắp xếp (page, size, sort)
     * @return Một đối tượng {@link Page} chứa danh sách các thông báo tìm được
     */
    Page<Notification> getNotifications(UUID userId, Pageable pageable);

    /**
     * Đếm tổng số lượng thông báo chưa đọc của một người dùng.
     *
     * @param userId ID của người dùng cần kiểm tra
     * @return Số lượng thông báo chưa đọc (kiểu long)
     */
    long getUnreadCount(UUID userId);

    /**
     * Đánh dấu một thông báo cụ thể là đã đọc bởi người dùng.
     *
     * @param userId         ID của người dùng sở hữu thông báo
     * @param notificationId ID của thông báo cần đánh dấu
     */
    void markAsRead(UUID userId, UUID notificationId);

    /**
     * Đánh dấu tất cả thông báo của một người dùng là đã đọc.
     *
     * @param userId ID của người dùng cần cập nhật
     */
    void markAllAsRead(UUID userId);
}