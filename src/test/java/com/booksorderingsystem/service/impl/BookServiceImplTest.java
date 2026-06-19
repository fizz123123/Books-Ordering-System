package com.booksorderingsystem.service.impl;

import com.booksorderingsystem.dto.BookPageResponse;
import com.booksorderingsystem.dto.BookRequest;
import com.booksorderingsystem.dto.BookResponse;
import com.booksorderingsystem.entity.Book;
import com.booksorderingsystem.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookServiceImpl bookService;

    private Book book;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        book = Book.builder()
                .bookId(1L)
                .title("Software Engineering")
                .author("Ian Sommerville")
                .publisher("Pearson")
                .isbn("9780137035151")
                .price(850)
                .publisherBookUrl("https://www.pearson.com/example-book")
                .build();

        bookRequest = new BookRequest(
                "Software Engineering",
                "Ian Sommerville",
                "Pearson",
                "9780137035151",
                850,
                "https://www.pearson.com/example-book"
        );
    }

    @Test
    void findAllBooks_shouldReturnBookPageResponse_whenKeywordIsEmpty() {
        //Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        PageImpl<Book> bookPage = new PageImpl<>(List.of(book), pageRequest, 1);

        when(bookRepository.findAll(any(Pageable.class)))
                .thenReturn(bookPage);

        //Act
        BookPageResponse response = bookService.findAllBooks(null, 1);

        //Assert
        assertNotNull(response);
        assertEquals(1, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertTrue(response.isFirst());
        assertTrue(response.isLast());

        assertEquals(1, response.getContent().size());
        assertEquals("Software Engineering", response.getContent().get(0).getTitle());

        verify(bookRepository).findAll(any(Pageable.class));
        verify(bookRepository, never()).searchByKeyword(anyString(), any(Pageable.class));
    }

    @Test
    void findAllBooks_shouldSearchByKeyword_whenKeywordIsProvided() {
        //Arrange
        PageRequest pageRequest = PageRequest.of(0, 10);
        PageImpl<Book> bookPage = new PageImpl<>(List.of(book), pageRequest, 1);

        when(bookRepository.searchByKeyword(eq("Software"), any(Pageable.class)))
                .thenReturn(bookPage);

        //Act
        BookPageResponse response = bookService.findAllBooks(" Software ", 1);

        //Assert
        assertNotNull(response);
        assertEquals(1, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getContent().size());
        assertEquals("Software Engineering", response.getContent().get(0).getTitle());

        verify(bookRepository).searchByKeyword(eq("Software"), any(Pageable.class));
        verify(bookRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void createBook_shouldReturnBookResponse_whenRequestIsValidAndIsbnNotExists() {
        //Arrange
        BookRequest request = new BookRequest(
                " Software Engineering ",
                " Ian Sommerville ",
                " Pearson ",
                "9780137035151",
                850,
                " https://www.pearson.com/example-book "
        );

        when(bookRepository.existsByIsbn(request.getIsbn()))
                .thenReturn(false);

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> {
                    Book savedBook = invocation.getArgument(0);
                    savedBook.setBookId(1L);
                    return savedBook;
                });

        //Act
        BookResponse response = bookService.createBook(request);

        //Assert
        assertNotNull(response);
        assertEquals(1L, response.getBookId());
        assertEquals("Software Engineering", response.getTitle());
        assertEquals("Ian Sommerville", response.getAuthor());
        assertEquals("Pearson", response.getPublisher());
        assertEquals("9780137035151", response.getIsbn());
        assertEquals(850, response.getPrice());
        assertEquals("https://www.pearson.com/example-book", response.getPublisherBookUrl());

        ArgumentCaptor<Book> bookCaptor = ArgumentCaptor.forClass(Book.class);
        verify(bookRepository).save(bookCaptor.capture());

        Book capturedBook = bookCaptor.getValue();
        assertEquals("Software Engineering", capturedBook.getTitle());
        assertEquals("Ian Sommerville", capturedBook.getAuthor());
        assertEquals("Pearson", capturedBook.getPublisher());
        assertEquals("https://www.pearson.com/example-book", capturedBook.getPublisherBookUrl());

        verify(bookRepository).existsByIsbn(request.getIsbn());
    }

    @Test
    void createBook_shouldThrowException_whenIsbnAlreadyExists() {
        //Arrange
        when(bookRepository.existsByIsbn(bookRequest.getIsbn()))
                .thenReturn(true);

        //Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.createBook(bookRequest)
        );

        assertEquals("ISBN已存在，無法新增重複書籍", exception.getMessage());

        verify(bookRepository).existsByIsbn(bookRequest.getIsbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void createBook_shouldThrowException_whenTitleIsBlank() {
        //Arrange
        BookRequest invalidRequest = new BookRequest(
                "",
                "Ian Sommerville",
                "Pearson",
                "9780137035151",
                850,
                "https://www.pearson.com/example-book"
        );

        //Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.createBook(invalidRequest)
        );

        assertEquals("書名不可為空", exception.getMessage());

        verify(bookRepository, never()).existsByIsbn(anyString());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void findBookById_shouldReturnBookResponse_whenBookExists() {
        //Arrange
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        //Act
        BookResponse response = bookService.findBookById(1L);

        //Assert
        assertNotNull(response);
        assertEquals(1L, response.getBookId());
        assertEquals("Software Engineering", response.getTitle());
        assertEquals("9780137035151", response.getIsbn());
        assertEquals("https://www.pearson.com/example-book", response.getPublisherBookUrl());

        verify(bookRepository).findById(1L);
    }

    @Test
    void findBookById_shouldThrowNotFoundException_whenBookDoesNotExist() {
        //Arrange
        when(bookRepository.findById(999L))
                .thenReturn(Optional.empty());

        //Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookService.findBookById(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("查無此書籍", exception.getReason());

        verify(bookRepository).findById(999L);
    }

    @Test
    void updateBook_shouldReturnUpdatedBookResponse_whenRequestIsValid() {
        //Arrange
        BookRequest updateRequest = new BookRequest(
                "Software Engineering 10th Edition",
                "Ian Sommerville",
                "Pearson",
                "9780137035151",
                900,
                "https://www.pearson.com/example-book-10e"
        );

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(bookRepository.existsByIsbnAndBookIdNot(updateRequest.getIsbn(), 1L))
                .thenReturn(false);

        when(bookRepository.save(any(Book.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        //Act
        BookResponse response = bookService.updateBook(1L, updateRequest);

        //Assert
        assertNotNull(response);
        assertEquals(1L, response.getBookId());
        assertEquals("Software Engineering 10th Edition", response.getTitle());
        assertEquals(900, response.getPrice());
        assertEquals("https://www.pearson.com/example-book-10e", response.getPublisherBookUrl());

        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbnAndBookIdNot(updateRequest.getIsbn(), 1L);
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_shouldThrowException_whenIsbnAlreadyExistsInAnotherBook() {
        //Arrange
        BookRequest updateRequest = new BookRequest(
                "Software Engineering 10th Edition",
                "Ian Sommerville",
                "Pearson",
                "9780137035151",
                900,
                "https://www.pearson.com/example-book-10e"
        );

        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        when(bookRepository.existsByIsbnAndBookIdNot(updateRequest.getIsbn(), 1L))
                .thenReturn(true);

        //Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> bookService.updateBook(1L, updateRequest)
        );

        assertEquals("ISBN已存在，無法修改為重複的ISBN", exception.getMessage());

        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbnAndBookIdNot(updateRequest.getIsbn(), 1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void deleteBook_shouldDeleteBook_whenBookExists() {
        //Arrange
        when(bookRepository.findById(1L))
                .thenReturn(Optional.of(book));

        //Act
        bookService.deleteBook(1L);

        //Assert
        verify(bookRepository).findById(1L);
        verify(bookRepository).delete(book);
    }

    @Test
    void deleteBook_shouldThrowNotFoundException_whenBookDoesNotExist() {
        //Arrange
        when(bookRepository.findById(999L))
                .thenReturn(Optional.empty());

        //Act & Assert
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> bookService.deleteBook(999L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("查無此書籍", exception.getReason());

        verify(bookRepository).findById(999L);
        verify(bookRepository, never()).delete(any(Book.class));
    }
}
