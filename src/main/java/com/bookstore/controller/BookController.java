package com.bookstore.controller;

import com.bookstore.entity.Book;
import com.bookstore.service.BookService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class BookController {

    private com.bookstore.service.BookService bookService;
    private com.bookstore.service.CategoryService categoryService;

    public BookController(BookService bookService, com.bookstore.service.CategoryService categoryService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Book> books = bookService.findAllBooks();
        List<Book> featured = bookService.findFeaturedBooks();

        // If no books are featured, fallback to first 4 books
        if (featured.isEmpty()) {
            featured = books.stream().limit(4).toList();
        }

        model.addAttribute("featuredBooks", featured);
        model.addAttribute("recentBooks", books.stream().skip(4).limit(4).toList());
        model.addAttribute("categories", categoryService.findAllCategories());
        return "home";
    }

    @GetMapping("/books")
    public String listBooks(Model model, @RequestParam(value = "query", required = false) String query) {
        List<Book> books = bookService.searchBooks(query);
        model.addAttribute("books", books);
        return "book_list";
    }

    @GetMapping("/book/{id}")
    public String bookDetails(@PathVariable Long id, Model model) {
        Book book = bookService.findBookById(id);
        if (book == null) {
            return "redirect:/books";
        }
        model.addAttribute("book", book);
        return "book_details";
    }
}
