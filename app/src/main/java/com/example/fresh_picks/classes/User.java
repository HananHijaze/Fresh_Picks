package com.example.fresh_picks.classes;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private List<String> addresses; // List of user's addresses
    private String phoneNumber; // User's phone number
    private Cart cart;
    private List<Order> orders;
    private boolean bulkSaleBuyer; // Indicates if the user qualifies for bulk sales

    // Constructors

    public User(int id, String name, String email, String password, List<String> addresses, String phoneNumber, Cart cart, List<Order> orders, boolean bulkSaleBuyer) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.addresses = addresses;
        this.phoneNumber = phoneNumber;
        this.cart = cart;
        this.orders = orders;
        this.bulkSaleBuyer = bulkSaleBuyer;
    }

    public User(String name, String email, String password, List<String> addresses, String phoneNumber) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.addresses = new ArrayList<>(addresses); // Ensure immutability by creating a new list
        this.phoneNumber = phoneNumber;
        this.cart = new Cart(new ArrayList<>());
        this.orders = new ArrayList<>();
        this.bulkSaleBuyer = false; // Default value
    }

    public User(int id, String name, String email, String password, List<String> addresses, Cart cart, List<Order> orders, boolean bulkSaleBuyer) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.addresses = addresses;
        this.cart = cart;
        this.orders = orders;
        this.bulkSaleBuyer = bulkSaleBuyer;
    }

    public User(String name, String email, String password, List<String> addresses) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.addresses = new ArrayList<>(addresses); // Ensure immutability by creating a new list
        this.cart = new Cart(new ArrayList<>());
        this.orders = new ArrayList<>();
        this.bulkSaleBuyer = false; // Default value
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public List<String> getAddresses() {
        return new ArrayList<>(addresses); // Return a copy of the list to ensure immutability
    }

    public void setAddresses(List<String> addresses) {
        this.addresses = new ArrayList<>(addresses); // Ensure immutability by creating a new list
    }

    public void addAddress(String address) {
        this.addresses.add(address);
    }

    public void removeAddress(String address) {
        this.addresses.remove(address);
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public boolean isBulkSaleBuyer() {
        return bulkSaleBuyer;
    }

    public void setBulkSaleBuyer(boolean bulkSaleBuyer) {
        this.bulkSaleBuyer = bulkSaleBuyer;
    }

    // Methods for managing orders
    public Order getOrderById(int orderId) {
        for (Order order : orders) {
            if (order.getId() == orderId) {
                return order;
            }
        }
        return null;
    }

    public void cancelOrder(int orderId) {
        orders.removeIf(order -> order.getId() == orderId);
    }

    // Utility Methods
    public void placeOrder(Order order) {
        orders.add(order);
        cart.clearCart(); // Clear the cart after placing the order
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", addresses=" + addresses +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", bulkSaleBuyer=" + bulkSaleBuyer +
                ", ordersCount=" + orders.size() +
                '}';
    }
}
