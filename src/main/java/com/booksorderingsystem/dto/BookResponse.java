package com.booksorderingsystem.dto;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 此DTO用於回傳書籍查詢結果：
 * - GET /api/books
 * - GET /api/books/{bookId}
 * - POST /api/books
 * - PUT /api/books/{bookId}
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
