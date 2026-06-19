# 圖書訂購系統

這是一個使用 Spring Boot 構建的簡單圖書訂購系統。

## 功能

*   用戶認證（註冊和登錄）
*   瀏覽和搜索圖書
*   訂購圖書

## 使用的技術

*   Java
*   Spring Boot
*   Spring Data JPA
*   MySQL

## 設定

1.  **下載專案：**
    ```bash
    git clone https://github.com/your-username/books-ordering-system.git
    ```
2.  **配置 database：**
    -   打開 `src/main/resources/application.properties`
    -   更新 `spring.datasource.url`、`spring.datasource.username` 和 `spring.datasource.password` 屬性以匹配 MySQL database。
3.  **執行應用程式：**
    ```bash
    ./mvnw spring-boot:run
    ```
    應用程序將在 `http://localhost:8080` 上可用。

## API 端點

### 認證

*   `POST /api/auth/signup` - 註冊新用戶
*   `POST /api/auth/signin` - 登錄

### 圖書

*   `GET /api/books` - 獲取所有圖書
*   `GET /api/books/{id}` - 按 ID 獲取圖書
*   `POST /api/books` - 創建新圖書
*   `PUT /api/books/{id}` - 更新圖書
*   `DELETE /api/books/{id}` - 刪除圖書

### 訂單

*   `POST /api/orders` - 下新訂單
*   `GET /api/orders` - 獲取已認證用戶的所有訂單