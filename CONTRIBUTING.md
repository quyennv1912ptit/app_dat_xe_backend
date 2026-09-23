# 🛠 Quy Trình Quản Lý Source Code (Git Workflow)

Dự án áp dụng mô hình **Git Flow**. Để tránh conflict và mất code, toàn bộ team tuân thủ nghiêm ngặt các quy tắc dưới đây.

---

## 1. Cấu Trúc Nhánh (7 Nhánh Cố Định)

| Nhánh | Vai trò |
| --- | --- |
| `main` | Môi trường Production, chỉ chứa code đã test kỹ và hoàn thiện |
| `develop` | Nhánh tích hợp chung của cả team. Mọi tính năng sau khi làm xong sẽ được gộp vào đây |
| `feat/auth` | Module Security & User (Người 1) |
| `feat/booking-core` | Module Booking & State Machine (Người 2A) |
| `feat/matching` | Module Matching & Concurrency (Người 2B) |
| `feat/realtime` | Module WebSocket & Redis Location (Người 3) |
| `feat/payment` | Module Payment & Rating (Người 4) |

> ⚠️ **Lưu ý tối thượng:** Tuyệt đối không commit hay `git push` trực tiếp lên `main` và `develop`. Mọi người chỉ làm việc trên nhánh `feat/` của mình.

---

## 2. Luồng Làm Việc Hàng Ngày

### Bước 1 — Bắt đầu ngày mới (đồng bộ code từ team)

Luôn cập nhật code mới nhất từ nhánh `develop` về máy trước khi bắt đầu code thêm tính năng mới.

```bash
git checkout develop
git pull origin develop
```

### Bước 2 — Chuyển sang nhánh làm việc cá nhân

```bash
# Nếu chưa có nhánh ở local (chỉ gõ 1 lần lúc mới nhận việc):
git checkout -b feat/<tên-nhánh-của-bạn>

# Từ những ngày sau, chỉ cần chuyển nhánh:
git checkout feat/<tên-nhánh-của-bạn>
```

### Bước 3 — Lưu lại tiến độ (Commit)

Nên commit thường xuyên sau mỗi cụm chức năng nhỏ hoàn thành.

```bash
git add .
git commit -m "feat: Thêm API tạo cuốc xe"
```

Tiền tố commit chuẩn:

| Tiền tố | Ý nghĩa |
| --- | --- |
| `feat` | Thêm mới tính năng |
| `fix` | Sửa lỗi |
| `refactor` | Tối ưu / dọn dẹp code, không đổi hành vi |

### Bước 4 — Cập nhật code lên GitHub (Push)

Nhớ kỹ: đang đứng ở nhánh `feat/...` thì chỉ push lên chính nhánh đó.

```bash
git push origin feat/<tên-nhánh-của-bạn>
```

### Bước 5 — Tạo Pull Request (PR) để gộp code

1. Lên trang GitHub của project.
2. Bạn sẽ thấy nút **Compare & pull request** màu xanh báo hiệu nhánh của bạn vừa có thay đổi — click vào đó.
3. Chọn **base branch** là `develop` và **compare branch** là nhánh `feat/` của bạn.
4. Bấm **Create pull request**.
5. Gắn thẻ (Assign) một người khác hoặc Leader vào review code. Người đó sẽ kiểm tra và bấm **Merge pull request** để gộp code của bạn vào `develop`.

---

## 3. Cách Tránh Xung Đột Code (Conflict)

Trước khi push code lên GitHub ở Bước 4, hãy làm thêm thao tác sau để đảm bảo code của bạn không đè lên code của người khác vừa merge vào `develop`:

```bash
# Đang đứng ở nhánh feat/... của bạn
git pull origin develop
```

Lệnh này sẽ lấy code mới nhất từ `develop` và tự động trộn vào nhánh của bạn.

Nếu có conflict, IDE (IntelliJ / VS Code) sẽ hiện đỏ các file bị trùng — bạn chỉ cần mở file ra, chọn phần code cần giữ lại, sau đó:

```bash
git add .
git commit
```

là xong.

## feat/auth (Người 1): Security & User

### 1. Database Entities

| Entity | Các trường chính (Fields) |
| --- | --- |
| **User** | `id`, `phone` (unique), `email`, `passwordHash`, `fullName`, `avatarUrl`, `role` (CUSTOMER/DRIVER/ADMIN), `status` (ACTIVE/LOCKED/PENDING) |
| **DriverProfile** | `userId`, `licenseNumber`, `licenseClass` (B1/B2/C...), `licenseExpiry`, `yearsExperience`, `verificationStatus` (PENDING/APPROVED/REJECTED), `rejectReason` |
| **DriverDocument** | `id`, `driverId`, `type` (LICENSE_FRONT/LICENSE_BACK/CCCD/PORTRAIT), `fileUrl` |
| **CustomerVehicle** | `id`, `ownerId`, `plate`, `brand`, `model`, `color`, `transmission` (MANUAL/AUTO), `seats`, `isDefault` |
| **SavedAddress** | `id`, `userId`, `label` (Nhà/Công ty), `address`, `lat`, `lng` |
| **RefreshToken** | `id`, `userId`, `tokenHash`, `deviceId`, `expiresAt`, `revoked` |
| **OtpCode** | `id`, `phone`, `codeHash`, `purpose`, `expiresAt`, `attempts` |
| **DeviceToken** | `id`, `userId`, `fcmToken`, `platform` |

### 2. REST APIs

| HTTP Method | Route | Mô tả nghiệp vụ |
| --- | --- | --- |
| **POST** | `/auth/register` | Đăng ký tài khoản (Customer/Driver) |
| **POST** | `/auth/otp/send`, `/auth/otp/verify` | Gửi và xác thực mã OTP |
| **POST** | `/auth/login`, `/auth/refresh`, `/auth/logout` | Đăng nhập, làm mới Access Token, đăng xuất |
| **POST** | `/auth/password/forgot`, `/auth/password/reset` | Quên và đặt lại mật khẩu |
| **GET / PUT** | `/users/me` | Xem và sửa hồ sơ cá nhân |
| **PUT** | `/users/me/password` | Đổi mật khẩu |
| **CRUD** | `/users/me/vehicles` | Quản lý danh sách xe của khách hàng |
| **CRUD** | `/users/me/addresses` | Quản lý các địa chỉ đã lưu (Nhà, Công ty) |
| **POST** | `/users/me/device-token` | Đăng ký Push Notification Token (FCM) |
| **GET / PUT** | `/drivers/me/profile` | Quản lý hồ sơ tài xế |
| **POST** | `/drivers/me/documents` | Upload hình ảnh giấy tờ (Bằng lái, CCCD) |
| **GET** | `/admin/drivers?status=PENDING` | Lấy danh sách tài xế đang chờ duyệt |
| **PATCH** | `/admin/drivers/{id}/verify` | Admin duyệt hoặc từ chối hồ sơ tài xế |
| **PATCH** | `/admin/users/{id}/status` | Admin khóa hoặc mở tài khoản người dùng |

> **📌 Trách nhiệm chung cho team:** Người 1 đảm nhiệm viết bộ lọc JWT Filter, cấu hình `@PreAuthorize` phân quyền theo Role, và xây dựng cơ chế `@CurrentUser` resolver.

---

## feat/booking-core (Người 2A): Booking & State Machine

### 1. Luồng trạng thái chuyến đi (State Machine Flow)

`PENDING` ➔ `SEARCHING` ➔ `DRIVER_ASSIGNED` ➔ `DRIVER_ARRIVING` ➔ `DRIVER_ARRIVED` ➔ `VEHICLE_HANDOVER` (Bàn giao xe lúc đón) ➔ `IN_PROGRESS` ➔ `ARRIVED_DESTINATION` ➔ `VEHICLE_RETURNED` (Bàn giao xe lúc trả) ➔ `COMPLETED`
*(Nhánh phụ: `CANCELLED`, `NO_DRIVER_FOUND`, `EXPIRED`)*

### 2. Database Entities

| Entity | Các trường chính (Fields) |
| --- | --- |
| **Booking** | `id`, `code`, `customerId`, `driverId` (nullable), `vehicleId`, `type` (INSTANT/SCHEDULED), `scheduledAt`, `pickupAddress`, `pickupLat`, `pickupLng`, `dropoffAddress`, `dropoffLat`, `dropoffLng`, `distanceKm`, `estimatedFare`, `finalFare`, `status`, `cancelReason`, `cancelledBy`, `@Version` |
| **BookingStatusHistory** | `id`, `bookingId`, `fromStatus`, `toStatus`, `actorId`, `note`, `createdAt` |
| **PricingRule** | `id`, `baseFare`, `perKm`, `perMinuteWaiting`, `nightSurchargePercent`, `nightStart/End`, `returnFeeForDriver`, `minFare`, `active` |
| **VehicleHandoverReport** | `id`, `bookingId`, `phase` (PICKUP/DROPOFF), `odometer`, `fuelLevel`, `notes`, `confirmedByCustomer` |
| **HandoverPhoto** | `id`, `reportId`, `angle` (FRONT/BACK/LEFT/RIGHT/DASHBOARD), `fileUrl` |

### 3. REST APIs

| HTTP Method | Route | Mô tả nghiệp vụ |
| --- | --- | --- |
| **POST** | `/bookings/estimate` | Ước tính giá cước dựa trên tọa độ đón/đến và thời gian |
| **POST** | `/bookings` | Tạo cuốc xe mới, phát sự kiện `BookingCreated` |
| **GET** | `/bookings/{id}` | Lấy chi tiết chuyến đi |
| **GET** | `/bookings?status=&page=` | Xem lịch sử chuyến đi của người dùng |
| **POST** | `/bookings/{id}/cancel` | Hủy chuyến (kèm lý do) |
| **POST** | `/bookings/{id}/arrived` | Tài xế báo đã di chuyển đến điểm đón khách |
| **POST** | `/bookings/{id}/handover/pickup` | Tài xế chụp ảnh 4 góc xe, đồng hồ km lúc nhận xe |
| **POST** | `/bookings/{id}/handover/pickup/confirm` | Khách hàng xác nhận biên bản bàn giao xe đầu đi |
| **POST** | `/bookings/{id}/start` | Bắt đầu hành trình chạy xe |
| **POST** | `/bookings/{id}/arrive-destination` | Tài xế báo đã lái xe đến đích |
| **POST** | `/bookings/{id}/handover/dropoff` | Bàn giao lại xe, chụp ảnh đối chứng lúc trả xe |
| **POST** | `/bookings/{id}/complete` | Hoàn tất cuốc xe, phát sự kiện `TripCompleted` |
| **GET/POST/PUT** | `/admin/pricing-rules` | Admin thiết lập và quản lý cấu hình bảng giá |
| **GET** | `/admin/bookings` | Admin tra cứu toàn bộ cuốc xe hệ thống |

> **📌 Trách nhiệm chung cho team:** Cung cấp service `BookingStateMachine.transition(bookingId, event, actor)` làm điểm duy nhất để thay đổi trạng thái cuốc xe. Các module khác bắt buộc gọi hàm này thay vì tự `save()` trạng thái vào database.

---

## feat/matching (Người 2B): Matching & Concurrency

### 1. Database Entities

| Entity | Các trường chính (Fields) |
| --- | --- |
| **BookingOffer** | `id`, `bookingId`, `driverId`, `status` (PENDING/ACCEPTED/REJECTED/EXPIRED/CANCELLED), `round`, `distanceToPickupKm`, `sentAt`, `expiresAt` |
| **DriverAvailability** | `driverId`, `status` (OFFLINE/ONLINE/BUSY), `lastOnlineAt`, `currentBookingId` |
| **DriverMatchStats** | `driverId`, `offersReceived`, `offersAccepted`, `acceptanceRate`, `cancelCount` (Chỉ số dùng để xếp hạng phát cuốc) |
| **MatchingAttempt** | `id`, `bookingId`, `round`, `radiusKm`, `candidateCount`, `result` (Log lưu lại quá trình ghép cuốc để debug) |

### 2. REST APIs

| HTTP Method | Route | Mô tả nghiệp vụ |
| --- | --- | --- |
| **PATCH** | `/drivers/me/availability` | Bật (Online) hoặc tắt (Offline) trạng thái sẵn sàng nhận chuyến |
| **GET** | `/drivers/me/offers` | Lấy danh sách các offer đang chờ (Fallback nếu WebSocket rớt mạng) |
| **POST** | `/offers/{id}/accept` | Tài xế bấm nhận chuyến |
| **POST** | `/offers/{id}/reject` | Tài xế chủ động từ chối cuốc |
| **POST** | `/admin/bookings/{id}/assign` | Admin điều phối gán tài xế thủ công |
| **GET** | `/admin/matching/attempts` | Xem lịch sử quá trình hệ thống tự động ghép cuốc |

### 3. Logic lõi cần xử lý

* **Tìm tài xế lân cận:** Lắng nghe sự kiện `BookingCreated`, gọi hàm `LocationService.findNearby(lat, lng, radius)` của Người 3 để lấy danh sách tài xế gần.
* **Điều kiện lọc (Filter):** Tài xế phải ở trạng thái `APPROVED`, `ONLINE`, và bằng lái phải phù hợp với hộp số (Số sàn/Tự động) của xe khách hàng.
* **Thuật toán phát cuốc:** Xếp hạng tài xế ưu tiên theo khoảng cách và tỷ lệ nhận chuyến (`acceptanceRate`). Bắn offer theo từng vòng (mở rộng dần bán kính, thời gian chờ timeout 15–20 giây mỗi vòng).
* **Khóa chống xung đột (Race Condition):** Đảm bảo xử lý nguyên tử (atomic) khi 2 người cùng bấm nhận bằng câu lệnh SQL: `UPDATE booking SET driver_id=?, status='DRIVER_ASSIGNED' WHERE id=? AND status='SEARCHING'`. Có thể bổ sung Redis Distributed Lock (`SET NX`) theo `bookingId`.
* **Xử lý hậu kỳ:** Sau khi 1 tài xế nhận thành công, phát sự kiện `DriverAssigned`, tự động hủy các offer còn lại của cuốc đó. Nếu chạy hết các vòng không ai nhận, chuyển booking sang `NO_DRIVER_FOUND`.

---

## feat/realtime (Người 3): WebSocket & Redis Location

### 1. Redis Caching & Database Entities

| Thành phần | Cấu trúc dữ liệu / Các trường chính |
| --- | --- |
| **Redis GEO** | `drivers:online` (member = driverId). Dùng `GEOSEARCH` tìm tài xế gần. |
| **Redis Hash** | `driver:{id}:loc` (lat, lng, heading, speed, updatedAt). Set TTL tự hủy 30s. |
| **Redis Stream/List** | `trip:{bookingId}:track` (Lưu danh sách tọa độ GPS suốt hành trình). |
| **TripRoutePoint** | Entity DB: `id`, `bookingId`, `lat`, `lng`, `recordedAt`. (Ghi batch từ Redis xuống PostgreSQL khi kết thúc chuyến để tính khoảng cách thực). |
| **Notification** | Entity DB: `id`, `userId`, `type`, `title`, `body`, `data` (JSON), `isRead`, `createdAt` |
| **ChatMessage** | Entity DB: `id`, `bookingId`, `senderId`, `content`, `createdAt` (Nếu làm tính năng chat) |

### 2. WebSocket Channels (STOMP)

| Hướng luồng | Endpoint / Topic | Mục đích |
| --- | --- | --- |
| **Handshake** | `/ws` | Kết nối socket gốc, xác thực JWT. |
| **Driver ➔ Server** | `/app/driver/location` | Tài xế đẩy tọa độ GPS định kỳ (3-5 giây/lần). |
| **Server ➔ Driver** | `/user/queue/offers` | Hệ thống đẩy Offer nhận cuốc mới hoặc báo Offer hết hạn. |
| **Server ➔ Khách/Tài xế** | `/topic/bookings/{id}/status` | Broadcast thông báo thay đổi trạng thái của chuyến đi. |
| **Server ➔ Khách** | `/topic/bookings/{id}/location` | Bắn tọa độ tài xế realtime về cho bản đồ của khách. |
| **Server ➔ User** | `/user/queue/notifications` | Kênh nhận Push Notification chung. |
| **Hai chiều** | `/app/bookings/{id}/chat` | Giao tiếp nhắn tin nội bộ chuyến đi. |

### 3. REST APIs

| HTTP Method | Route | Mô tả nghiệp vụ |
| --- | --- | --- |
| **POST** | `/drivers/me/location` | API Fallback để tài xế gửi tọa độ nếu không dùng WebSocket. |
| **GET** | `/bookings/{id}/driver-location` | API lấy tọa độ tài xế mới nhất của một cuốc xe. |
| **GET** | `/bookings/{id}/route` | Truy vấn toàn bộ lộ trình đã di chuyển của cuốc xe. |
| **GET** | `/notifications` | Lấy danh sách thông báo. |
| **PATCH** | `/notifications/{id}/read` | Đánh dấu thông báo đã xem. |

> **📌 Trách nhiệm chung cho team:** Cung cấp hàm `LocationService.findNearby(lat, lng, radius)` cho Người 2B. Lắng nghe event `BookingStatusChanged` và `DriverAssigned` để push WebSocket và bắn Firebase Cloud Messaging (FCM).

---

## feat/payment (Người 4): Payment & Rating

### 1. Database Entities

| Entity | Các trường chính (Fields) |
| --- | --- |
| **Payment** | `id`, `bookingId` (unique), `amount`, `method` (CASH/WALLET/VNPAY/MOMO), `status` (PENDING/PAID/FAILED/REFUNDED), `paidAt` |
| **PaymentTransaction** | `id`, `paymentId`, `provider`, `providerTxnId`, `amount`, `rawResponse`, `status` |
| **Wallet** | `id`, `userId`, `balance`, `@Version` |
| **WalletTransaction** | `id`, `walletId`, `type` (TOPUP/PAYMENT/COMMISSION/PAYOUT/REFUND), `amount`, `refId`, `balanceAfter` |
| **CommissionRule** | `id`, `platformPercent`, `active` |
| **WithdrawalRequest** | `id`, `driverId`, `amount`, `bankInfo`, `status` |
| **Voucher** | `id`, `code`, `discountType`, `value`, `maxDiscount`, `validFrom`, `validTo`, `usageLimit` |
| **Rating** | `id`, `bookingId`, `raterId`, `rateeId`, `stars` (1–5), `tags`, `comment`, `direction` (CUSTOMER_TO_DRIVER/DRIVER_TO_CUSTOMER) |
| **DamageClaim** | `id`, `bookingId`, `reportedBy`, `description`, `evidenceUrls`, `status`, `compensation` (Tuỳ chọn: Khách báo hư hỏng xe đối chiếu với ảnh chụp bàn giao) |

### 2. REST APIs

| HTTP Method | Route | Mô tả nghiệp vụ |
| --- | --- | --- |
| **POST** | `/payments` | Tạo yêu cầu thanh toán cho chuyến đi |
| **GET** | `/payments/booking/{bookingId}` | Xem trạng thái hóa đơn của chuyến đi |
| **POST** | `/payments/webhook/{provider}` | Webhook nhận callback từ cổng thanh toán (VNPay/MoMo). Validate chữ ký và chống lặp (Idempotent). |
| **POST** | `/payments/{id}/refund` | Admin thực hiện hoàn tiền |
| **GET** | `/wallet`, `/wallet/transactions` | Xem số dư ví nội bộ và lịch sử biến động |
| **POST** | `/wallet/topup` | Nạp tiền vào ví nội bộ |
| **GET** | `/drivers/me/earnings` | Thống kê thu nhập tài xế theo ngày/tuần/tháng |
| **POST** | `/drivers/me/withdrawals` | Tài xế yêu cầu rút tiền ra tài khoản ngân hàng |
| **POST** | `/vouchers/validate` | Áp mã giảm giá và tính toán số tiền được giảm |
| **POST** | `/bookings/{id}/ratings` | Đánh giá sao và nhận xét đối tác sau chuyến đi |
| **GET** | `/drivers/{id}/ratings` | Xem danh sách đánh giá của một tài xế |
| **POST** | `/bookings/{id}/damage-claims` | Báo cáo sự cố hư hỏng xe (Tùy chọn) |

> **📌 Xử lý logic lõi:** Lắng nghe sự kiện `TripCompleted` từ module 2A để chốt `finalFare`. Quá trình trừ chiết khấu hoa hồng và cộng tiền vào ví tài xế phải được bọc trong một hàm `@Transactional` nguyên vẹn. Sau khi thanh toán xử lý xong, phát sự kiện `PaymentCompleted`.
