package com.bookstore.service.impl;

import com.bookstore.entity.Book;
import com.bookstore.exception.InsufficientStockException;
import com.bookstore.exception.ResourceNotFoundException;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    @Override
    public List<Book> findAllBooks() {
        log.debug("Finding all books");
        return bookRepository.findAll();
    }

    @Override
    public Page<Book> findPaginated(Pageable pageable) {
        log.debug("Finding paginated books: {}", pageable);
        return bookRepository.findByDeletedFalse(pageable);
    }

    @Override
    public Book findBookById(Long id) {
        log.debug("Finding book by id: {}", id);
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found with id: " + id));
    }

    @Override
    @Transactional
    public void saveBook(Book book) {
        log.info("Saving book: {}", book.getTitle());
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void updateBook(Book book) {
        log.info("Updating book with id: {}", book.getId());
        if (!bookRepository.existsById(book.getId())) {
            throw new ResourceNotFoundException("Cannot update. Book not found with id: " + book.getId());
        }
        bookRepository.save(book);
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        log.info("Soft deleting book with id: {}", id);
        Book book = findBookById(id);
        book.setDeleted(true);
        bookRepository.save(book);
    }

    @Override
    public Page<Book> searchBooks(String keyword, Pageable pageable) {
        log.debug("Searching books with keyword: {} and pageable: {}", keyword, pageable);
        if (keyword != null && !keyword.trim().isEmpty()) {
            return bookRepository.searchBooks(keyword, pageable);
        }
        return bookRepository.findByDeletedFalse(pageable);
    }

    @Override
    public List<Book> findFeaturedBooks() {
        log.debug("Finding featured books");
        return bookRepository.findByFeaturedTrue();
    }

    @Override
    @Transactional
    public void decrementStock(Long bookId, int quantity) {
        log.info("Decrementing stock for book id: {} by quantity: {}", bookId, quantity);
        Book book = findBookById(bookId);
        if (book.getStockQuantity() < quantity) {
            log.error("Insufficient stock for book: {}. Available: {}, Requested: {}",
                    book.getTitle(), book.getStockQuantity(), quantity);
            throw new InsufficientStockException("Not enough stock for book: " + book.getTitle());
        }
        book.setStockQuantity(book.getStockQuantity() - quantity);
        bookRepository.save(book);
    }
}
