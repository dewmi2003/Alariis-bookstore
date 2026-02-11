package com.bookstore.service.impl;

import com.bookstore.dto.CartItem;
import com.bookstore.entity.Book;
import com.bookstore.service.CartService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Service
@SessionScope
public class CartServiceImpl implements CartService {

    private List<CartItem> items = new ArrayList<>();

    @Override
    public void addItem(Book book, int quantity) {
        for (CartItem item : items) {
            if (item.getBook().getId().equals(book.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(book, quantity));
    }

    @Override
    public void updateItem(Long bookId, int quantity) {
        for (CartItem item : items) {
            if (item.getBook().getId().equals(bookId)) {
                item.setQuantity(quantity);
                return;
            }
        }
    }

    @Override
    public void removeItem(Long bookId) {
        Iterator<CartItem> iterator = items.iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getBook().getId().equals(bookId)) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public void clearCart() {
        items.clear();
    }

    @Override
    public List<CartItem> getItems() {
        return items;
    }

    @Override
    public double getTotalAmount() {
        return items.stream().mapToDouble(CartItem::getTotalPrice).sum();
    }
}
