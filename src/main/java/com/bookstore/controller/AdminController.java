package com.bookstore.controller;

import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.service.BookService;
import com.bookstore.service.CategoryService;
import com.bookstore.service.OrderService;
import com.bookstore.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.bookstore.entity.OrderItem;
import com.bookstore.entity.Book;
import com.bookstore.entity.Category;
import com.bookstore.entity.Order;
import com.bookstore.dto.UserDto;
import com.bookstore.entity.User;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private BookService bookService;
    private CategoryService categoryService;
    private OrderService orderService;
    private UserService userService;

    @Value("${app.upload.dir}")
    private String uploadDir;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public AdminController(BookService bookService, CategoryService categoryService,
            OrderService orderService, UserService userService) {
        this.bookService = bookService;
        this.categoryService = categoryService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) throws JsonProcessingException {
        List<Book> books = bookService.findAllBooks();
        List<Order> orders = orderService.findAllOrders();
        List<UserDto> users = userService.findAllUsers();
        List<Category> categories = categoryService.findAllCategories();

        model.addAttribute("books", books);
        model.addAttribute("orders", orders);
        model.addAttribute("users", users);
        model.addAttribute("categories", categories);

        // --- Dashboard Calculations ---

        // 1. Daily Sales for Current Month
        LocalDate now = LocalDate.now();
        Map<String, Double> salesData = orders.stream()
                .filter(o -> o.getOrderDate() != null &&
                        o.getOrderDate().getMonth() == now.getMonth() &&
                        o.getOrderDate().getYear() == now.getYear())
                .collect(Collectors.groupingBy(
                        o -> o.getOrderDate().toLocalDate().toString(),
                        TreeMap::new,
                        Collectors.summingDouble(com.bookstore.entity.Order::getTotalAmount)));
        model.addAttribute("dailySalesJson", objectMapper.writeValueAsString(salesData));

        // 2. Order Status Breakdown
        Map<String, Long> statusData = orders.stream()
                .collect(Collectors.groupingBy(com.bookstore.entity.Order::getStatus, Collectors.counting()));
        model.addAttribute("orderStatusJson", objectMapper.writeValueAsString(statusData));

        // 3. Top Selling Books (Top 5)
        Map<String, Integer> topBooks = orders.stream()
                .flatMap(o -> o.getItems().stream())
                .collect(Collectors.groupingBy(
                        item -> item.getBook().getTitle(),
                        Collectors.summingInt(OrderItem::getQuantity)))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
        model.addAttribute("topBooksJson", objectMapper.writeValueAsString(topBooks));

        // 4. Low Stock Count
        long lowStockCount = books.stream().filter(b -> b.getStockQuantity() != null && b.getStockQuantity() <= 5)
                .count();
        model.addAttribute("lowStockCount", lowStockCount);

        return "admin/dashboard";
    }

    // --- Book Management ---

    @GetMapping("/books")
    public String listBooks(Model model) {
        model.addAttribute("books", bookService.findAllBooks());
        return "admin/books";
    }

    @GetMapping("/books/new")
    public String createBookForm(Model model) {
        Book book = new Book();
        model.addAttribute("book", book);
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/book_form";
    }

    @PostMapping("/books")
    public String saveBook(@ModelAttribute("book") Book book,
            @RequestParam("image") MultipartFile startImage) throws IOException {

        if (!startImage.isEmpty()) {
            String fileName = StringUtils.cleanPath(startImage.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;

            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (var inputStream = startImage.getInputStream()) {
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                book.setCoverImage(uniqueFileName);
            } catch (IOException ioe) {
                throw new IOException("Could not save image file: " + fileName, ioe);
            }
        }

        // Ensure stock is saved
        if (book.getStockQuantity() == null) {
            book.setStockQuantity(0);
        }

        bookService.saveBook(book);
        return "redirect:/admin/books";
    }

    @GetMapping("/books/edit/{id}")
    public String editBookForm(@PathVariable Long id, Model model) {
        model.addAttribute("book", bookService.findBookById(id));
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/book_form";
    }

    @PostMapping("/books/{id}")
    public String updateBook(@PathVariable Long id,
            @ModelAttribute("book") Book book,
            @RequestParam("image") MultipartFile startImage) throws IOException {

        Book existingBook = bookService.findBookById(id);
        existingBook.setTitle(book.getTitle());
        existingBook.setAuthor(book.getAuthor());
        existingBook.setPrice(book.getPrice());
        existingBook.setIsbn(book.getIsbn());
        existingBook.setDescription(book.getDescription());
        existingBook.setCategory(book.getCategory());

        // Update stock quantity
        existingBook.setStockQuantity(book.getStockQuantity());

        if (!startImage.isEmpty()) {
            String fileName = StringUtils.cleanPath(startImage.getOriginalFilename());
            String uniqueFileName = UUID.randomUUID().toString() + "_" + fileName;
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath))
                Files.createDirectories(uploadPath);
            try (var inputStream = startImage.getInputStream()) {
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                existingBook.setCoverImage(uniqueFileName);
            }
        }

        bookService.updateBook(existingBook);
        return "redirect:/admin/books";
    }

    @GetMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }

    @GetMapping("/books/toggle-feature/{id}")
    public String toggleFeature(@PathVariable Long id) {
        Book book = bookService.findBookById(id);
        if (book != null) {
            book.setFeatured(!book.isFeatured());
            bookService.saveBook(book);
        }
        return "redirect:/admin/books";
    }

    // --- Category Management ---

    @GetMapping("/categories")
    public String listCategories(Model model) {
        model.addAttribute("categories", categoryService.findAllCategories());
        return "admin/categories";
    }

    @GetMapping("/categories/new")
    public String createCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category_form";
    }

    @PostMapping("/categories")
    public String saveCategory(@ModelAttribute("category") Category category) {
        categoryService.saveCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/categories/delete/{id}")
    public String deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return "redirect:/admin/categories";
    }

    // --- Order Management ---

    @GetMapping("/orders")
    public String listOrders(Model model) {
        model.addAttribute("orders", orderService.findAllOrders());
        return "admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("status") String status) {
        orderService.updateOrderStatus(id, status);
        return "redirect:/admin/orders";
    }

    @GetMapping("/orders/edit/{id}")
    public String editOrderForm(@PathVariable Long id, Model model) {
        model.addAttribute("order", orderService.findOrderById(id));
        return "admin/order_edit";
    }

    @PostMapping("/orders/update/{id}")
    public String updateOrderDetails(@PathVariable Long id,
            @RequestParam("status") String status,
            @RequestParam("trackingNumber") String trackingNumber,
            @RequestParam("shippingCompany") String shippingCompany) {
        orderService.updateOrderDetails(id, status, trackingNumber, shippingCompany);
        return "redirect:/admin/orders";
    }

    // --- User Management ---

    @GetMapping("/users")
    public String listUsers(Model model) {
        model.addAttribute("users", userService.findAllUsers());
        model.addAttribute("orders", orderService.findAllOrders());
        return "admin/users";
    }

    @GetMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }
}
