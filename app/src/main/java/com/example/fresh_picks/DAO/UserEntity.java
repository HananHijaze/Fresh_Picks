package com.example.fresh_picks.DAO;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "users")
public class UserEntity {
    @PrimaryKey
    @NonNull
    private String id; // Firestore user ID will be used

    private String name;
    private String email;
    private String password; // Store hashed password
    private String phoneNumber;
    private String address;

    public UserEntity(String id, String name, String email, String password, String phoneNumber, String address) {
        this.id = id;  // Firestore ID is stored
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.address = address;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public String getPassword() { return password; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getAddress() { return address; }
}
