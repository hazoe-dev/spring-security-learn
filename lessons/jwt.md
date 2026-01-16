# JWT -  JSON Web Token

> Mục tiêu: **biết mình đang làm gì, vì sao làm vậy, và tránh các lỗi chết người khi dùng JWT với Spring Security**

---

## I. NỀN TẢNG BẮT BUỘC 

### 1️⃣ JWT KHÔNG phải cơ chế bảo mật – chỉ là token format

* JWT **không tự authenticate**
* JWT **không tự authorize**
* JWT **không liên quan Spring nếu bạn không viết filter**

👉 Spring Security **chỉ tin `Authentication` trong `SecurityContext`**, không tin JWT.

---

### 2️⃣ Authentication vs Authorization trong Spring

Phải phân biệt được:

| Khái niệm      | Trong Spring                         |
| -------------- | ------------------------------------ |
| Authentication | `Authentication` object              |
| Authorization  | `GrantedAuthority` / `@PreAuthorize` |
| JWT            | chỉ là **nguồn dữ liệu**             |

📌 JWT **chỉ được dùng để tạo Authentication**, không phải để check quyền trực tiếp.

---

### 3️⃣ Stateless nghĩa là gì (và hậu quả)

Khi dùng JWT:

```java
sessionCreationPolicy(STATELESS)
```

Spring **không nhớ user**, dẫn đến:

* Mỗi request phải gửi JWT
* Logout không invalidate token
* Token lộ → dùng được đến khi hết hạn

👉 Phải chấp nhận hoặc có chiến lược bổ sung (refresh / blacklist).

---

## II. JWT CỤ THỂ TRONG SPRING SECURITY

### 4️⃣ JWT được xử lý ở đâu?

**KHÔNG phải Controller**

JWT chạy ở:

```
OncePerRequestFilter
```

Flow chuẩn:

```
Request
 → JwtFilter
   → verify token
   → build Authentication
   → set SecurityContext
 → AuthorizationFilter
 → Controller
```

📌 Nếu không set `SecurityContextHolder` → Spring coi như **anonymous**.

---

### 5️⃣ Authentication object là trung tâm

Bạn **phải tạo đúng Authentication**:

```java
Authentication auth =
    new UsernamePasswordAuthenticationToken(
        userId,
        null,
        authorities
    );
SecurityContextHolder.getContext().setAuthentication(auth);
```

Không có bước này:

* `@PreAuthorize` ❌
* `hasRole()` ❌
* `authentication.name` ❌

---

### 6️⃣ Role vs Authority (rất hay dính bug)

Spring có quy ước:

| Cách dùng                  | Spring expect |
| -------------------------- | ------------- |
| hasRole("ADMIN")           | ROLE_ADMIN    |
| hasAuthority("ROLE_ADMIN") | ROLE_ADMIN    |

📌 JWT thường chứa `"ADMIN"` → **phải map**:

```java
new SimpleGrantedAuthority("ROLE_" + role)
```

---

## III. JWT – PHẢI HIỂU VỀ BẢO MẬT

### 7️⃣ Verify chữ ký trước khi đọc payload

JWT có thể **bị giả payload** nếu:

* không verify signature
* chỉ base64 decode

👉 Quy tắc sắt:

> **Không đọc claim nào trước khi verify token**

---

### 8️⃣ Thuật toán ký: HS256 vs RS256

| Thuật toán | Khi dùng                   |
| ---------- | -------------------------- |
| HS256      | monolith                   |
| RS256      | microservice / auth-server |

📌 Nhiều project chết vì:

> dùng HS256 cho 5 service chia sẻ secret ❌

---

### 9️⃣ Expiration không phải optional

JWT **phải có `exp`**

* Access token: 5–15 phút
* Refresh token: dài hạn

❌ Token sống 1–7 ngày = **lỗ hổng bảo mật**

---

## IV. JWT + HTTP / BROWSER

### 🔟 JWT không tự chống CSRF

| Cách gửi JWT         | CSRF |
| -------------------- | ---- |
| Authorization Header | ❌    |
| Cookie               | ✅    |

📌 Sai lầm kinh điển:

> “JWT rồi nên disable CSRF” ❌

→ chỉ đúng nếu **Bearer token**

---

### 1️⃣1️⃣ Lưu JWT ở đâu?

| Nơi             | Đánh giá    |
| --------------- | ----------- |
| localStorage    | ❌ XSS       |
| sessionStorage  | ⚠️          |
| HttpOnly Cookie | ✅ (có CSRF) |

👉 Phải hiểu trade-off, không có cách “chuẩn tuyệt đối”.

---

## V. NHỮNG THỨ PHẢI BIẾT ĐỂ KHÔNG DÙNG SAI

### 1️⃣2️⃣ Logout trong JWT nghĩa là gì?

* Không phải xoá token server
* Là client **ngừng gửi token**
* Hoặc server có blacklist

📌 Ai nói “JWT logout như session” là **chưa hiểu JWT**.

---

### 1️⃣3️⃣ JWT không thay thế session trong mọi trường hợp

**KHÔNG nên dùng JWT khi**:

* cần revoke user ngay
* hệ thống nội bộ
* ít user, ít scale

👉 Session + Redis đơn giản & an toàn hơn.

---

## VI. CHECKLIST “DÙNG ĐÚNG JWT TRONG SPRING”

✔ JWT xử lý trong filter  
✔ Token luôn được verify  
✔ Authentication được set đúng  
✔ Role map đúng chuẩn Spring  
✔ Stateless thật sự  
✔ Access token ngắn hạn  
✔ Hiểu rõ CSRF khi dùng cookie

---
## Khái niệm cần nắm

Đây là **checklist những khái niệm BẮT BUỘC phải nắm** nếu muốn **dùng JWT đúng – không “ảo tưởng bảo mật” – trong Spring / Spring Security**. Mình sẽ đi từ **bản chất JWT → tích hợp Spring → các lỗi chết người hay gặp**.

---

### 1️⃣ JWT không phải là “cơ chế auth”, nó chỉ là **format token**

**JWT = JSON Web Token**

👉 Nó **chỉ là một chuỗi text có cấu trúc**, không tự động:

* authenticate
* authorize
* protect API

Spring Security **không “hiểu JWT” sẵn**, bạn phải:

* verify token
* extract user
* set `Authentication` vào `SecurityContext`

📌 Sai lầm kinh điển:

> “Dùng JWT rồi thì không cần Spring Security nữa” ❌

---

### 2️⃣ Authentication vs Authorization (rất hay bị lẫn)

| Khái niệm      | JWT làm gì                                      |
| -------------- | ----------------------------------------------- |
| Authentication | JWT **chứng minh danh tính** (userId, username) |
| Authorization  | JWT **chứa claim quyền** (roles, scopes)        |

JWT thường chứa:

```json
{
  "sub": "user123",
  "roles": ["ROLE_ADMIN"],
  "exp": 1712345678
}
```

📌 JWT **không quyết định quyền**, Spring Security quyết định quyền dựa trên:

```java
Authentication.getAuthorities()
```

---

### 3️⃣ Stateless là gì – và cái giá phải trả

JWT thường đi với:

```java
sessionCreationPolicy(STATELESS)
```

👉 Nghĩa là:

* Server **không nhớ gì về user**
* Mỗi request **tự mang theo token**
* Logout ≠ invalidate token

📌 Hệ quả:

* ❌ Không revoke token được ngay
* ❌ Không ép logout toàn hệ thống
* ❌ Token bị lộ → dùng được đến khi hết hạn

➡ Giải pháp thực tế:

* access token ngắn hạn
* refresh token
* blacklist / token version / redis (nâng cao)

---

### 4️⃣ Cấu trúc JWT – phải hiểu để không dùng sai

JWT gồm 3 phần:

```
header.payload.signature
```

#### 🔹 Header

```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

#### 🔹 Payload (CLAIMS – cực kỳ quan trọng)

| Claim | Ý nghĩa         |
| ----- | --------------- |
| sub   | user identifier |
| exp   | hết hạn         |
| iat   | thời điểm tạo   |
| roles | quyền           |

📌 **Không bao giờ tin payload nếu chưa verify signature**

---

### 5️⃣ Ký JWT: Symmetric vs Asymmetric (rất quan trọng)

#### 🔐 HS256 (shared secret)

* Server ký
* Server verify
* Đơn giản
* ❌ Microservice rất nguy hiểm

#### 🔐 RS256 (public / private key)

* Auth server ký
* Resource server verify bằng public key
* Chuẩn OAuth2

📌 Dự án > 1 service → **KHÔNG dùng HS256**

---

### 6️⃣ JWT trong Spring chạy ở đâu?

JWT **KHÔNG chạy trong Controller**

👉 Nó chạy trong **Security Filter Chain**

Thứ tự:

```
Request
 ↓
JwtAuthenticationFilter
 ↓
SecurityContextHolder.setAuthentication()
 ↓
Authorization Filter
 ↓
Controller
```

📌 Nếu bạn thấy:

```java
authentication == null
```

→ 99% là **filter sai vị trí**

---

### 7️⃣ Authentication trong Spring Security là gì?

Sau khi parse JWT, bạn phải tạo:

```java
UsernamePasswordAuthenticationToken
```

```java
Authentication auth =
    new UsernamePasswordAuthenticationToken(
        userId,
        null,
        authorities
    );
```

📌 Nếu không set `SecurityContextHolder`:

* `@PreAuthorize` ❌
* `hasRole()` ❌
* `authentication.name` ❌

---

### 8️⃣ Role vs Authority – lỗi gây “đau não”

| Spring expects             | Ví dụ                |
| -------------------------- | -------------------- |
| ROLE_ prefix               | `ROLE_ADMIN`         |
| hasRole("ADMIN")           | tự động thêm `ROLE_` |
| hasAuthority("ROLE_ADMIN") | không thêm           |

📌 JWT chứa:

```json
"roles": ["ADMIN"]
```

❌ Spring **không hiểu**

➡ Phải map thành:

```java
new SimpleGrantedAuthority("ROLE_ADMIN")
```

---

### 9️⃣ JWT ≠ CSRF protection

JWT **KHÔNG tự động chống CSRF**

| Trường hợp            | CSRF cần không |
| --------------------- | -------------- |
| Authorization: Bearer | ❌              |
| Cookie chứa JWT       | ✅              |

📌 Lỗi chết người:

> “JWT rồi thì disable CSRF luôn” ❌  
> → Chỉ đúng nếu **Bearer token trong header**

---

### 🔟 Refresh Token – không phải optional

**Access token**:

* ngắn hạn (5–15 phút)

**Refresh token**:

* dài hạn
* lưu DB / Redis
* rotate

📌 Không có refresh token → user bị logout liên tục

---

### 1️⃣1️⃣ Những sai lầm kinh điển (đã thấy rất nhiều)

❌ Nhét password vào JWT  
❌ Token sống 24h–7 ngày  
❌ Không check `exp`  
❌ Không verify signature  
❌ Trust payload từ client  
❌ Để JWT trong localStorage mà bật CORS `*`  
❌ Dùng JWT cho session-based app  

---

### 1️⃣2️⃣ Khi nào KHÔNG nên dùng JWT?

🚫 App nội bộ, admin dashboard  
🚫 App cần revoke user tức thì  
🚫 App ít user, ít scale

➡ Session + Redis **đơn giản & an toàn hơn**

## References:
- https://www.jwt.io/

## Install libraries

Giải thích kỹ hơn một chút để dùng **đúng ngữ cảnh Maven + JJWT**.

---

## 1. `jjwt-api` là gì?

Từ phiên bản **JJWT 0.11+**, thư viện được tách thành **3 module**:

| Module                       | Vai trò                                                    |
| ---------------------------- | ---------------------------------------------------------- |
| `jjwt-api`                   | **Chỉ chứa interface / API** (JwtBuilder, Claims, Jwts, …) |
| `jjwt-impl`                  | Implementation thực tế                                     |
| `jjwt-jackson` / `jjwt-gson` | JSON serializer                                            |

👉 `jjwt-api` **KHÔNG phải là API theo nghĩa “provided by container”**  
👉 Nó là **public API của thư viện**, nhưng **bạn vẫn phải đóng gói nó vào app**

---

## 2. Dùng scope nào cho `jjwt-api`?

### ✅ ĐÚNG: `compile` (hoặc không ghi scope)

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.13.0</version>
</dependency>
```

Hoặc:

```xml
<scope>compile</scope>
```

**Lý do**:

* Code của bạn **import trực tiếp**:

  ```java
  import io.jsonwebtoken.Jwts;
  import io.jsonwebtoken.Claims;
  ```
* Nên `jjwt-api` **phải có mặt khi compile + runtime**

---

## 3. ❌ Vì sao KHÔNG dùng `provided`?

`provided` chỉ dùng khi:

* Thư viện **đã được container cung cấp sẵn**
* Ví dụ: `jakarta.servlet-api`, `javax.servlet-api`

❌ `jjwt-api` **KHÔNG được Spring / JVM / Tomcat cung cấp**
→ dùng `provided` sẽ gây lỗi:

```
ClassNotFoundException: io.jsonwebtoken.Jwts
```

---

## 4. Cấu hình JJWT chuẩn trong Spring Boot

Cách **best practice**:

```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.13.0</version>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>

<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.13.0</version>
    <scope>runtime</scope>
</dependency>
```

### Vì sao `impl` & `jackson` để `runtime`?

* Bạn **không import trực tiếp class của chúng**
* Chỉ cần khi chạy app

---

## 5. Tóm tắt nhanh 

> **“API” trong tên artifact KHÔNG quyết định Maven scope**

| Artifact       | Scope đúng |
| -------------- | ---------- |
| `jjwt-api`     | `compile`  |
| `jjwt-impl`    | `runtime`  |
| `jjwt-jackson` | `runtime`  |

---
# Xem thiết kế thư viện cấp kiến trúc
Mình đi theo 3 tầng:

1. Vì sao **JJWT phải tách module**
2. Cơ chế kỹ thuật phía sau
3. So sánh **JJWT vs Spring (`spring-web` / `spring-webmvc`)**

---

# 1. Vì sao JJWT tách module?

## 1.1. Vấn đề của thư viện “all-in-one” (trước 0.11)

Trước đây JJWT là **1 jar duy nhất**:

```text
jjwt-0.9.x.jar
```

### Vấn đề:

* ❌ Bị **ràng buộc chặt** vào:

    * Jackson
    * Một số crypto provider
* ❌ Không dùng được trong:

    * Android
    * GraalVM native
    * App server hạn chế classpath
* ❌ Người dùng **không có quyền chọn JSON engine**

👉 Vi phạm **Dependency Inversion Principle**

---

## 1.2. Mục tiêu khi tách module

JJWT muốn:

* Chỉ expose **contract**
* Implementation **có thể thay đổi / thay thế**
* App **chỉ phụ thuộc cái nó cần**

=> Áp dụng **Clean Architecture / Hexagonal Architecture**

---

# 2. Kiến trúc JJWT sau khi tách

## 2.1. Module breakdown

```
jjwt-api
 ├─ JwtBuilder
 ├─ JwtParser
 ├─ Claims
 ├─ Jwts (factory)
 └─ interfaces only

jjwt-impl
 ├─ DefaultJwtBuilder
 ├─ DefaultJwtParser
 ├─ crypto implementation
 └─ uses ServiceLoader

jjwt-jackson / jjwt-gson
 └─ JSON serialization implementation
```

---

## 2.2. Điểm mấu chốt: **Service Provider Interface (SPI)**

Trong `jjwt-api`:

```java
public interface Serializer<T> { ... }
public interface Deserializer<T> { ... }
```

Trong `jjwt-jackson`:

```java
public class JacksonSerializer implements Serializer<Map<String, ?>> { ... }
```

### Kết nối bằng:

```text
META-INF/services/...
```

→ Runtime mới bind implementation

💡 **API không biết impl là ai**

---

## 2.3. Lợi ích thực tế

### ✅ Loose coupling

* Đổi Jackson → Gson **không sửa code**

### ✅ Smaller footprint

* App chỉ cần API + runtime impl

### ✅ Dễ test

* Có thể mock API
* Có thể thay impl khác

### ✅ Future-proof

* JJWT có thể:

    * đổi thuật toán ký
    * hỗ trợ native image
    * thêm JSON engine mới

---

# 3. So sánh với Spring: `spring-web` vs `spring-webmvc`

## 3.1. Sơ đồ tổng quan

```
spring-web
 ├─ HttpRequest
 ├─ RestTemplate
 ├─ WebClient
 ├─ Multipart
 └─ HTTP abstraction

spring-webmvc
 ├─ DispatcherServlet
 ├─ @Controller
 ├─ @RequestMapping
 ├─ HandlerAdapter
 └─ Servlet-based MVC
```

---

## 3.2. Vai trò từng module

### `spring-web`

👉 **HTTP abstraction layer**

* Không phụ thuộc Servlet
* Dùng được cho:

    * MVC
    * WebFlux
    * Client-side HTTP

Ví dụ:

```java
HttpHeaders headers = new HttpHeaders();
```

---

### `spring-webmvc`

👉 **Servlet-based web framework**

* Phụ thuộc:

    * Servlet API
* Implement MVC pattern

Ví dụ:

```java
@RestController
@GetMapping("/hello")
```

---

## 3.3. Điểm giống nhau với JJWT

| JJWT                      | Spring                |
| ------------------------- | --------------------- |
| `jjwt-api`                | `spring-web`          |
| `jjwt-impl`               | `spring-webmvc`       |
| API thuần                 | Implementation cụ thể |
| Không phụ thuộc framework | Phụ thuộc Servlet     |

👉 **spring-webmvc phụ thuộc spring-web**  
👉 **jjwt-impl phụ thuộc jjwt-api**

---

## 3.4. Điểm khác nhau quan trọng

| JJWT                       | Spring              |
| -------------------------- | ------------------- |
| Dùng SPI (`ServiceLoader`) | Dùng DI Container   |
| Runtime auto-discovery     | Startup-time wiring |
| Lightweight                | Heavy framework     |
| Thư viện                   | Framework           |

---

# 4. Review

> **Why does JJWT split into api / impl modules?**

💬 Reply:

> *To enforce separation between contract and implementation, reduce dependency coupling, allow pluggable JSON/crypto providers, and support environments like Android or GraalVM. The API module is stable, while implementations can evolve independently.*

---

# 5. Mental model cần nhớ

> **Tên artifact có chữ “api” KHÔNG liên quan tới Maven scope**

* `api` → architectural boundary
* `compile / runtime` → build-time decision

---

# GraalVM Native Image

- ảnh hưởng trực tiếp tới cách **Spring / JJWT / Jackson** thiết kế thư viện.

Mình giải thích theo thứ tự:

1. GraalVM là gì
2. GraalVM Native Image là gì
3. Vì sao nó “khó”
4. Vì sao thư viện như **JJWT phải tách module để hỗ trợ GraalVM**
5. So sánh JVM thường vs Native Image

---

# 1. GraalVM là gì?

**GraalVM** là một **high-performance runtime** do Oracle phát triển, gồm:

* JVM (chạy Java bytecode)
* Polyglot (Java, JS, Python, Ruby…)
* JIT compiler rất mạnh (Graal)
* **Native Image tool**

👉 GraalVM ≠ chỉ là Native Image  
👉 Native Image chỉ là **một tính năng**

---

# 2. GraalVM Native Image là gì?

👉 **Compile Java app thành file binary chạy thẳng trên OS**

```text
Java code
  ↓ AOT (Ahead-of-Time)
Native binary (Linux / Windows / macOS)
```

### Kết quả:

* ❌ Không cần JVM khi chạy
* ✅ Start cực nhanh (ms)
* ✅ Memory rất thấp

Ví dụ:

```bash
./my-spring-app
```

---

# 3. JVM thường vs Native Image

| Tiêu chí              | JVM thường    | GraalVM Native      |
| --------------------- | ------------- | ------------------- |
| Compilation           | JIT (runtime) | AOT (build time)    |
| Startup               | Chậm (2–5s)   | Rất nhanh (10–50ms) |
| Memory                | Cao           | Thấp                |
| Reflection            | Thoải mái     | ❌ Hạn chế           |
| Dynamic class loading | OK            | ❌ Gần như không     |
| Classpath scanning    | OK            | ❌                   |

👉 Native Image **đóng băng mọi thứ lúc build**

---

# 4. Vì sao GraalVM “khó chơi”?

## 4.1. Reflection bị hạn chế

Code kiểu này:

```java
Class.forName("com.foo.Bar")
```

→ ❌ Native Image **không biết trước**

Bạn phải khai báo:

```json
{
  "name": "com.foo.Bar",
  "allDeclaredConstructors": true
}
```

---

## 4.2. SPI (`ServiceLoader`) cũng là dynamic

Ví dụ:

```java
ServiceLoader.load(Serializer.class)
```

→ Native Image **không tự phát hiện**

Nếu thư viện viết không khéo → **crash khi build**

---

# 5. Vì sao JJWT phải tách module để hỗ trợ GraalVM?

## 5.1. Nếu JJWT không tách

```
jjwt-all
 ├─ jackson
 ├─ gson
 ├─ crypto
 ├─ reflection
```

❌ Native Image:

* Không biết JSON impl nào dùng
* Phải config reflection rất phức tạp
* Binary to, khó optimize

---

## 5.2. Khi JJWT tách module

```
App
 ├─ jjwt-api        (compile)
 ├─ jjwt-impl       (runtime)
 └─ jjwt-jackson    (runtime)
```

### Lợi ích:

* Native Image chỉ thấy **những gì thực sự dùng**
* Không scan lung tung
* JSON engine **được chọn tường minh**

👉 **Deterministic build** (cực kỳ quan trọng cho native)

---

# 6. So sánh với Spring Boot & GraalVM

Spring trước đây ❌ **rất khó native** vì:

* Reflection everywhere
* Classpath scanning
* Dynamic proxies

### Spring giải quyết bằng:

* Spring AOT
* Runtime hints
* Build-time processing

Ví dụ:

```java
@RegisterReflectionForBinding(MyDto.class)
```

---

# 7. Use case thực tế

### Khi nào nên dùng Native Image?

* Microservices
* Serverless (AWS Lambda)
* CLI tools
* Cold-start sensitive apps

### Khi KHÔNG nên dùng?

* App enterprise lớn
* Reflection-heavy
* Dynamic plugin system

---

# 8. Review

> **What is GraalVM Native Image?**

💬 Reply:

> *It is an ahead-of-time compilation technology that converts Java applications into standalone native binaries, enabling very fast startup time and low memory usage, at the cost of limited runtime dynamism such as reflection and class loading.*

---

# 9. Liên hệ 

> ❓ *Why does JJWT split modules?*

👉 Một lý do rất lớn:

* **Support GraalVM Native Image**
* Reduce dynamic behavior
* Make dependency graph explicit

---
# Spring Security dùng **SPI + Strategy**

Spring Security dùng **SPI + Strategy** không phải vì “cho hay”, mà vì **bắt buộc** nếu muốn tồn tại trong **hàng chục môi trường + use case khác nhau**.

Mình phân tích theo 5 lớp:

1. Bài toán Spring Security phải giải
2. Strategy pattern trong Spring Security
3. SPI (Service Provider Interface) trong Spring Security
4. Vì sao **không thể** dùng cách khác

---

# 1. Bài toán Spring Security phải giải

Spring Security phải hoạt động cho:

* Session-based (form login)
* Stateless (JWT)
* OAuth2 / OIDC
* LDAP
* SAML
* Basic / Digest
* Servlet / Reactive
* Tomcat / Jetty / Netty
* Monolith / Microservice

👉 **Không có 1 cách auth duy nhất**

Nếu hard-code:

```java
if (jwt) { ... }
else if (session) { ... }
else if (oauth) { ... }
```

→ ❌ chết kiến trúc ngay

---

# 2. Strategy pattern trong Spring Security

## 2.1. Authentication = Strategy

### Interface gốc:

```java
public interface Authentication {
    Object getPrincipal();
    Object getCredentials();
}
```

### Strategy xử lý:

```java
public interface AuthenticationProvider {
    Authentication authenticate(Authentication authentication);
}
```

### Các implementation:

* `DaoAuthenticationProvider`
* `JwtAuthenticationProvider`
* `OAuth2LoginAuthenticationProvider`
* `LdapAuthenticationProvider`

👉 **Đổi chiến lược = đổi Provider**

---

## 2.2. Authorization cũng là Strategy

```java
public interface AuthorizationManager<T> {
    AuthorizationDecision check(Supplier<Authentication> auth, T object);
}
```

Implementations:

* `AuthorityAuthorizationManager`
* `AuthenticatedAuthorizationManager`
* Custom ownership checker

---

## 2.3. Password encoding

```java
public interface PasswordEncoder {
    String encode(CharSequence raw);
}
```

Implementations:

* BCrypt
* Argon2
* PBKDF2

👉 Strategy cho crypto (rất quan trọng)

---

# 3. SPI trong Spring Security

## 3.1. SPI là gì trong ngữ cảnh Spring?

SPI = **Framework định nghĩa interface**
→ **User / thư viện khác implement**

Spring Security **không biết**:

* Bạn dùng JWT hay Session
* Password encoder nào
* User source ở đâu

---

## 3.2. Ví dụ SPI cực điển hình: `UserDetailsService`

```java
public interface UserDetailsService {
    UserDetails loadUserByUsername(String username);
}
```

### User tự implement:

```java
@Service
public class CustomUserDetailsService
        implements UserDetailsService {
}
```

👉 Spring gọi code của bạn  
👉 **Framework đảo chiều phụ thuộc**

(DIP – Dependency Inversion Principle)

---

## 3.3. Filter chain = SPI + Strategy + Chain of Responsibility

```java
SecurityFilterChain
 ├─ UsernamePasswordAuthenticationFilter
 ├─ JwtAuthenticationFilter
 ├─ ExceptionTranslationFilter
 ├─ AuthorizationFilter
```

Bạn có thể:

* Thêm
* Xoá
* Đổi thứ tự

👉 **Composable security**

---

# 4. Vì sao Spring Security không dùng cách khác?

## 4.1. Vì không thể hard-code

Security là:

* Environment-specific
* Policy-specific
* Risk-specific

Framework **không được phép đoán**

---

## 4.2. Vì phải Open for Extension, Closed for Modification

Spring Security **20+ năm tuổi**

Nếu mỗi auth type sửa core:  
→ ❌ maintenance hell

---

## 4.3. Vì phải sống với GraalVM / Android / Reactive

* SPI → explicit contract
* Strategy → hạn chế reflection
* Dễ AOT processing

---

# 5. So sánh với JJWT & SLF4J

| Framework       | API         | Impl          |
| --------------- | ----------- | ------------- |
| Spring Security | Interface   | User / module |
| JJWT            | `jjwt-api`  | `jjwt-impl`   |
| SLF4J           | `slf4j-api` | `logback`     |

👉 **Same philosophy**

---

# 6. Một ví dụ flow hoàn chỉnh (JWT)

```
Request
 → JwtAuthenticationFilter
 → AuthenticationManager
 → JwtAuthenticationProvider
 → JwtDecoder (strategy)
 → Authentication
 → AuthorizationManager
```

Không bước nào biết implementation cụ thể.

---

# 7. Review

> **Why does Spring Security heavily rely on SPI and Strategy patterns?**

💬 Reply:

> *Because authentication and authorization mechanisms vary widely across applications and environments. SPI allows the framework to define stable contracts while delegating implementations to users or modules, and Strategy enables interchangeable security behaviors without modifying the core framework. This ensures extensibility, maintainability, and long-term evolution.*

---

# 8. Mental model nên nhớ

> **Spring Security = security orchestration framework**

Nó:

* Không authenticate cho bạn
* Không authorize cho bạn  
  👉 Nó **điều phối** các chiến lược bạn cung cấp

---

Câu này **rất đúng thời điểm** 👍
Vì từ **Spring Boot 3 / Spring Framework 6**, **AOT (Ahead-Of-Time)** không còn là “tùy chọn”, mà là **nền tảng để Spring sống được với GraalVM**.

Mình giải thích theo 6 phần, từ dễ → sâu.

---

# 1. AOT là gì 

**AOT (Ahead-Of-Time)** trong Spring => **Phân tích & xử lý ứng dụng Spring ở build-time thay vì runtime**

Thay vì:

* chạy app
* scan classpath
* dùng reflection
* tạo proxy động

➡️ Spring **làm trước** ở lúc build.

---

# 2. Spring truyền thống hoạt động thế nào (runtime-heavy)

Khi bạn chạy app Spring:

```
start JVM
 → scan classpath
 → parse @Configuration
 → process @Bean
 → create proxy (CGLIB / JDK)
 → reflection everywhere
 → build ApplicationContext
```

### Hệ quả:

* Startup chậm
* Tốn RAM
* ❌ Native Image rất khó

---

# 3. Spring AOT làm gì?

## 3.1. AOT engine

Spring AOT **chạy khi build**:

```
mvn package
 → Spring AOT engine
   → analyze @Configuration
   → analyze @Bean
   → analyze @Autowired
   → analyze proxies
   → analyze reflection usage
```

---

## 3.2. Sinh code tĩnh (generated code)

Spring AOT sinh ra code kiểu:

```java
public class MyApp__BeanDefinitions {
    public static BeanDefinition userService() {
        RootBeanDefinition bd =
            new RootBeanDefinition(UserService.class);
        bd.setInstanceSupplier(UserService::new);
        return bd;
    }
}
```

👉 **Không cần reflection lúc runtime**

---

## 3.3. Sinh runtime hints

```java
RuntimeHints hints = ...
hints.reflection().registerType(User.class);
```

👉 Cho GraalVM Native biết **class nào được phép reflect**

---

# 4. AOT để làm gì?

## 4.1. Mục tiêu chính

| Mục tiêu        | Kết quả            |
| --------------- | ------------------ |
| Giảm startup    | ms thay vì seconds |
| Giảm memory     | ~50–70%            |
| Native-friendly | build được GraalVM |
| Deterministic   | ít magic runtime   |

---

## 4.2. AOT ≠ chỉ dành cho Native

⚠️ Hiểu nhầm phổ biến:

> “AOT chỉ dùng khi build native image”

❌ Sai

Spring Boot 3:

* **JVM mode cũng hưởng lợi**
* Startup nhanh hơn
* Ít reflection hơn

---

# 5. AOT khác gì so với GraalVM Native Image?

| Spring AOT        | GraalVM Native         |
| ----------------- | ---------------------- |
| Framework-level   | JVM-level              |
| Chuẩn bị metadata | Compile thành binary   |
| Sinh code Java    | Sinh native executable |
| Có thể chạy JVM   | Không cần JVM          |

👉 Spring AOT là **điều kiện cần**  
👉 Native Image là **điều kiện đủ**

---

# 6. Ví dụ thực tế: Spring Boot 3 + JWT

### Trước AOT:

* JWT filter tạo proxy runtime
* Reflection decode claims
* Native build fail

### Sau AOT:

* JwtDecoder được register trước
* Reflection được khai báo rõ
* Native build OK

---

# 7. Vì sao Spring bắt buộc phải làm AOT?

Nếu không:

* ❌ Không cạnh tranh được với:

    * Quarkus
    * Micronaut
* ❌ Không chạy serverless tốt
* ❌ Không chạy native

👉 **AOT là sống còn**

---

# 8. Review

> **What is Spring AOT?**

💬 Reply:

> *Spring AOT is a build-time optimization that analyzes Spring application configuration ahead of runtime and generates static code and metadata to reduce reflection, improve startup time, lower memory usage, and enable GraalVM native image support.*

---

# 9. Mental model cần nhớ

> **Spring AOT = biến Spring từ “runtime magic” thành “build-time code”**

---

# Spring AOT và `spring-boot-devtools`
- Gần như là 2 thái cực đối lập**.

> **AOT không những *không* liên quan trực tiếp đến `spring-boot-devtools`, mà còn đi ngược triết lý của devtools.**

Bây giờ phân tích kỹ.

---

## 1. `spring-boot-devtools` là gì?

`spring-boot-devtools` phục vụ **DEV-TIME ONLY**:

* Auto restart khi đổi code
* LiveReload
* ClassLoader tách đôi:

    * **Base ClassLoader** (lib)
    * **Restart ClassLoader** (code app)

👉 Mục tiêu: **đổi code nhanh – reload nhanh**

---

## 2. Spring AOT là gì?

Spring AOT phục vụ **BUILD / RUNTIME OPTIMIZATION**:

* Phân tích app **ở build-time**
* Sinh code tĩnh
* Giảm reflection / dynamic behavior
* Chuẩn bị cho **GraalVM Native Image**

👉 Mục tiêu: **startup nhanh – runtime ổn định**

---

## 3. Vì sao AOT và DevTools “xung đột”?

### 3.1. Triết lý đối nghịch

| DevTools         | AOT                  |
| ---------------- | -------------------- |
| Dynamic          | Static               |
| Runtime reload   | Build-time freeze    |
| ClassLoader hack | Fixed class graph    |
| Reflection OK    | Reflection minimized |

---

### 3.2. DevTools phá assumption của AOT

AOT giả định:

* Bean graph **cố định**
* Proxy structure **biết trước**
* Class không thay đổi

DevTools:

* Thay class liên tục
* Reload context
* Tạo lại proxy runtime

👉 **Không thể AOT hóa DevTools**

---

## 4. Thực tế Spring Boot xử lý thế nào?

### 4.1. DevTools **tự động bị vô hiệu hóa** trong AOT / Native

* Khi:

    * `spring.aot.enabled=true`
    * Hoặc build native image

➡️ Spring Boot:

* ❌ Không load `spring-boot-devtools`
* ❌ Không apply restart mechanism

---

### 4.2. Tại sao?

Vì:

* DevTools dùng reflection
* Dynamic class loading
* Hot restart

👉 Native Image **không cho phép**

---

## 5. Cấu hình chuẩn (best practice)

### 5.1. Dev profile

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-devtools</artifactId>
    <scope>runtime</scope>
    <optional>true</optional>
</dependency>
```

✔ Chỉ dùng khi dev  
✔ Không add vào production jar

---

### 5.2. AOT / Native profile

```properties
spring.aot.enabled=true
```

Hoặc:

```bash
mvn -Pnative native:compile
```

👉 DevTools bị bỏ qua

---

## 6. Một hiểu nhầm phổ biến ❌

> “DevTools giúp AOT reload nhanh hơn?”

❌ Sai hoàn toàn

* DevTools = reload runtime
* AOT = freeze build-time

---

## 7. So sánh nhanh để nhớ

| Tiêu chí       | DevTools       | AOT          |
| -------------- | -------------- | ------------ |
| Phase          | Dev-time       | Build-time   |
| Reload         | Có             | Không        |
| Native support | ❌              | ✅            |
| Reflection     | Nhiều          | Ít           |
| Startup        | Không quan tâm | Rất quan tâm |

---

## 8. Review

> **Is Spring Boot DevTools related to Spring AOT?**

💬 Reply:

> *No. Spring Boot DevTools is a development-time tool focused on fast feedback through automatic restarts and dynamic class reloading, while Spring AOT is a build-time optimization mechanism aimed at reducing runtime dynamism and enabling fast startup and GraalVM native images. In fact, DevTools is disabled when AOT or native image is used.*

---

## 9. Mental model chuẩn

> **DevTools = tốc độ viết code**  
> **AOT = tốc độ chạy code**

---




