package com.booksorderingsystem.dto.book;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 此DTO用於新增、修改書籍：
 * - POST /api/books
 * - PUT /api/books/{bookId}
 */

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookRequest {

    @NotBlank(message = "書名不可為空")
    private String title;

    @NotBlank(message = "作者不可為空")
    private String author;

    @NotBlank(message = "出版社不可為空")
    private String publisher;

    @NotBlank(message = "ISBN 不可為空")
    private String isbn;

    @NotNull(message = "價格不可為空")
    @Min(value = 0, message = "價格不可小於 0")
    private Integer price;

    @Size(max = 500, message = "出版社書本連結不可超過 500 字")
    private String publisherBookUrl;
}
