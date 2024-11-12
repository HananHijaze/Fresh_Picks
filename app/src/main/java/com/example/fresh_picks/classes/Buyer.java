package com.example.fresh_picks.classes;

import java.util.List;

public class Buyer extends User {
    private List<Product> cart;
    private List<Product> wishList;
    private List<Order> orderHistory;

    public Buyer(int id, String name, String email, String password, String address, String phoneNumber) {
        super(id, name, email, password, address, phoneNumber);
    }

    public List<Product> getCart() {
        return cart;
    }

    public void setCart(List<Product> cart) {
        this.cart = cart;
    }

    public List<Product> getWishList() {
        return wishList;
    }

    public void setWishList(List<Product> wishList) {
        this.wishList = wishList;
    }

    public void setOrderHistory(List<Order> orderHistory) {
        this.orderHistory = orderHistory;
    }

    @Override
    public void login(String email, String password) {
        // Implement login logic for buyers
    }

    @Override
    public void logout() {
        // Implement logout logic
    }

    @Override
    public void updateProfile(String newName, String newEmail, String newAddress, String newPhoneNumber) {
        // Update buyer profile details
    }

    public void addToCart(Product product) {
        cart.add(product);
    }

    public void placeOrder() {
        // Logic for placing an order
    }

    public List<Order> getOrderHistory() {
        return orderHistory;
    }
}
