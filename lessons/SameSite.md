# SameSite

**SameSite** là **thuộc tính của cookie**, dùng để **kiểm soát khi nào browser được phép gửi cookie theo request cross-site**, mục tiêu chính là **giảm CSRF**.

👉 Nó **không phải Spring Security**, không phải CSRF token
👉 Nó là **cơ chế ở tầng browser**

---

## 1. Vấn đề SameSite giải quyết là gì?

Trước SameSite:

* Browser **luôn tự động gửi cookie**
* Kể cả khi request đến từ **website khác**

👉 Đây chính là **nguồn gốc của CSRF**

---

## 2. SameSite hoạt động thế nào?

Browser sẽ **xem xét ngữ cảnh request**:

> “Request này có phải đến từ **cùng site** không?”

Nếu **không cùng site**, cookie **có thể bị chặn** tùy chế độ SameSite.

---

## 3. Ba chế độ SameSite

### 1️⃣ `SameSite=None`

```http
Set-Cookie: SESSION=abc; SameSite=None; Secure
```

* Cookie **luôn được gửi**
* Kể cả cross-site request
* Bắt buộc phải có `Secure`

📌 Dùng khi:

* App nhúng iframe
* SSO
* Domain khác nhau (auth.domain.com → app.domain.com)

⚠️ Nguy hiểm nếu không có CSRF token

---

### 2️⃣ `SameSite=Lax` (mặc định của browser hiện nay)

```http
Set-Cookie: SESSION=abc; SameSite=Lax
```

* Cookie **không gửi với POST cross-site**
* Nhưng **vẫn gửi khi user click link**

📌 Giảm CSRF phần lớn  
📌 Chưa đủ cho hệ thống nhạy cảm

---

### 3️⃣ `SameSite=Strict`

```http
Set-Cookie: SESSION=abc; SameSite=Strict
```

* Cookie **chỉ gửi khi cùng site**
* Mọi cross-site request → **không gửi cookie**

📌 An toàn nhất  
📌 Nhưng dễ làm hỏng UX

---

## 4. “SameSite” ≠ “Same Origin”

Rất hay nhầm ❌

|          | SameSite                                      | SameOrigin           |
| -------- | --------------------------------------------- | -------------------- |
| Dựa trên | eTLD+1                                        | scheme + host + port |
| Ví dụ    | `a.example.com` & `b.example.com` → same-site | ❌                    |
| Tầng     | Browser cookie                                | CORS / JS            |

---

## 5. SameSite có thay thế CSRF không?

❌ **Không hoàn toàn**

|                   | SameSite | CSRF Token |
| ----------------- | -------- | ---------- |
| Chặn CSRF         | Một phần | Đầy đủ     |
| Phụ thuộc browser | ✅        | ❌          |
| Có bypass         | Có       | Rất khó    |

👉 Spring Security **không coi SameSite là CSRF protection**

---

## 6. Khi nào nên dùng SameSite=Strict?

✅ Tốt khi:

* Internal system
* Single domain
* Không SSO
* Không iframe

❌ Không nên khi:

* OAuth / SSO
* Subdomain khác nhau
* Redirect login

---

## 7. Trong Spring (gắn với thực tế)

### Servlet (Spring MVC)

```properties
server.servlet.session.cookie.same-site=strict
```

### Reactive (WebFlux)

```properties
server.reactive.session.cookie.same-site=strict
```

📌 Chỉ áp dụng cho **session cookie**

---

## 8. Nhớ 1 câu là đủ

> **SameSite quyết định: “cookie có được gửi khi request đến từ site khác hay không”**

---
