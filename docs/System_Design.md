# 書庫系統 系統設計文件

## 1. 專案摘要

本專案目前定位為「學生書籍訂購系統」的前置核心模組，主要功能為書籍基本資料管理，系統提供管理者維護書籍資料，並讓一般使用者查詢書籍資訊

---

## 2. Tech Stack

| 類別 | 技術 |
|---|---|
| 後端 | Spring Boot、Java |
| 前端 | HTML、CSS、JavaScript |
| 資料庫 | MySQL |
| 資料存取 | Spring Data JPA |


---

## 3. 專案整體架構

### 3.1 系統架構圖

```mermaid
flowchart TD
    A[使用者 / 管理者] --> B[前端靜態頁面<br>HTML / CSS / JavaScript]
    B -->|fetch API| C[Controller]

    C -->|Request DTO| D[Service Interface]
    D --> E[ServiceImpl<br>業務邏輯 / 分頁處理 / Entity-DTO 轉換]

    E -->|Entity| F[Repository<br>Spring Data JPA]
    F --> G[(MySQL Database)]

    E -->|Response DTO / PageResponse| C
    C -->|JSON Response| B
```

---

### 3.2 專案資料夾架構

```text
Books-Ordering-System
├── src
│   └── main
│       ├── java
│       │   └── com.booksorderingsystem
│       │       ├── BooksOrderingSystemApplication.java
│       │       │
│       │       ├── controller
│       │       │   ├── BookController.java
│       │       │   └── AuthController.java
│       │       │
│       │       ├── service
│       │       │   ├── BookService.java
│       │       │   ├── AuthService.java
│       │       │   └── impl
│       │       │       ├── BookServiceImpl.java
│       │       │       └── AuthServiceImpl.java
│       │       │
│       │       ├── repository
│       │       │   ├── BookRepository.java
│       │       │   └── UserRepository.java
│       │       │
│       │       ├── entity
│       │       │   ├── Book.java
│       │       │   ├── User.java
│       │       │   └── Role.java
│       │       │
│       │       ├── dto
│       │       │   ├── BookRequest.java
│       │       │   ├── BookResponse.java
│       │       │   ├── BookPageResponse.java
│       │       │   ├── LoginRequest.java
│       │       │   └── LoginResponse.java
│       │       │
│       │       └── exception
│       │           └── GlobalExceptionHandler.java
│       │
│       └── resources
│           ├── static
│           │   ├── login.html
│           │   ├── index.html
│           │   ├── css
│           │   │   └── style.css
│           │   └── js
│           │       ├── auth.js
│           │       └── book.js
│           │
│           └── application.properties
│
└── pom.xml
```

---

## 4. Service Layer 設計說明

本專案的 Service Layer 採用 "介面與實作分離" 的設計方式

Controller 依賴 Service 介面，而非直接依賴實作類別；實際業務邏輯則由 ServiceImpl 類別負責處理。此設計可降低模組之間的耦合度，並提高後續維護、測試與擴充的彈性

```text
BookController → BookService interface → BookServiceImpl → BookRepository
AuthController → AuthService interface → AuthServiceImpl → UserRepository
```

---

## 5. 資料庫設計

### 5.1 books table

| 欄位名稱               | 型別           | Constraint         | 說明      |
| ------------------ | ------------ | ------------------ | ------- |
| book_id            | BIGINT       | PK, AUTO_INCREMENT | 書籍編號    |
| title              | VARCHAR(100) | NOT NULL           | 書名      |
| author             | VARCHAR(100) | NOT NULL           | 作者      |
| publisher          | VARCHAR(100) | NOT NULL           | 出版社     |
| isbn               | VARCHAR(20)  | NOT NULL, UNIQUE   | ISBN    |
| price              | INT          | NOT NULL           | 價格      |
| publisher_book_url | VARCHAR(500) | NULL               | 出版社書本連結 |
| created_at         | DATETIME     | NOT NULL           | 建立時間    |
| updated_at         | DATETIME     | NOT NULL           | 更新時間    |


`publisher_book_url` 為選填欄位，因為部分書籍可能尚未提供出版社連結，或助教新增資料時尚未取得該網址

---

### 5.2 users table

| 欄位名稱       | 型別           | Constraint         | 說明                     |
| ---------- | ------------ | ------------------ |------------------------|
| user_id    | BIGINT       | PK, AUTO_INCREMENT | 使用者編號                  |
| username   | VARCHAR(50)  | NOT NULL, UNIQUE   | 使用者帳號                  |
| password   | VARCHAR(255) | NOT NULL           | 使用者密碼                  |
| role       | ENUM('ADMIN', 'USER')  | NOT NULL           | 使用者角色 (`ADMIN`、`USER`) |
| created_at | DATETIME     | NOT NULL           | 建立時間                   |
| updated_at | DATETIME     | NOT NULL           | 更新時間                   |

---

## 6. Entity 設計

### 6.1 Book Entity

```java
public class Book {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private Integer price;
    private String publisherBookUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### 6.2 User Entity

```java
public class User {
    private Long id;
    private String username;
    private String password;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### 6.3 Role Enum

```java
public enum Role {
    ADMIN,
    USER
}
```

---

## 7. 權限設計

| 角色 | 可使用功能 |
|---|---|
| USER | 查詢書籍、搜尋書籍 |
| ADMIN | 查詢書籍、搜尋書籍、新增書籍、修改書籍、刪除書籍 |

使用簡易登入機制進行角色判斷，登入成功後，前端根據使用者角色決定是否顯示新增、修改、刪除按鈕，後端在新增、修改、刪除 API 中也會檢查目前登入者是否為 ADMIN，避免一般使用者直接呼叫 API 操作資料

---

## 8. DTO 設計

### 8.1 LoginRequest

```java
public class LoginRequest {
    private String username;
    private String password;
}
```

---

### 8.2 LoginResponse

```java
public class LoginResponse {
    private Long userId;
    private String username;
    private Role role;
}
```

---

### 8.3 BookRequest

新增與修改書籍時使用

```java
public class BookRequest {
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private Integer price;
    private String publisherBookUrl;
}
```

---

### 8.4 BookResponse

查詢書籍時回傳給前端使用

```java
public class BookResponse {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String isbn;
    private Integer price;
    private String publisherBookUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

---

### 8.5 BookPageResponse

查詢書籍列表時使用，回傳當頁書籍資料與分頁資訊

```java
public class BookPageResponse {
    private List<BookResponse> content;
    private int currentPage;
    private int pageSize;
    private long totalElements;
    private int totalPages;
    private boolean first;
    private boolean last;
}
```

---

## 9. API 說明文件

API Base URL：

```text
http://localhost:8080/api
```

---

## 9.1 Auth API

### 9.1.1 使用者登入

```http
POST /api/auth/login
```

#### 功能說明

使用者輸入帳號與密碼後，系統確認身份並回傳使用者角色。

#### Request Body

```json
{
  "username": "admin",
  "password": "1234"
}
```

#### Response Body

```json
{
  "userId": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

#### 錯誤情況

| 狀態碼 | 說明 |
|---|---|
| 400 | 帳號或密碼不得為空、帳號或密碼錯誤 |


---

### 9.1.2 取得目前登入者資訊

```http
GET /api/auth/me
```

#### 功能說明

取得目前登入使用者的基本資訊。

#### Response Body

```json
{
  "userId": 1,
  "username": "admin",
  "role": "ADMIN"
}
```

---

### 9.1.3 使用者登出

```http
POST /api/auth/logout
```

#### 功能說明

登出目前使用者，系統會使目前登入 Session 失效。登出成功後，前端可清除使用者登入狀態並導回登入頁面。

#### 成功回應
204 No Content

---

## 9.2 Book API

### 9.2.1 查詢書籍列表（含分頁）

```http
GET /api/books?page=1
GET /api/books?keyword=software&page=1
```

#### 功能說明

取得書籍列表資料，每頁固定顯示 10 筆資料，若未提供 `page` 參數，預設查詢第 1 頁。

#### Query Parameters

| 參數 | 是否必填 | 說明 |
|---|---|---|
| keyword | 否 | 可搜尋書名、作者、出版社或 ISBN |
| page | 否 | 頁碼，從 1 開始，預設為 1 |

#### 分頁規則

| 規則 | 說明 |
|---|---|
| 每頁筆數 | 固定 10 筆 |
| 頁碼起始 | `page=1` 表示第 1 頁 |
| 未傳入 page | 預設查詢第 1 頁 |
| 排序方式 | 可依實作設定，例如依 `bookId` 由新到舊排序 |

#### Response Body

```json
{
  "content": [
    {
      "bookId": 1,
      "title": "Software Engineering",
      "author": "Ian Sommerville",
      "publisher": "Pearson",
      "isbn": "9780137035151",
      "price": 850,
      "publisherBookUrl": "https://www.pearson.com/example-book",
      "createdAt": "2026-06-11T10:00:00",
      "updatedAt": "2026-06-11T10:00:00"
    }
  ],
  "currentPage": 1,
  "pageSize": 10,
  "totalElements": 25,
  "totalPages": 3,
  "first": true,
  "last": false
}
```

---

### 9.2.2 查詢單本書籍

```http
GET /api/books/{bookId}
```

#### 功能說明

根據 `bookId` 查詢單本書籍詳細資料。

#### Response Body

```json
{
  "bookId": 1,
  "title": "Software Engineering",
  "author": "Ian Sommerville",
  "publisher": "Pearson",
  "isbn": "9780137035151",
  "price": 850,
  "publisherBookUrl": "https://www.pearson.com/example-book",
  "createdAt": "2026-06-11T10:00:00",
  "updatedAt": "2026-06-11T10:00:00"
}
```

#### 錯誤情況

| 狀態碼 | 說明 |
|---|---|
| 404 | 查無此書籍 |

---

### 9.2.3 新增書籍

```http
POST /api/books
```

#### 權限

僅 `ADMIN` 可使用。

#### 功能說明

新增一本書籍資料。管理者可在新增時填入出版社書本連結，方便使用者查詢書籍時直接前往出版社頁面查看完整資訊。

#### Request Body

```json
{
  "title": "Software Engineering",
  "author": "Ian Sommerville",
  "publisher": "Pearson",
  "isbn": "9780137035151",
  "price": 850,
  "publisherBookUrl": "https://www.pearson.com/example-book"
}
```

#### Response Body

```json
{
  "bookId": 1,
  "title": "Software Engineering",
  "author": "Ian Sommerville",
  "publisher": "Pearson",
  "isbn": "9780137035151",
  "price": 850,
  "publisherBookUrl": "https://www.pearson.com/example-book",
  "createdAt": "2026-06-11T10:00:00",
  "updatedAt": "2026-06-11T10:00:00"
}
```

#### 驗證規則

| 欄位 | 規則 |
|---|---|
| title | 不可為空 |
| author | 不可為空 |
| publisher | 不可為空 |
| isbn | 不可為空，建議不可重複 |
| price | 不可小於 0 |
| publisherBookUrl | 選填，建議限制長度 500 字以內 |

---

### 9.2.4 修改書籍

```http
PUT /api/books/{bookId}
```

#### 權限

僅 `ADMIN` 可使用。

#### 功能說明

根據 `bookId` 修改書籍資料，可更新書名、作者、出版社、ISBN、價格與出版社書本連結。

#### Request Body

```json
{
  "title": "Software Engineering 10th Edition",
  "author": "Ian Sommerville",
  "publisher": "Pearson",
  "isbn": "9780137035151",
  "price": 900,
  "publisherBookUrl": "https://www.pearson.com/example-book-10e"
}
```

#### Response Body

```json
{
  "bookId": 1,
  "title": "Software Engineering 10th Edition",
  "author": "Ian Sommerville",
  "publisher": "Pearson",
  "isbn": "9780137035151",
  "price": 900,
  "publisherBookUrl": "https://www.pearson.com/example-book-10e",
  "createdAt": "2026-06-11T10:00:00",
  "updatedAt": "2026-06-11T10:30:00"
}
```

#### 錯誤情況

| 狀態碼 | 說明 |
|---|---|
| 400 | 輸入資料格式錯誤 |
| 403 | 權限不足 |
| 404 | 查無此書籍 |

---

### 9.2.5 刪除書籍

```http
DELETE /api/books/{bookId}
```

#### 權限

僅 `ADMIN` 可使用。

#### 功能說明

根據 bookId 刪除指定書籍資料。刪除成功後，該書籍資料將從資料庫中移除。

#### 成功回應
204 No Content

#### 錯誤情況

| 狀態碼 | 說明    |
|-----|-------|
| 401 | 尚未登入  |
| 403 | 權限不足  |
| 404 | 查無此書籍 |

---



# 10. 高階 BCE 類別圖

本圖用於需求分析階段，主要呈現系統中的 Boundary、Control、Entity 之間的關係。

Boundary 負責與使用者或外部系統互動，Control 負責處理系統流程與業務邏輯，Entity 則代表系統中的核心資料物件。

```mermaid
classDiagram
    direction LR

    class LoginPage {
        <<Boundary>>
        使用者登入畫面
    }

    class BookManagementPage {
        <<Boundary>>
        書籍查詢、分頁與管理畫面
    }

    class AuthController {
        <<Boundary>>
        處理登入相關請求
    }

    class BookController {
        <<Boundary>>
        處理書籍查詢、分頁與管理相關請求
    }

    class AuthService {
        <<Control>>
        驗證使用者身份
        判斷使用者角色
    }

    class BookService {
        <<Control>>
        處理書籍新增、分頁查詢、修改、刪除
        驗證書籍資料
    }

    class User {
        <<Entity>>
        使用者資料
    }

    class Role {
        <<Entity>>
        使用者角色
    }

    class Book {
        <<Entity>>
        書籍資料
        出版社書本連結
    }

    LoginPage --> AuthController
    BookManagementPage --> BookController

    AuthController --> AuthService
    BookController --> BookService

    AuthService --> User
    User --> Role

    BookService --> Book
```

---

# 11. 系統設計類別圖

本圖用於系統設計階段，主要呈現 Spring Boot 專案中的實際 class 設計。

此圖以實作架構為主，包含 Controller、Service interface、ServiceImpl、Repository、Entity 與 DTO。

```mermaid
classDiagram
    direction LR

    class AuthController {
        +login(LoginRequest request) LoginResponse
        +getCurrentUser() LoginResponse
        +logout() void
    }

    class BookController {
        +getAllBooks(String keyword, int page) BookPageResponse
        +getBookById(Long bookId) BookResponse
        +createBook(BookRequest request) BookResponse
        +updateBook(Long bookId, BookRequest request) BookResponse
        +deleteBook(Long bookId) void
    }

    class AuthService {
        <<interface>>
        +login(LoginRequest request) LoginResponse
    }

    class BookService {
        <<interface>>
        +findAllBooks(String keyword, int page) BookPageResponse
        +findBookById(Long bookId) BookResponse
        +createBook(BookRequest request) BookResponse
        +updateBook(Long bookId, BookRequest request) BookResponse
        +deleteBook(Long bookId) void
    }

    class AuthServiceImpl {
        +login(LoginRequest request) LoginResponse
    }

    class BookServiceImpl {
        +findAllBooks(String keyword, int page) BookPageResponse
        +findBookById(Long bookId) BookResponse
        +createBook(BookRequest request) BookResponse
        +updateBook(Long bookId, BookRequest request) BookResponse
        +deleteBook(Long bookId) void
        -validateBookRequest(BookRequest request) void
        -normalizeOptionalText(String value) String
        -toBookResponse(Book book) BookResponse
    }

    class UserRepository {
        +findByUsername(String username) Optional~User~
        +existsByUsername(String username) boolean
    }

    class BookRepository {
        +findAll(Pageable pageable) Page~Book~
        +findById(Long bookId) Optional~Book~
        +save(Book book) Book
        +delete(Book book) void
        +existsByIsbn(String isbn) boolean
        +existsByIsbnAndBookIdNot(String isbn, Long bookId) boolean
        +searchByKeyword(String keyword, Pageable pageable) Page~Book~
    }

    class User {
        -Long id
        -String username
        -String password
        -Role role
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    class Book {
        -Long bookId
        -String title
        -String author
        -String publisher
        -String isbn
        -Integer price
        -String publisherBookUrl
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    class Role {
        <<enumeration>>
        ADMIN
        USER
    }

    class LoginRequest {
        -String username
        -String password
    }

    class LoginResponse {
        -Long userId
        -String username
        -Role role
    }

    class BookRequest {
        -String title
        -String author
        -String publisher
        -String isbn
        -Integer price
        -String publisherBookUrl
    }

    class BookResponse {
        -Long bookId
        -String title
        -String author
        -String publisher
        -String isbn
        -Integer price
        -String publisherBookUrl
        -LocalDateTime createdAt
        -LocalDateTime updatedAt
    }

    class BookPageResponse {
        -List~BookResponse~ content
        -int currentPage
        -int pageSize
        -long totalElements
        -int totalPages
        -boolean first
        -boolean last
    }

    AuthController --> AuthService
    BookController --> BookService

    AuthServiceImpl ..|> AuthService
    BookServiceImpl ..|> BookService

    AuthServiceImpl --> UserRepository
    BookServiceImpl --> BookRepository

    AuthController ..> LoginRequest
    AuthController ..> LoginResponse
    BookController ..> BookRequest
    BookController ..> BookResponse
    BookController ..> BookPageResponse

    AuthServiceImpl ..> LoginResponse
    BookServiceImpl ..> BookRequest
    BookServiceImpl ..> BookResponse
    BookServiceImpl ..> BookPageResponse

    UserRepository --> User
    BookRepository --> Book
    User --> Role
```

---

## 12. UI 流程設計

### 12.1 整體 UI 流程

```mermaid
flowchart TD
    A[進入 login.html] --> B[輸入帳號密碼]
    B --> C{登入是否成功}

    C -->|否| D[顯示錯誤訊息]
    D --> B

    C -->|是| E[進入 index.html 書籍列表頁]
    E --> F[取得目前使用者角色]
    F --> G{角色判斷}

    G -->|USER| H[顯示查詢、搜尋、分頁與出版社連結]
    G -->|ADMIN| I[顯示查詢、搜尋、分頁、新增、修改、刪除與出版社連結]

    H --> J[查詢書籍列表]
    H --> K[搜尋書籍]
    H --> O[切換分頁]
    H --> P[點擊出版社書本連結]

    I --> J
    I --> K
    I --> O
    I --> P
    I --> L[新增書籍]
    I --> M[修改書籍]
    I --> N[刪除書籍]
```

---

### 12.2 登入頁面 login.html

#### 頁面功能

| 功能 | 說明 |
|---|---|
| 輸入帳號 | 使用者輸入 username |
| 輸入密碼 | 使用者輸入 password |
| 登入按鈕 | 呼叫 `POST /api/auth/login` |
| 錯誤訊息 | 若登入失敗，顯示帳號或密碼錯誤 |

#### 登入成功後

```text
login.html → index.html
```

---

### 12.3 書籍列表頁 index.html

#### USER 角色可看到的功能

| 功能 | 說明 |
|---|---|
| 查詢書籍列表 | 呼叫 `GET /api/books?page=1` |
| 搜尋書籍 | 呼叫 `GET /api/books?keyword=...&page=1` |
| 切換分頁 | 透過 `page` 參數取得不同頁面的書籍資料 |
| 查看出版社書本連結 | 點擊書籍資料中的出版社連結，前往出版社頁面 |
| 查看書籍詳細資料 | 呼叫 `GET /api/books/{bookId}` |

---

#### ADMIN 角色可看到的功能

| 功能 | 說明 |
|---|---|
| 查詢書籍列表 | 呼叫 `GET /api/books?page=1` |
| 搜尋書籍 | 呼叫 `GET /api/books?keyword=...&page=1` |
| 切換分頁 | 透過 `page` 參數取得不同頁面的書籍資料 |
| 新增書籍 | 呼叫 `POST /api/books`，可填入出版社書本連結 |
| 修改書籍 | 呼叫 `PUT /api/books/{bookId}`，可更新出版社書本連結 |
| 刪除書籍 | 呼叫 `DELETE /api/books/{bookId}` |

---

## 13. 前端與 API 串接流程

### 13.1 查詢書籍流程（含分頁）

```mermaid
sequenceDiagram
    actor User as 使用者
    participant Page as index.html
    participant JS as book.js
    participant Controller as BookController
    participant Service as BookService
    participant Impl as BookServiceImpl
    participant Repository as BookRepository
    participant DB as MySQL

    User->>Page: 開啟書籍列表頁或切換分頁
    Page->>JS: 載入 book.js
    JS->>Controller: GET /api/books?keyword=...&page=1
    Controller->>Service: findAllBooks(keyword, page)
    Service->>Impl: 呼叫實作邏輯
    Impl->>Repository: searchByKeyword(keyword, pageable) 或 findAll(pageable)
    Repository->>DB: SELECT * FROM books LIMIT 10 OFFSET ...
    DB-->>Repository: page books data
    Repository-->>Impl: Page<Book>
    Impl-->>Service: BookPageResponse
    Service-->>Controller: BookPageResponse
    Controller-->>JS: JSON response with content and page info
    JS-->>Page: 渲染書籍列表與分頁按鈕
```

---

### 13.2 新增書籍流程

```mermaid
sequenceDiagram
    actor Admin as 管理者
    participant Page as index.html
    participant JS as book.js
    participant Controller as BookController
    participant Service as BookService
    participant Impl as BookServiceImpl
    participant Repository as BookRepository
    participant DB as MySQL

    Admin->>Page: 填寫新增書籍表單，包含出版社書本連結
    Page->>JS: 點擊新增按鈕
    JS->>Controller: POST /api/books
    Controller->>Service: createBook(BookRequest)
    Service->>Impl: 呼叫實作邏輯
    Impl->>Impl: validateBookRequest()
    Impl->>Repository: save(Book)
    Repository->>DB: INSERT INTO books
    DB-->>Repository: saved book
    Repository-->>Impl: Book
    Impl-->>Service: BookResponse
    Service-->>Controller: BookResponse
    Controller-->>JS: JSON response
    JS-->>Page: 更新書籍列表
```

---

### 13.3 修改書籍流程

```mermaid
sequenceDiagram
    actor Admin as 管理者
    participant Page as index.html
    participant JS as book.js
    participant Controller as BookController
    participant Service as BookService
    participant Impl as BookServiceImpl
    participant Repository as BookRepository
    participant DB as MySQL

    Admin->>Page: 點擊修改書籍
    Page->>JS: 將書籍資料帶入表單，包含出版社書本連結
    Admin->>Page: 修改書籍資料並送出
    JS->>Controller: PUT /api/books/{bookId}
    Controller->>Service: updateBook(bookId, BookRequest)
    Service->>Impl: 呼叫實作邏輯
    Impl->>Repository: findById(bookId)
    Repository->>DB: SELECT * FROM books WHERE book_id = ?
    DB-->>Repository: book data
    Repository-->>Impl: Book
    Impl->>Impl: 更新 Book 欄位
    Impl->>Repository: save(Book)
    Repository->>DB: UPDATE books
    Repository-->>Impl: updated Book
    Impl-->>Service: BookResponse
    Service-->>Controller: BookResponse
    Controller-->>JS: JSON response
    JS-->>Page: 更新書籍列表
```

---

### 13.4 刪除書籍流程

```mermaid
sequenceDiagram
    actor Admin as 管理者
    participant Page as index.html
    participant JS as book.js
    participant Controller as BookController
    participant Service as BookService
    participant Impl as BookServiceImpl
    participant Repository as BookRepository
    participant DB as MySQL

    Admin->>Page: 點擊刪除按鈕
    Page->>JS: 確認是否刪除
    JS->>Controller: DELETE /api/books/{bookId}
    Controller->>Service: deleteBook(bookId)
    Service->>Impl: 呼叫實作邏輯
    Impl->>Repository: findById(bookId)
    Repository->>DB: SELECT * FROM books WHERE book_id = ?
    DB-->>Repository: book data
    Repository-->>Impl: Book
    Impl->>Repository: delete(Book)
    Repository->>DB: DELETE FROM books WHERE book_id = ?
    Impl-->>Service: 刪除完成
    Service-->>Controller: 刪除完成
    Controller-->>JS: 刪除成功訊息
    JS-->>Page: 從列表移除該書籍
```

---

## 14. 主要功能與分工

| 模組 | 主要內容 |
|---|---|
| 前端 UI | `login.html`、`index.html`、`style.css` |
| 前端 API 串接 | `auth.js`、`book.js`，包含登入、書籍 CRUD、搜尋與分頁查詢 |
| 後端 Controller | `AuthController`、`BookController` |
| 後端 Service 介面 | `AuthService`、`BookService` |
| 後端 Service 實作 | `AuthServiceImpl`、`BookServiceImpl`，包含書籍驗證、出版社連結處理與分頁查詢邏輯 |
| 資料庫與 Entity | `User`、`Role`、`Book`、`UserRepository`、`BookRepository` |
| 文件與 UML | 高階 BCE 類別圖、系統設計類別圖、循序圖、API 文件、UI 流程圖 |
| 測試 | 單元測試、API 測試、系統測試|

---

## 15. 開發順序建議

### Step 1：建立資料庫與 Entity

先完成：

```text
Book
User
Role
BookRepository
UserRepository
```

Book 需包含：

```text
bookId、title、author、publisher、isbn、price、publisherBookUrl、createdAt、updatedAt
```

---

### Step 2：完成 Service Interface 與 ServiceImpl

完成：

```text
BookService
BookServiceImpl
AuthService
AuthServiceImpl
```

BookService 查詢方法需支援：

```text
findAllBooks(String keyword, int page)
```

---

### Step 3：完成 Book CRUD API 與分頁查詢

完成：

```text
GET /api/books?page=1
GET /api/books?keyword=...&page=1
GET /api/books/{bookId}
POST /api/books
PUT /api/books/{bookId}
DELETE /api/books/{bookId}
```

其中 `GET /api/books` 每頁固定回傳 10 筆資料，並包含分頁資訊。

---

### Step 4：完成登入與角色判斷

完成：

```text
POST /api/auth/login
GET /api/auth/me
POST /api/auth/logout
```

並讓前端能根據角色顯示不同功能。

---

### Step 5：完成前端頁面與 API 串接

完成：

```text
login.html
index.html
style.css
auth.js
book.js
```

---

### Step 6：整合測試與 Demo

測試情境包含：

| 測試項目 | 預期結果 |
|---|---|
| 使用 ADMIN 登入 | 可以新增、修改、刪除、查詢書籍 |
| 使用 USER 登入 | 只能查詢書籍 |
| 新增空白書名 | 系統顯示錯誤 |
| 新增價格小於 0 的書籍 | 系統顯示錯誤 |
| 新增出版社書本連結 | 書籍資料成功儲存並於列表或詳細資料中顯示連結 |
| 修改出版社書本連結 | 更新成功後前端顯示新的出版社連結 |
| 查詢第 1 頁書籍 | 系統回傳最多 10 筆資料與分頁資訊 |
| 切換到第 2 頁 | 系統回傳第 2 頁書籍資料 |
| 搜尋關鍵字並分頁 | 系統只顯示符合條件的書籍，且仍維持每頁 10 筆 |
| 修改不存在的 bookId | 系統回傳查無資料 |
| 刪除書籍 | 書籍從列表中移除 |

---

## 16. Demo 用測試帳號

| 帳號 | 密碼 | 角色 |
|---|---|---|
| admin | 1234 | ADMIN |
| user | 1234 | USER |

---

## 17. 測試規劃
| 測試類型   | 測試方式    | 測試內容                                                |
| ------ | ------- |-----------------------------------------------------|
| 單元測試   | JUnit + Mockito | 測試 BookServiceImpl 的新增、修改、查詢與錯誤處理                   |
| API 測試 | Postman | 測試 Controller → Service → Repository → MySQL 是否正確合作 |
| 系統測試   | 瀏覽器操作前端頁面 | 測試登入、權限、分頁、搜尋、CRUD 與資料庫變化                           |

---

## 18. 專案範圍說明

本次期末專題以「書庫系統」作為主要實作範圍，著重於書籍基本資料管理、查詢、分頁瀏覽與角色權限控制。

本系統目前管理的書籍資料包含書名、作者、出版社、ISBN、價格與出版社書本連結，不包含庫存、課程、學期、訂單與付款功能。出版社書本連結用於讓助教或管理者在建立書籍資料時，可放入出版社提供的書籍介紹或購買頁面，方便使用者查詢後取得更多書籍資訊。

未來若要擴充為完整的「學生書籍訂購系統」，可再加入訂購紀錄、學生資料、課程資料、庫存數量與付款狀態等資料表。

未來可擴充功能包含：

| 擴充功能 | 說明 |
|---|---|
| 課程書籍對應 | 將書籍與課程建立關聯 |
| 學生訂購書籍 | 學生可選擇書籍並送出訂單 |
| 訂單管理 | 管理者可查看各書籍訂購數量 |
| 庫存管理 | 紀錄書籍庫存數量與缺書狀態 |
| 線上付款 | 串接校園無現金付款機制 |