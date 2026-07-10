# Hướng dẫn Cài đặt & Chạy Dự án

## Yêu cầu Hệ thống

| Công cụ        | Phiên bản tối thiểu |
| -------------- | ------------------- |
| Java           | 21                  |
| Node.js        | 24                  |
| pnpm           | 9+                  |
| Docker         | 24+                 |
| Docker Compose | v2.24+              |

## Cấu trúc Thư mục

```
job-board-platform/
  server/              # Backend Spring Boot 4 + Java 21
  client/              # Frontend React 18 + Vite + TypeScript
  db/                  # Lược đồ DBML
  docker-compose.yml   # Orchestrator container
  .env.example         # Mẫu biến môi trường
```

## Biến môi trường

### File `.env` (gốc — dành cho Docker)

Sao chép từ `.env.example` và điều chỉnh:

```bash
cp .env.example .env
```

| Biến                        | Mô tả                         | Giá trị mặc định                           |
| --------------------------- | ----------------------------- | ------------------------------------------ |
| `POSTGRES_USER`             | User PostgreSQL               | `postgres`                                 |
| `POSTGRES_PASSWORD`         | Mật khẩu PostgreSQL           | `postgres`                                 |
| `POSTGRES_DB`               | Tên database                  | `job_board_db`                             |
| `DB_PORT`                   | Cổng database (host)          | `5432`                                     |
| `DB_URL`                    | JDBC URL (dùng nội bộ Docker) | `jdbc:postgresql://db:5432/job_board_db`   |
| `DB_USERNAME`               | Username kết nối DB           | `postgres`                                 |
| `DB_PASSWORD`               | Password kết nối DB           | `postgres`                                 |
| `SERVER_PORT`               | Cổng backend                  | `8080`                                     |
| `APP_JWT_SECRET`            | Secret key JWT                | (thay bằng chuỗi an toàn trong production) |
| `APP_JWT_EXPIRATION_MILLIS` | Thời hạn token JWT (ms)       | `3600000`                                  |

### File `client/.env` (dành cho Frontend)

| Biến           | Mô tả                   | Giá trị mặc định                                               |
| -------------- | ----------------------- | -------------------------------------------------------------- |
| `VITE_API_URL` | URL gốc của backend API | `http://localhost:5000` (mặc định từ `application.properties`) |

> **Lưu ý:** Khi chạy local (không Docker), backend ngầm định dùng cổng **5000**. Khi chạy qua Docker, biến `SERVER_PORT` trong `.env` gốc quyết định cổng backend.

## Cách chạy

### 1. Chạy bằng Docker (toàn bộ hệ thống)

```bash
# Tạo file .env từ mẫu
cp .env.example .env

# Khởi động tất cả services
docker compose up -d

# Kiểm tra trạng thái
docker compose ps
```

Sau đó truy cập:

- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI docs:** http://localhost:8080/api-docs

### 2. Chạy local (phát triển)

Khởi động database riêng, server và client chạy trực tiếp trên máy host.

#### Bước 1: Database

```bash
docker compose up db -d
```

#### Bước 2: Backend (Spring Boot)

```bash
cd server
./mvnw spring-boot:run
```

Backend chạy tại **http://localhost:5000** (mặc định).

> **Tuỳ chỉnh:** Truyền biến môi trường để ghi đè cấu hình, ví dụ:
>
> ```bash
> SERVER_PORT=8080 DB_URL=jdbc:postgresql://localhost:5433/job_board_db ./mvnw spring-boot:run
> ```

#### Bước 3: Frontend (React + Vite)

```bash
cd client
pnpm install
pnpm dev
```

Frontend chạy tại **http://localhost:5173**.

### 3. Các lệnh hữu ích

| Mục đích                    | Lệnh                          |
| --------------------------- | ----------------------------- |
| Dừng tất cả container       | `docker compose down`         |
| Xoá volume database         | `docker compose down -v`      |
| Kiểm tra biên dịch backend  | `cd server && ./mvnw compile` |
| Chạy test backend           | `cd server && ./mvnw test`    |
| Kiểm tra biên dịch frontend | `cd client && pnpm run build` |
| Chạy lint frontend          | `cd client && pnpm run lint`  |
| Chạy test frontend          | `cd client && pnpm run test`  |

## Luồng dữ liệu mặc định

```
Client (5173)  ──proxy──>  Server (5000)  ──JDBC──>  PostgreSQL (5432)
         └── docker: nginx (3000) ──> Server (8080)
```

Khi chạy bằng Docker, client được serve qua Nginx ở cổng 3000. Khi chạy local, Vite proxy các request `/api` và `/uploads` đến backend.
# Hướng dẫn Cài đặt & Chạy Dự án

## Yêu cầu Hệ thống

| Công cụ     | Phiên bản tối thiểu |
| ----------- | -------------------- |
| Java        | 21                   |
| Node.js     | 24                   |
| pnpm        | 9+                   |
| Docker      | 24+                  |
| Docker Compose | v2.24+            |

## Cấu trúc Thư mục

```
job-board-platform/
  server/              # Backend Spring Boot 4 + Java 21
  client/              # Frontend React 18 + Vite + TypeScript
  db/                  # Lược đồ DBML
  docker-compose.yml   # Orchestrator container
  .env.example         # Mẫu biến môi trường
```

## Biến môi trường

### File `.env` (gốc — dành cho Docker)

Sao chép từ `.env.example` và điều chỉnh:

```bash
cp .env.example .env
```

| Biến                     | Mô tả                          | Giá trị mặc định                          |
| ------------------------ | ------------------------------ | ----------------------------------------- |
| `POSTGRES_USER`          | User PostgreSQL                 | `postgres`                                |
| `POSTGRES_PASSWORD`      | Mật khẩu PostgreSQL             | `postgres`                                |
| `POSTGRES_DB`            | Tên database                    | `job_board_db`                            |
| `DB_PORT`               | Cổng database (host)            | `5432`                                    |
| `DB_URL`                | JDBC URL (dùng nội bộ Docker)   | `jdbc:postgresql://db:5432/job_board_db`  |
| `DB_USERNAME`           | Username kết nối DB             | `postgres`                                |
| `DB_PASSWORD`           | Password kết nối DB             | `postgres`                                |
| `SERVER_PORT`           | Cổng backend                    | `8080`                                    |
| `APP_JWT_SECRET`        | Secret key JWT                  | (thay bằng chuỗi an toàn trong production)|
| `APP_JWT_EXPIRATION_MILLIS` | Thời hạn token JWT (ms)     | `3600000`                                 |

### File `client/.env` (dành cho Frontend)

| Biến            | Mô tả                  | Giá trị mặc định                 |
| --------------- | ---------------------- | -------------------------------- |
| `VITE_API_URL`  | URL gốc của backend API | `http://localhost:5000` (mặc định từ `application.properties`) |

> **Lưu ý:** Khi chạy local (không Docker), backend ngầm định dùng cổng **5000**. Khi chạy qua Docker, biến `SERVER_PORT` trong `.env` gốc quyết định cổng backend.

## Cách chạy

### 1. Chạy bằng Docker (toàn bộ hệ thống)

```bash
# Tạo file .env từ mẫu
cp .env.example .env

# Khởi động tất cả services
docker compose up -d

# Kiểm tra trạng thái
docker compose ps
```

Sau đó truy cập:
- **Frontend:** http://localhost:3000
- **Backend API:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui.html
- **OpenAPI docs:** http://localhost:8080/api-docs

### 2. Chạy local (phát triển)

Khởi động database riêng, server và client chạy trực tiếp trên máy host.

#### Bước 1: Database

```bash
docker compose up db -d
```

#### Bước 2: Backend (Spring Boot)

```bash
cd server
./mvnw spring-boot:run
```

Backend chạy tại **http://localhost:5000** (mặc định).

> **Tuỳ chỉnh:** Truyền biến môi trường để ghi đè cấu hình, ví dụ:
> ```bash
> SERVER_PORT=8080 DB_URL=jdbc:postgresql://localhost:5433/job_board_db ./mvnw spring-boot:run
> ```

#### Bước 3: Frontend (React + Vite)

```bash
cd client
pnpm install
pnpm dev
```

Frontend chạy tại **http://localhost:5173**.

### 3. Các lệnh hữu ích

| Mục đích                       | Lệnh                                            |
| ------------------------------ | ----------------------------------------------- |
| Dừng tất cả container          | `docker compose down`                           |
| Xoá volume database            | `docker compose down -v`                        |
| Kiểm tra biên dịch backend     | `cd server && ./mvnw compile`                   |
| Chạy test backend              | `cd server && ./mvnw test`                      |
| Kiểm tra biên dịch frontend    | `cd client && pnpm run build`                   |
| Chạy lint frontend             | `cd client && pnpm run lint`                    |
| Chạy test frontend             | `cd client && pnpm run test`                    |

## Luồng dữ liệu mặc định

```
Client (5173)  ──proxy──>  Server (5000)  ──JDBC──>  PostgreSQL (5432)
         └── docker: nginx (3000) ──> Server (8080)
```

Khi chạy bằng Docker, client được serve qua Nginx ở cổng 3000. Khi chạy local, Vite proxy các request `/api` và `/uploads` đến backend.
