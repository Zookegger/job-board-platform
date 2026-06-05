package com.yoedu.job_board_platform.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.yoedu.job_board_platform.config.ApiPaths;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping(ApiPaths.BASE + "/jobs")
@PreAuthorize("hasRole('EMPLOYER')")
@Tag(name = "Nhà tuyển dụng — Quản lý tin", description = "CRUD tin tuyển dụng, quản lý trạng thái, thông tin công ty, dashboard. Yêu cầu role EMPLOYER.")
public class EmployerJobController {

    @GetMapping
    @Operation(summary = "Danh sách tin của công ty", description = """
            Lấy danh sách tin tuyển dụng thuộc về công ty của employer đang đăng nhập.
            Có thể lọc theo trạng thái (DRAFT, PENDING_APPROVAL, ACTIVE, EXPIRED, REJECTED).
            Kết quả phân trang, sắp xếp theo ngày tạo mới nhất.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Danh sách tin tuyển dụng của công ty (có phân trang)", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập (chỉ EMPLOYER)", content = @Content)
    })
    public ResponseEntity<?> getMyJobs(
            @Parameter(description = "Lọc theo trạng thái: DRAFT, PENDING_APPROVAL, ACTIVE, EXPIRED, REJECTED", example = "ACTIVE")
            @RequestParam(required = false) String status,
            @Parameter(description = "Số trang (bắt đầu từ 0)", example = "0")
            @RequestParam(defaultValue = "0") int page
    ) {
        return ResponseEntity.ok("Danh sách tin");
    }

    @PostMapping
    @Operation(summary = "Đăng tin tuyển dụng mới", description = """
            Tạo một tin tuyển dụng mới cho công ty của employer.
            Tin sẽ ở trạng thái DRAFT ban đầu, sau đó cần gửi duyệt để chờ admin phê duyệt.
            Yêu cầu body chứa đầy đủ thông tin: tiêu đề, mô tả, yêu cầu, mức lương, địa điểm, loại hình...
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Tạo tin tuyển dụng thành công (trạng thái DRAFT)", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ (thiếu trường bắt buộc, sai định dạng)", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền truy cập", content = @Content)
    })
    public ResponseEntity<?> createJob() {
        return ResponseEntity.ok("Tạo tin thành công");
    }

    @GetMapping("/{id}")
    @Operation(summary = "Chi tiết tin (có thể chỉnh sửa)", description = """
            Lấy chi tiết tin tuyển dụng của công ty mình để xem và chỉnh sửa.
            Trả về toàn bộ thông tin bao gồm cả các trường admin không thấy được.
            Chỉ trả về nếu tin thuộc về công ty của employer hiện tại.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Chi tiết tin tuyển dụng (đầy đủ)", content = @Content),
        @ApiResponse(responseCode = "403", description = "Tin không thuộc về công ty của bạn", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
    })
    public ResponseEntity<?> getJobDetail(
            @Parameter(description = "ID của tin tuyển dụng", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Chi tiết tin");
    }

    @PutMapping("/{id}")
    @Operation(summary = "Cập nhật tin tuyển dụng", description = """
            Cập nhật thông tin tin tuyển dụng đã đăng. 
            Chỉ cập nhật được các tin thuộc về công ty của mình.
            Nếu tin đang ở trạng thái ACTIVE, sau khi cập nhật có thể cần duyệt lại.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật tin tuyển dụng thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu cập nhật không hợp lệ", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền sửa tin này", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
    })
    public ResponseEntity<?> updateJob(
            @Parameter(description = "ID của tin tuyển dụng cần cập nhật", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Cập nhật thành công");
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Xóa tin tuyển dụng", description = """
            Xóa vĩnh viễn một tin tuyển dụng của công ty mình.
            Hành động này không thể hoàn tác — tất cả đơn ứng tuyển liên quan cũng sẽ bị xóa.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Xóa tin tuyển dụng thành công", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền xóa tin này", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
    })
    public ResponseEntity<?> deleteJob(
            @Parameter(description = "ID của tin tuyển dụng cần xóa", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Xóa thành công");
    }

    @PostMapping("/{id}/toggle-status")
    @Operation(summary = "Bật/tắt trạng thái tuyển dụng", description = """
            Chuyển đổi trạng thái tuyển dụng giữa ACTIVE và CLOSED.
            Khi CLOSED, tin không còn hiển thị với ứng viên và không nhận thêm đơn ứng tuyển mới.
            Khi ACTIVE, tin được hiển thị trở lại.
            """)
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thay đổi trạng thái thành công", content = @Content),
        @ApiResponse(responseCode = "403", description = "Không có quyền thay đổi tin này", content = @Content),
        @ApiResponse(responseCode = "404", description = "Không tìm thấy tin tuyển dụng", content = @Content)
    })
    public ResponseEntity<?> toggleJobStatus(
            @Parameter(description = "ID của tin tuyển dụng", example = "1", required = true)
            @PathVariable Long id
    ) {
        return ResponseEntity.ok("Thay đổi trạng thái thành công");
    }

    @GetMapping("/my-company")
    @Operation(summary = "Thông tin công ty của tôi", description = "Lấy thông tin chi tiết của công ty mà employer đang quản lý: tên, địa chỉ, mô tả, website, logo, email, số điện thoại.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Thông tin công ty của employer hiện tại", content = @Content),
        @ApiResponse(responseCode = "404", description = "Chưa có thông tin công ty", content = @Content)
    })
    public ResponseEntity<?> getMyCompany() {
        return ResponseEntity.ok("Thông tin công ty");
    }

    @PutMapping("/my-company")
    @Operation(summary = "Cập nhật thông tin công ty", description = "Cập nhật thông tin công ty: tên, địa chỉ, mô tả, website, logo, email, số điện thoại. Công ty sẽ cần được admin duyệt lại nếu thay đổi thông tin.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Cập nhật thông tin công ty thành công", content = @Content),
        @ApiResponse(responseCode = "400", description = "Dữ liệu không hợp lệ", content = @Content)
    })
    public ResponseEntity<?> updateMyCompany() {
        return ResponseEntity.ok("Cập nhật công ty thành công");
    }

    @GetMapping("/employer-dashboard")
    @Operation(summary = "Dashboard nhà tuyển dụng", description = """
            Thống kê tổng quan cho nhà tuyển dụng: tổng số tin đã đăng, số tin đang Active,
            tổng số ứng viên đã nộp, số ứng viên mới trong tuần, số ứng viên theo từng trạng thái.
            """)
    @ApiResponse(responseCode = "200", description = "Dữ liệu thống kê dashboard", content = @Content)
    public ResponseEntity<?> getEmployerDashboard() {
        return ResponseEntity.ok("Dashboard thống kê");
    }
}
