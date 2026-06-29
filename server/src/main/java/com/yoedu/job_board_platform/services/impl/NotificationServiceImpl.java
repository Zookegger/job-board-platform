package com.yoedu.job_board_platform.services.impl;

import com.yoedu.job_board_platform.models.Notification;
import com.yoedu.job_board_platform.models.NotificationStatus;
import com.yoedu.job_board_platform.models.User;
import com.yoedu.job_board_platform.repositories.NotificationRepository;
import com.yoedu.job_board_platform.repositories.UserRepository;
import com.yoedu.job_board_platform.services.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Service để gửi thông báo tới người dùng.
 * Xử lý thông báo về thay đổi trạng thái công ty.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
	private final NotificationRepository notificationRepository;
	private final UserRepository userRepository;

	/**
	 * Tạo một thông báo mới cho người dùng.
	 *
	 * @param userId   ID của người dùng nhận thông báo
	 * @param type     Loại thông báo (ví dụ: SYSTEM, TRANSACTION, ALERT)
	 * @param entityId ID của đối tượng liên quan trực tiếp đến thông báo (nếu có)
	 * @param message  Nội dung chi tiết của thông báo
	 */
	@Override
	public void createNotification(UUID userId, NotificationStatus type, UUID entityId, String message) {
		User user = userRepository.getReferenceById(userId);

		Notification notification = Notification.builder().user(user).type(type).entityId(entityId).message(message).build();

		notificationRepository.save(notification);
		log.info("Created {} notification for user {}", type, userId);
	}

	/**
	 * Lấy danh sách thông báo của một người dùng theo dạng phân trang.
	 *
	 * @param userId   ID của người dùng cần lấy thông báo
	 * @param pageable Cấu hình phân trang và sắp xếp (page, size, sort)
	 *
	 * @return Một đối tượng {@link Page} chứa danh sách các thông báo tìm được
	 */
	@Override
	public Page<Notification> getNotifications(UUID userId, Pageable pageable) {
		return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
	}

	/**
	 * Đếm tổng số lượng thông báo chưa đọc của một người dùng.
	 *
	 * @param userId ID của người dùng cần kiểm tra
	 *
	 * @return Số lượng thông báo chưa đọc (kiểu long)
	 */
	@Override
	public long getUnreadCount(UUID userId) {
		return notificationRepository.countByUserIdAndReadAtIsNull(userId);
	}

	/**
	 * Đánh dấu một thông báo cụ thể là đã đọc bởi người dùng.
	 *
	 * @param userId         ID của người dùng sở hữu thông báo
	 * @param notificationId ID của thông báo cần đánh dấu
	 */
	@Override
	public void markAsRead(UUID userId, UUID notificationId) {
		OffsetDateTime now = OffsetDateTime.now();

		notificationRepository.findById(notificationId).ifPresent(notification -> {
			if (notification.getReadAt() != null) return;

			if (notification.getUser().getId().equals(userId)) {
				notification.setReadAt(now);
			}
		});
	}

	/**
	 * Đánh dấu tất cả thông báo của một người dùng là đã đọc.
	 *
	 * @param userId ID của người dùng cần cập nhật
	 */
	@Override
	@Transactional
	public void markAllAsRead(UUID userId) {
		OffsetDateTime now = OffsetDateTime.now();
		int updatedCount = notificationRepository.markAllAsRead(userId, now);

		log.info("Đã đánh dấu {} thông báo là đã đọc cho user {}", updatedCount, userId);
	}
}
