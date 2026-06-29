package com.yoedu.job_board_platform.mappers;

import com.yoedu.job_board_platform.dtos.notification.NotificationResponse;
import com.yoedu.job_board_platform.models.Notification;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface NotificationMapper {
	List<NotificationResponse> toResponseList(List<Notification> notifications);

	@AfterMapping
	default NotificationResponse toResponse(Notification n) {
		return new NotificationResponse(
				n.getId(),
				n.getType(),
				n.getEntityId(),
				n.getMessage(),
				n.getCreatedAt(),
				n.getReadAt(),
				n.getReadAt() != null
		);
	}
}
