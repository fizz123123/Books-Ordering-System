# 圖書訂購系統

當修習新課程時，學生們皆有訂購教科書之需求，而現行流程多由各班班代人工統計數量、經手現金後，再轉交系辦助教向出版商訂購。此傳統模式不僅作業流程繁瑣，且班代與助教需反覆核對並修正訂購明細，除了易衍生人為疏失外，經手現金之做法亦與現行學校所推動的「無現金校園」政策相違背。
為解決上述痛點，開發本系統 - 書庫系統，期望透過數位化管理平台簡化書籍訂購流程、提高資料準確性，並於未來將其逐步擴充為「學生書籍訂購系統」。 

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
