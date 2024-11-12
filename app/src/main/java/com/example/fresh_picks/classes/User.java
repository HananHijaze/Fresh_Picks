package com.example.fresh_picks.classes;

public abstract class User {
    protected int id;
    protected String name;
    protected String email;
    protected String password;
    protected String address;
    protected String phoneNumber;

    public User(int id, String name, String email, String password, String address, String phoneNumber) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.address = address;
        this.phoneNumber = phoneNumber;
    }

    public abstract void login(String email, String password);
    public abstract void logout();
    public abstract void updateProfile(String newName, String newEmail, String newAddress, String newPhoneNumber);
}
