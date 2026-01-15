# Spring Security 


## 1. Security Fundamentals 

Nếu chưa vững phần này → **dùng Spring Security rất dễ sai**

### 1.1 Authentication vs Authorization (phải phân biệt rõ)

* **Authentication**: Bạn là ai? (login)
* **Authorization**: Bạn được làm gì? (permission)

👉 Sai lầm phổ biến:

* Nhét logic **quyền truy cập** vào authentication
* Check role thủ công trong controller

---

### 1.2 Principal – Credentials – GrantedAuthority

* `Principal`: người dùng hiện tại
* `Credentials`: mật khẩu / token
* `GrantedAuthority`: role / permission

👉 Hiểu sai:

* Role ≠ Permission
* `ROLE_ADMIN` chỉ là **convention string**, không phải magic

---

## 2. Security Architecture (CỰC KỲ QUAN TRỌNG)

> Phần này quyết định bạn có **control được security** hay không

### 2.1 Security Filter Chain (xương sống)

Spring Security **chạy bằng Filter, không phải Controller**

Luồng chuẩn:

```
Request
 → Security Filter Chain
   → Authentication Filter
   → Authorization Filter
 → DispatcherServlet
 → Controller
```

👉 Phải hiểu:

* Request **bị chặn trước khi vào controller**
* Controller **không phải nơi làm security**

---

### 2.2 Filter quan trọng cần biết

| Filter                                 | Vai trò           |
| -------------------------------------- | ----------------- |
| `UsernamePasswordAuthenticationFilter` | Login form        |
| `OncePerRequestFilter`                 | Custom JWT filter |
| `SecurityContextHolderFilter`          | Lưu auth info     |
| `ExceptionTranslationFilter`           | 401 / 403         |
| `FilterSecurityInterceptor`            | Authorization     |

👉 Biết:

* Khi nào cần **custom filter**
* Filter nên đặt **trước hay sau** filter nào

---

## 3. SecurityContext & Thread model

### 3.1 SecurityContextHolder

* Lưu thông tin user **theo thread**
* Mặc định dùng `ThreadLocal`

👉 Phải biết:

* Async / @Async → mất context
* Feign / RestTemplate → phải propagate token

---

### 3.2 Authentication object

Chứa:

* principal
* authorities
* isAuthenticated

👉 Không nên:

* Tạo Authentication fake bừa bãi
* Set context thủ công nếu không hiểu rõ

---

## 4. Authentication mechanisms (cách đăng nhập)

### 4.1 Form Login vs Basic vs Token

| Cơ chế       | Khi dùng                   |
| ------------ | -------------------------- |
| Form login   | Web MVC                    |
| HTTP Basic   | Internal tools             |
| JWT / OAuth2 | API / Mobile / FE tách rời |

👉 Dự án hiện đại **99% là stateless (JWT)**

---

### 4.2 UserDetailsService

* Load user từ DB
* Chỉ dùng cho **Authentication**

👉 Không nhét business logic vào đây
👉 Không query permission dư thừa

---

### 4.3 PasswordEncoder

* `BCryptPasswordEncoder` là chuẩn
* **Không bao giờ tự hash**

---

## 5. Authorization (phần hay bị làm sai nhất)

### 5.1 URL-based vs Method-based security

#### URL-based

```java
http.authorizeHttpRequests()
    .requestMatchers("/admin/**").hasRole("ADMIN")
```

#### Method-based (khuyến nghị)

```java
@PreAuthorize("hasRole('ADMIN')")
```

👉 Dự án lớn:

* **URL = coarse-grained**
* **Method = fine-grained**

---

### 5.2 Role vs Permission

* Role: `ADMIN`
* Permission: `JOB_READ`, `JOB_DELETE`

👉 Best practice:

* FE: role
* BE: permission

---

### 5.3 Ownership / Resource-based authorization (rất quan trọng)

Ví dụ:

* User chỉ được sửa **job của chính mình**

```java
@PreAuthorize("@jobSecurity.isOwner(#jobId)")
```

👉 Đây là chỗ:

* Dùng **Spring EL**
* Kết hợp với **Service / Repository**

---

## 6. CSRF (phần nhiều người hiểu sai)

### 6.1 CSRF là gì (tóm gọn)

* Tấn công dựa trên **cookie tự gửi**
* Chỉ nguy hiểm với **stateful session**

👉 JWT + header → **KHÔNG cần CSRF**

---

### 6.2 Khi nào bật CSRF

| Trường hợp        | CSRF |
| ----------------- | ---- |
| Web MVC + session | ✅    |
| REST API + JWT    | ❌    |
| Mobile app        | ❌    |

---

## 7. Stateless Security (JWT – thực tế nhất)

### 7.1 Stateless nghĩa là gì

* Server **không lưu session**
* Mỗi request phải tự chứng minh

👉 Phải disable:

* session
* csrf
* form login

---

### 7.2 JWT Filter đúng cách

* Validate token
* Load user
* Set Authentication vào context

👉 Không:

* Query DB mỗi request vô tội vạ
* Nhét business logic vào filter

---

## 8. Exception Handling (rất hay bị bỏ qua)

Phân biệt:

| Status | Nghĩa                          |
| ------ | ------------------------------ |
| 401    | Chưa login                     |
| 403    | Login rồi nhưng không có quyền |

👉 Custom:

* `AuthenticationEntryPoint`
* `AccessDeniedHandler`

---

## 9. Security Configuration best practices

### 9.1 Không dùng WebSecurityConfigurerAdapter (deprecated)

Dùng:

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http)
```

---

### 9.2 Không disable security bừa bãi

```java
http.csrf().disable(); // chỉ khi hiểu rõ
```

---

## 10. Testing Security (rất quan trọng nhưng hay quên)

* `@WithMockUser`
* `@WithSecurityContext`
* Test 401 / 403 riêng

---

## 11. Những sai lầm phổ biến (nhớ tránh)

❌ Check quyền trong Controller
❌ Dùng `if (role == ADMIN)`
❌ Dùng session + JWT chung
❌ Nhét security logic vào business
❌ Không hiểu filter order
❌ Nghĩ CSRF = token bị lấy là xong

---

## Tóm tắt “PHẢI NẮM” (Checklist nhanh)

Nếu bạn **trả lời được tất cả** → dùng Spring Security ổn:

* [ ] Phân biệt authn vs authz
* [ ] Hiểu filter chain
* [ ] Biết SecurityContextHolder hoạt động thế nào
* [ ] Dùng method-level security
* [ ] Biết ownership authorization
* [ ] Hiểu khi nào cần CSRF
* [ ] Biết build JWT stateless đúng
* [ ] Phân biệt 401 vs 403

---

# Những cách dùng Spring Security **thực tế cần lưu ý**

**NHỮNG CÁCH DÙNG THỰC TẾ + CẢNH BÁO** trong Spring Security.

> Đọc xong phần này, bạn sẽ biết:
>
> * Chỗ nào **nên dùng**
> * Chỗ nào **tuyệt đối không nên**
> * Vì sao nhiều dự án security “trông có vẻ đúng nhưng thực ra sai”

---

## 1. Đặt đúng ranh giới: Security ≠ Business

### ❌ Sai (rất hay gặp)

```java
if (!user.isAdmin()) {
   throw new ForbiddenException();
}
```

### ✅ Đúng

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {}
```

📌 Nguyên tắc:

> Security = declarative
> Business = imperative

---

## 2. Ưu tiên Method-level Security hơn URL

### ❌ Chỉ dùng URL

```java
.requestMatchers("/jobs/**").hasRole("USER")
```

### ✅ Kết hợp

* URL: chặn thô
* Method: chặn logic

📌 Lý do:

* URL không hiểu business
* Method gắn trực tiếp với use case

---

## 3. Ownership / Resource-based authorization là bắt buộc

### Tình huống thực tế

* User A **không được sửa resource của User B**
* Role USER giống nhau

### Cách làm đúng

```java
@PreAuthorize("@jobSecurity.isOwner(#jobId)")
```

📌 Lưu ý:

* Không hard-code trong service
* Không check trong controller

---

## 4. Không lạm dụng ROLE – dùng Permission cho backend

### ❌ Anti-pattern

* 1 role = 20 quyền
* Mỗi khi thêm feature → thêm role

### ✅ Best practice

* Role: coarse-grained (FE, menu)
* Permission: fine-grained (BE)

📌 Ví dụ:

```
ROLE_ADMIN
  → USER_READ
  → USER_DELETE
```

---

## 5. Stateless (JWT) → bỏ hết session mindset

### Phải làm đủ:

```java
sessionCreationPolicy(STATELESS)
csrf().disable()
formLogin().disable()
```

📌 Cảnh báo:

* Không dùng HttpSession
* Không lưu user context server-side

---

## 6. JWT Filter: chỉ làm security, không làm business

### ❌ Sai

* Query nhiều bảng
* Check quyền trong filter

### ✅ Đúng

* Validate token
* Set Authentication
* Stop

📌 Quy tắc:

> Filter = gate
> Service = logic

---

## 7. Phân biệt rõ 401 vs 403 (rất hay bị sai)

| Code | Khi nào                     |
| ---- | --------------------------- |
| 401  | Chưa login / token invalid  |
| 403  | Login rồi nhưng thiếu quyền |

📌 FE + Mobile cực kỳ cần phân biệt để xử lý UX đúng

---

## 8. CSRF: chỉ bật khi thực sự cần

### ❌ Bật CSRF cho REST API

→ tự bắn vào chân

### ✅ Bật CSRF cho:

* Web MVC
* Cookie + session

📌 Nhớ:

> JWT header ≠ CSRF target

---

## 9. Đừng tin annotation nếu chưa bật đúng config

### ❌

```java
@PreAuthorize("hasRole('ADMIN')")
```

nhưng **quên**:

```java
@EnableMethodSecurity
```

📌 Thực tế:

* Annotation không có tác dụng
* Mọi request đều qua

---

## 10. SecurityContext không tự tồn tại ngoài request

### ❌

* @Async
* Scheduler
* Kafka consumer

→ `authentication == null`

### ✅

* Truyền token
* Dùng DelegatingSecurityContext

---

## 11. Đừng để Security phụ thuộc DB nặng

### ❌

* Mỗi request query full permission
* Join 5–6 bảng

### ✅

* Cache quyền
* Embed permission vào JWT (cân nhắc)

📌 Trade-off:

* Performance vs real-time permission

---

## 12. Luôn test security, không chỉ test logic

### Test bắt buộc:

* Anonymous → 401
* User → 403
* Admin → 200

📌 Dự án thực tế:

> Security bug = production bug

---

## 13. Logging & Debug đúng chỗ

* Log khi:

    * Auth failed
    * Access denied

* Không log:

    * Token
    * Password

📌 Bật tạm:

```properties
logging.level.org.springframework.security=DEBUG
```

---

## 14. Không disable “cho nhanh”

### ❌

```java
http.authorizeHttpRequests().anyRequest().permitAll();
```

📌 Lý do:

* Security config thường **không được revisit**
* “tạm thời” = vĩnh viễn

---

## 15. Đừng over-engineer

### ❌

* OAuth2
* Keycloak
* RBAC phức tạp

trong app CRUD nhỏ

### ✅

* Đủ dùng
* Dễ maintain
* Có thể mở rộng

---

# Checklist “dùng đúng trong dự án”

Trước khi merge:

* [ ] Không check quyền trong controller
* [ ] Có method-level security
* [ ] Phân biệt 401 / 403
* [ ] JWT filter gọn
* [ ] Không lạm dụng role
* [ ] Có ownership validation
* [ ] Không bật CSRF sai chỗ

---

# @EnableWebSecurity vs @EnableMethodSecurity

Câu này rất hay, vì **rất nhiều người dùng Spring Security nhưng không hiểu ranh giới của 2 annotation này**.

---

## 1. @EnableWebSecurity là gì?

👉 **Bật cơ chế bảo mật ở tầng Web (HTTP layer)**

### Nó làm gì?

* Kích hoạt **Security Filter Chain**
* Cho phép bạn cấu hình:

  * Authentication
  * Authorization theo URL
  * CSRF
  * Session
  * Login / Logout
* Áp dụng cho **mọi HTTP request**

📌 Hiểu đơn giản:

> **@EnableWebSecurity = bật “cổng bảo vệ” cho request HTTP**

---

### Ví dụ tác dụng

```java
@Bean
SecurityFilterChain securityFilterChain(HttpSecurity http) {
    http.authorizeHttpRequests()
        .requestMatchers("/admin/**").hasRole("ADMIN")
        .anyRequest().authenticated();
    return http.build();
}
```

👉 Không có `@EnableWebSecurity` → filter chain **không được kích hoạt**

---

## 2. @EnableMethodSecurity là gì?

👉 **Bật bảo mật ở tầng Method (business layer)**

### Nó làm gì?

* Kích hoạt các annotation:

  * `@PreAuthorize`
  * `@PostAuthorize`
  * `@Secured`
  * `@RolesAllowed`
* Áp dụng cho:

  * Service
  * Component
  * (có thể cả Controller method)

📌 Hiểu đơn giản:

> **@EnableMethodSecurity = gắn khóa ngay trên method**

---

### Ví dụ tác dụng

```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) {}
```

👉 Nếu **không bật `@EnableMethodSecurity`**:

* Annotation tồn tại
* **Nhưng KHÔNG được thực thi** ❌

---

## 3. So sánh nhanh

|                        | @EnableWebSecurity | @EnableMethodSecurity       |
| ---------------------- | ------------------ | --------------------------- |
| Tầng                   | HTTP / Web         | Method / Business           |
| Bảo vệ cái gì          | Request            | Method call                 |
| Cơ chế                 | Filter             | AOP (proxy)                 |
| Dùng cho               | URL, session, CSRF | Role, permission, ownership |
| Có thay thế nhau không | ❌                  | ❌                           |

---

## 4. Có cần dùng cả hai không?

### Câu trả lời thực tế:

> **Dự án nghiêm túc → CẦN CẢ HAI**

### Vì sao?

* Web security:

  * Chặn request không hợp lệ từ ngoài
* Method security:

  * Chặn logic sai từ bên trong (reuse service, async, test…)

📌 Nguyên tắc:

> **Không tin tầng ngoài, phải khóa tầng trong**

---

## 5. Các case thực tế

### Case 1: Chỉ dùng @EnableWebSecurity ❌

* Service bị gọi trực tiếp
* Controller khác reuse service
* Async job gọi nhầm

👉 Bypass security

---

### Case 2: Chỉ dùng @EnableMethodSecurity ❌

* Request chưa auth vẫn vào controller
* CSRF, session không được bảo vệ

---

### Case 3: Kết hợp ✅ (best practice)

```java
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {}
```

---

## 6. Spring Boot mới thì sao?

📌 Spring Boot 3+:

* `@EnableWebSecurity` **có thể không cần explicit**
* Nhưng:

  * **NÊN khai báo rõ ràng** cho dễ đọc & maintain

---

## 7. Nhớ 1 câu là đủ

> **@EnableWebSecurity bảo vệ REQUEST,
> @EnableMethodSecurity bảo vệ LOGIC**

---

References:
- https://docs.spring.io/spring-security/reference/servlet/getting-started.html