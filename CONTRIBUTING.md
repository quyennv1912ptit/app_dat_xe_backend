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