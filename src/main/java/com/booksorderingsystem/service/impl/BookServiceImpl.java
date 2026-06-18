package com.booksorderingsystem.service.impl;

import com.booksorderingsystem.dto.BookPageResponse;
import com.booksorderingsystem.dto.BookRequest;
import com.booksorderingsystem.dto.BookResponse;
import com.booksorderingsystem.entity.Book;
import com.booksorderingsystem.repository.BookRepository;
import com.booksorderingsystem.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final int PAGE_SIZE = 10;

    private final BookRepository bookRepository;

    @Override
    public BookPageResponse findAllBooks(String keyword, int page) {
        int currentPage = Math.max(page, 1);

        Pageable pageable = PageRequest.of(
                currentPage - 1,
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "bookId")
        );

        Page<Book> bookPage;

        if (StringUtils.hasText(keyword)) {
            bookPage = bookRepository.searchByKeyword(keyword.trim(), pageable);
        } else {
            bookPage = bookRepository.findAll(pageable);
        }

        List<BookResponse> content = bookPage.getContent()
                .stream()
                .map(this::toBookResponse)
                .toList();

        return BookPageResponse.builder()
                .content(content)
                .currentPage(currentPage)
                .pageSize(PAGE_SIZE)
                .totalElements(bookPage.getTotalElements())
                .totalPages(bookPage.getTotalPages())
                .first(bookPage.isFirst())
                .last(bookPage.isLast())
                .build();
    }

    @Override
    public BookResponse findBookById(Long bookId) {
        Book book = findBookEntityById(bookId);
        return toBookResponse(book);
    }

    @Override
    public BookResponse createBook(BookRequest request) {
        validateBookRequest(request);

        if (bookRepository.existsByIsbn(request.getIsbn())) {
            throw new IllegalArgumentException("ISBN已存在，無法新增重複書籍");
        }

        Book book = Book.builder()
                .title(request.getTitle().trim())
                .author(request.getAuthor().trim())
                .publisher(request.getPublisher().trim())
                .isbn(request.getIsbn().trim())
                .price(request.getPrice())
                .publisherBookUrl(normalizeOptionalText(request.getPublisherBookUrl()))
                .build();

        Book savedBook = bookRepository.save(book);
        return toBookResponse(savedBook);
    }

    @Override
    public BookResponse updateBook(Long bookId, BookRequest request) {
        validateBookRequest(request);

        Book book = findBookEntityById(bookId);

        if (bookRepository.existsByIsbnAndBookIdNot(request.getIsbn(), bookId)) {
            throw new IllegalArgumentException("ISBN已存在，無法修改為重複的ISBN");
        }

        book.setTitle(request.getTitle().trim());
        book.setAuthor(request.getAuthor().trim());
        book.setPublisher(request.getPublisher().trim());
        book.setIsbn(request.getIsbn().trim());
        book.setPrice(request.getPrice());
        book.setPublisherBookUrl(normalizeOptionalText(request.getPublisherBookUrl()));

        Book updatedBook = bookRepository.save(book);
        return toBookResponse(updatedBook);
    }

    @Override
    public void deleteBook(Long bookId) {
        Book book = findBookEntityById(bookId);
        bookRepository.delete(book);
    }

    //===== Helper methods =====

    private Book findBookEntityById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "查無此書籍"));
    }

    private void validateBookRequest(BookRequest request) {
        if (!StringUtils.hasText(request.getTitle())) {
            throw new IllegalArgumentException("書名不可為空");
        }

        if (!StringUtils.hasText(request.getAuthor())) {
            throw new IllegalArgumentException("作者不可為空");
        }

        if (!StringUtils.hasText(request.getPublisher())) {
            throw new IllegalArgumentException("出版社不可為空");
        }

        if (!StringUtils.hasText(request.getIsbn())) {
            throw new IllegalArgumentException("ISBN不可為空");
        }

        if (request.getPrice() == null || request.getPrice() < 0) {
            throw new IllegalArgumentException("價格不可為空或小於0");
        }
    }

    private BookResponse toBookResponse(Book book) {
        return BookResponse.builder()
                .bookId(book.getBookId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .price(book.getPrice())
                .publisherBookUrl(book.getPublisherBookUrl())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }

    private String normalizeOptionalText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
