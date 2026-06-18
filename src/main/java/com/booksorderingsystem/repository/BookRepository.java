package com.booksorderingsystem.repository;

import com.booksorderingsystem.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {

    boolean existsByIsbn(String isbn);

    boolean existsByIsbnAndBookIdNot(String isbn, Long bookId);

    @Query("""
           SELECT b
           FROM Book b
           WHERE LOWER(b.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.author) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.publisher) LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(b.isbn) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    Page<Book> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
