package com.yoedu.job_board_platform.models;

/**
 * Danh sách các loại hình công việc, giúp phân loại và hiển thị thông tin về loại hình làm việc của vị trí công việc.
 * - <strong>Full-time</strong>: Làm việc toàn thời gian, thường yêu cầu làm việc 40 giờ mỗi tuần. Ứng viên sẽ có lịch làm việc cố định và thường được hưởng đầy đủ các quyền lợi như bảo hiểm, nghỉ phép, v.v.
 * - <strong>Part-time</strong>: Làm việc bán thời gian, thường yêu cầu làm việc ít hơn 40 giờ mỗi tuần. Ứng viên có thể có lịch làm việc linh hoạt và thường không được hưởng đầy đủ các quyền lợi như nhân viên toàn thời gian.
 * - <strong>Contract</strong>: Làm việc theo hợp đồng, thường có thời hạn cụ thể và không phải là nhân viên chính thức của công ty. Ứng viên sẽ làm việc theo dự án hoặc nhiệm vụ cụ thể và thường không được hưởng các quyền lợi như nhân viên toàn thời gian.
 */
public enum EmploymentType {
    FULL_TIME,
    PART_TIME,
    CONTRACT,
}
