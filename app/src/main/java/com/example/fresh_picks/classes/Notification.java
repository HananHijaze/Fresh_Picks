package com.example.fresh_picks.classes;

public class Notification {
    private int notificationId;
    private int userId;
    private String message;
    private String date;

    public Notification(int notificationId, int userId, String message, String date) {
        this.notificationId = notificationId;
        this.userId = userId;
        this.message = message;
        this.date = date;
    }

    public void sendNotification() {
        // Logic to send notification
    }

    // Getters and setters for notification details
}
