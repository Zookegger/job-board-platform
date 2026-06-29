package com.yoedu.job_board_platform.events.listeners;

import com.yoedu.job_board_platform.events.ApplicationStatusChangeEvent;
import com.yoedu.job_board_platform.events.CompanyStatusChangeEvent;
import com.yoedu.job_board_platform.events.JobStatusChangeEvent;
import com.yoedu.job_board_platform.models.*;
import com.yoedu.job_board_platform.repositories.CompanyEmployerDetailRepository;
import com.yoedu.job_board_platform.services.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class NotificationEventListener {
	private final NotificationService notificationService;
	private final CompanyEmployerDetailRepository companyEmployerDetailRepository;

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleApplicationStatusChange(ApplicationStatusChangeEvent event) {
		Application application = event.application();
		List<CompanyEmployerDetail> employerDetails = companyEmployerDetailRepository.findAllByCompanyId(application.getJob().getCompany().getId());

		String message = event.newStatus().equals(ApplicationStatus.PENDING) ? "Ứng viên mới đã nộp đơn cho vị trí " + application.getJob().getTitle() : "Đơn ứng tuyển của bạn cho vị trí " + application.getJob().getTitle() + " đã được cập nhật sang trạng thái " + event.newStatus();

		if (event.newStatus().equals(ApplicationStatus.PENDING)) {
			employerDetails.forEach(employerDetail -> {
				notificationService.createNotification(employerDetail.getProfile().getUser().getId(), NotificationStatus.APPLICATION_STATUS_CHANGED, application.getId(), message);
			});
		} else {
			notificationService.createNotification(application.getCandidate().getUser().getId(), NotificationStatus.APPLICATION_STATUS_CHANGED, application.getId(), message);
		}
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleCompanyStatusChange(CompanyStatusChangeEvent event) {
		Company company = event.company();
		List<CompanyEmployerDetail> employerDetails = companyEmployerDetailRepository.findAllByCompanyId(company.getId());

		String message = switch (event.newStatus()) {
			case APPROVED -> "Hồ sơ công ty đã được duyệt. Bạn có thể đăng tuyển ngay.";
			case SUSPENDED -> "Công ty bị tạm ngưng hoạt động. Vui lòng liên hệ hỗ trợ.";
			case PENDING -> "Hồ sơ công ty đang chờ kiểm duyệt.";
			case REJECTED -> "Hồ sơ công ty bị từ chối. Vui lòng cập nhật lại thông tin.";
			default -> String.format("Trạng thái công ty cập nhật thành: %s", event.newStatus().name());
		};

		employerDetails.forEach(employerDetail -> {
			notificationService.createNotification(employerDetail.getProfile().getUser().getId(), NotificationStatus.COMPANY_STATUS_CHANGED, company.getId(), message);
		});
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handleJobStatusChange(JobStatusChangeEvent event) {
		Job job = event.job();
		List<CompanyEmployerDetail> employerDetails = companyEmployerDetailRepository.findAllByCompanyId(job.getCompany().getId());

		String message = switch (event.newStatus()) {
			case JobStatus.ACTIVE -> "Tin tuyển dụng " + job.getTitle() + " đã được phê duyệt và hiển thị công khai.";
			case JobStatus.REJECTED -> "Tin tuyển dụng " + job.getTitle() + " đã bị từ chối. Lý do: " + job.getRejectionReason();
			default -> "Trạng thái tin tuyển dụng đã thay đổi.";
		};

		employerDetails.forEach(employerDetail -> {
			notificationService.createNotification(
					employerDetail.getProfile().getUser().getId(),
					NotificationStatus.JOB_STATUS_CHANGED,
					job.getId(),
					message
			);
		});
	}
}
