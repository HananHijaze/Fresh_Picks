package com.example.fresh_picks.classes;

import java.util.ArrayList;
import java.util.List;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private Address address; // New field
    private List<Product> wishlist;
    private Cart cart;
    private List<Order> orders;

    // Constructors
    public User(int id, String name, String email, String password, Address address) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.wishlist = new ArrayList<>();
        this.cart = new Cart();
        this.orders = new ArrayList<>();
    }

    // Getters and Setters
    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    // Methods for managing wishlist, cart, and orders
}
