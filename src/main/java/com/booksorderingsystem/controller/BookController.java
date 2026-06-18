package com.booksorderingsystem.controller;

import com.booksorderingsystem.dto.BookPageResponse;
import com.booksorderingsystem.dto.BookRequest;
import com.booksorderingsystem.dto.BookResponse;
import com.booksorderingsystem.dto.LoginResponse;
import com.booksorderingsystem.entity.Book;
import com.booksorderingsystem.entity.Role;
import com.booksorderingsystem.service.BookService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
public class BookController {

    private final BookService bookService;

    @GetMapping
    public BookPageResponse getAllBooks(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page
    ) {
        return bookService.findAllBooks(keyword, page);
    }

    @GetMapping("/{bookId}")
    public BookResponse getBookById(@PathVariable Long bookId) {
        return bookService.findBookById(bookId);
    }

    @PostMapping
    public BookResponse createBook(
            @Valid @RequestBody BookRequest request,
            HttpServletRequest httpRequest
    ) {
        checkAdmin(httpRequest);
        return bookService.createBook(request);
    }

    @PutMapping("/{bookId}")
    public BookResponse updateBook(
            @PathVariable Long bookId,
            @Valid @RequestBody BookRequest request,
            HttpServletRequest httpRequest
    ) {
        checkAdmin(httpRequest);
        return bookService.updateBook(bookId, request);
    }

    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteBook(
            @PathVariable Long bookId,
            HttpServletRequest httpRequest
    ) {
        checkAdmin(httpRequest);
        bookService.deleteBook(bookId);
    }

    // ===== Helper methods =====

    private void checkAdmin(HttpServletRequest request) {
        LoginResponse loginUser = getLoginUserFromSession(request);

        if (loginUser.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "權限不足，僅管理者可執行此操作");
        }
    }

    private LoginResponse getLoginUserFromSession(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請先登入");
        }

        Object loginUser = session.getAttribute(AuthController.LOGIN_USER_SESSION_KEY);

        if (!(loginUser instanceof LoginResponse loginResponse)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "請先登入");
        }

        return loginResponse;
    }
}
