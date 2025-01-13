package com.example.fresh_picks.classes;

import java.util.Date;

public class Notification {
    private String message; // Notification message
    private Date date; // Date and time of the notification
    private boolean isRead; // Whether the notification has been read

    // Constructor
    public Notification(String message) {
        this.message = message;
        this.date = new Date(); // Set the current date and time
        this.isRead = false; // Default to unread
    }

    // Getters and Setters
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Mark the notification as read
    public void markAsRead() {
        this.isRead = true;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "message='" + message + '\'' +
                ", date=" + date +
                ", isRead=" + isRead +
                '}';
    }
}
