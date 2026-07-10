# Hướng dẫn chạy Integration Tests — Job Board Platform

## Yêu cầu
- Server Spring Boot đang chạy: `cd server && mvn spring-boot:run`
- PostgreSQL đang chạy, database `job_board_db` đã được migrate
- Postman Desktop (không dùng Web)

---

## 1. Import vào Postman

### Environment
**Import → chọn file:**
```
postman/local.postman_environment.json
```
> Sau khi import, chọn **Local** làm environment active (góc trên phải Postman).

### Collections
**Import → chọn tất cả 6 file:**
```
postman/collections/Flow1_Auth.postman_collection.json
postman/collections/Flow2_Employer.postman_collection.json
postman/collections/Flow3_Candidate.postman_collection.json
postman/collections/Flow4a_Admin_Approve.postman_collection.json
postman/collections/Flow4b_Employer_Admin.postman_collection.json
postman/collections/Flow5_Report.postman_collection.json
```

---

## 2. Thứ tự chạy

**Bắt buộc chạy đúng thứ tự sau:**

| Bước | Collection | Mục đích |
|------|-----------|---------|
| 1 | Flow 1 — Authentication | Đăng ký, đăng nhập 3 vai trò |
| 2 | Flow 2 — Employer | Tạo & submit job |
| 3 | **Flow 4a — Admin Approve** | Admin duyệt công ty + job |
| 4 | Flow 3 — Candidate | Tìm việc, ứng tuyển |
| 5 | Flow 4b — Employer & Admin | Employer review + Admin quản lý |
| 6 | Flow 5 — Report | Báo cáo vi phạm |

> **Quan trọng:** Không đảo thứ tự. Flow 4a phải chạy trước Flow 3.

---

## 3. Cách chạy từng collection

1. Click tên collection trong sidebar
2. Click nút **▶ Run**
3. Đảm bảo Environment chọn **Local**
4. Click **Run [Tên collection]**
5. Chờ kết quả

---

## 4. Danh sách 20 Test Cases

| TC | Collection | Mô tả | Expected |
|----|-----------|-------|---------|
| TC-01 | Flow 1 | Candidate đăng ký tài khoản | 201/409 |
| TC-02 | Flow 1 | Employer đăng ký tài khoản + công ty | 201/409 |
| TC-03 | Flow 1 | Đăng nhập tài khoản hợp lệ (3 vai trò) | 200 |
| TC-04 | Flow 1 | Xem thông tin user hiện tại (/auth/me) | 200 |
| TC-05 | Flow 1 | Làm mới access token (refresh token) | 200 |
| TC-06 | Flow 2 | Employer tạo tin tuyển dụng (DRAFT) | 201 |
| TC-07 | Flow 2 | Employer cập nhật tin tuyển dụng | 200 |
| TC-08 | Flow 2 | Employer gửi tin để admin duyệt (PENDING) | 200 |
| TC-09 | Flow 3 | Tìm kiếm job công khai theo từ khóa | 200 |
| TC-10 | Flow 3 | Xem chi tiết tin tuyển dụng | 200 |
| TC-11 | Flow 3 | Candidate nộp đơn ứng tuyển | 201 |
| TC-12 | Flow 3 | Xem danh sách đơn đã nộp | 200 |
| TC-13 | Flow 3 | Xem timeline trạng thái hồ sơ | 200 |
| TC-14 | Flow 4a | Admin xem thống kê dashboard | 200 |
| TC-15 | Flow 4a | Admin phê duyệt công ty mới | 200 |
| TC-16 | Flow 4a | Admin phê duyệt tin tuyển dụng | 200 |
| TC-17 | Flow 4b | Employer xem & cập nhật trạng thái ứng viên | 200 |
| TC-18 | Flow 4b | Admin xem danh sách tài khoản | 200 |
| TC-19 | Flow 4b | Admin tạm ngưng & khôi phục tài khoản | 200 |
| TC-20 | Flow 5 | Candidate báo cáo vi phạm, Admin xử lý | 200 |

---

## 5. Lưu ý quan trọng

**Lỗi 403 trên admin/employer requests:**
> Server đọc JWT từ **cookie**, không phải `Authorization` header. Mỗi lần chạy collection, bước đăng nhập đầu tiên sẽ tự set cookie đúng.

**Chạy lại từ đầu (reset):**
Nếu muốn chạy lại toàn bộ, cần reset dữ liệu test trong DB:
```sql
DELETE FROM applications WHERE id IN (
  SELECT a.id FROM applications a 
  JOIN candidate_details cd ON a.candidate_id = cd.profile_id
  JOIN users u ON cd.profile_id = u.id 
  WHERE u.email = 'testcandidate@flow1.com'
);
DELETE FROM jobs WHERE title = 'Lead Java Developer' 
  AND status != 'ACTIVE' 
  OR (status = 'ACTIVE' AND created_at > NOW() - INTERVAL '1 day');
```
Hoặc đơn giản: tạo job mới (Flow 2) và cập nhật `jobId` trong Environment.

**jobId phải là job của testemployer:**
Biến `jobId` phải trỏ đến job được tạo bởi `testemployer@flow1.com`. Kiểm tra bằng:
```
GET {{baseUrl}}/employer/jobs  (dùng employerAccessToken)
```
