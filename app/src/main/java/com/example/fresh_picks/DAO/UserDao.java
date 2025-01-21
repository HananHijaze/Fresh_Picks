package com.example.fresh_picks.DAO;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {
    @Insert
    void insertUser(UserEntity user);

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    UserEntity getUserByEmail(String email);

    @Query("SELECT * FROM users")
    List<UserEntity> getAllUsers();

    @Query("SELECT * FROM users WHERE phoneNumber = :Phone LIMIT 1")
    UserEntity getUserByPhone(String Phone);
    // Delete all users
    @Query("DELETE FROM users")
    void deleteAllUsers();

}
