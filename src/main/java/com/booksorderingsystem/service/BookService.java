package com.booksorderingsystem.service;

import com.booksorderingsystem.dto.book.BookPageResponse;
import com.booksorderingsystem.dto.book.BookRequest;
import com.booksorderingsystem.dto.book.BookResponse;

public interface BookService {

    BookPageResponse findAllBooks(String keyword, int page);

    BookResponse findBookById(Long bookId);

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(Long bookId, BookRequest request);

    void deleteBook(Long bookId);
}
