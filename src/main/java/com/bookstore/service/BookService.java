package com.bookstore.service;

import com.bookstore.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;

public interface BookService {
    List<Book> findAllBooks();

    Page<Book> findPaginated(Pageable pageable);

    Book findBookById(Long id);

    void saveBook(Book book);

    void updateBook(Book book);

    void deleteBook(Long id);

    Page<Book> searchBooks(String keyword, Pageable pageable);

    List<Book> findFeaturedBooks();

    void decrementStock(Long bookId, int quantity);
}
