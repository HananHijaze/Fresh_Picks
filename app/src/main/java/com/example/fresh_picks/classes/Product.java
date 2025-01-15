package com.example.fresh_picks.classes;

import com.google.gson.annotations.SerializedName;

public class Product {

    @SerializedName("_id")
    private IdWrapper id; // Wrapper for ObjectId to handle "$oid"

    private String name;
    private double price;
    private String category;
    private int stockQuantity;
    private String imageUrl;

    public static class IdWrapper {
        @SerializedName("$oid")
        private String oid;

        public String getOid() {
            return oid;
        }

        public void setOid(String oid) {
            this.oid = oid;
        }
    }

    // Constructors
    public Product() {}

    public Product(IdWrapper id, String name, double price, String category, int stockQuantity, String imageUrl) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.stockQuantity = stockQuantity;
        this.imageUrl = imageUrl;
    }

    // Getters and Setters
    public String getId() {
        return id != null ? id.getOid() : null;
    }

    public void setId(String oid) {
        if (this.id == null) {
            this.id = new IdWrapper();
        }
        this.id.setOid(oid);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
