package com.booksorderingsystem.service;

import com.booksorderingsystem.dto.BookPageResponse;
import com.booksorderingsystem.dto.BookRequest;
import com.booksorderingsystem.dto.BookResponse;

import java.util.List;

public interface BookService {

    BookPageResponse findAllBooks(String keyword, int page);

    BookResponse findBookById(Long bookId);

    BookResponse createBook(BookRequest request);

    BookResponse updateBook(Long bookId, BookRequest request);

    void deleteBook(Long bookId);
}
