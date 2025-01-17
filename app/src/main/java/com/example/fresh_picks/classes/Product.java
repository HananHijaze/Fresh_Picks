package com.example.fresh_picks.classes;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Product {

    // Basic Information
    private String id; // Regular String for ID

    private String name;
    private String category;

    // Attributes
    private double price;
    private String packSize; // Information about packaging size (e.g., "1 kg", "500 g")
    private int stockQuantity; // Total stock available in inventory
    private String unit; // Unit of measurement (e.g., kg, pcs)

    // Shelf Life
    private String shelfLife;

    // Dietary Information
    private List<String> dietaryInfo;

    // Cuisine Tags
    private List<String> cuisineTags;

    // Availability
    private boolean inStock;
    private boolean seasonal;

    // AI-Friendly Tags
    private List<String> foodPairings;
    private List<String> recipeSuggestions;

    // Product Image URL
    private String imageUrl;

    // Constructors
    public Product() {}

    public Product(String id, String name, String category, double price, String packSize, int stockQuantity, String unit, boolean inStock) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.packSize = packSize;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.inStock = inStock;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getPackSize() {
        return packSize;
    }

    public void setPackSize(String packSize) {
        this.packSize = packSize;
    }

    public int getStockQuantity() {
        return stockQuantity;
    }

    public void setStockQuantity(int stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getShelfLife() {
        return shelfLife;
    }

    public void setShelfLife(String shelfLife) {
        this.shelfLife = shelfLife;
    }

    public List<String> getDietaryInfo() {
        return dietaryInfo;
    }

    public void setDietaryInfo(List<String> dietaryInfo) {
        this.dietaryInfo = dietaryInfo;
    }

    public List<String> getCuisineTags() {
        return cuisineTags;
    }

    public void setCuisineTags(List<String> cuisineTags) {
        this.cuisineTags = cuisineTags;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public boolean isSeasonal() {
        return seasonal;
    }

    public void setSeasonal(boolean seasonal) {
        this.seasonal = seasonal;
    }

    public List<String> getFoodPairings() {
        return foodPairings;
    }

    public void setFoodPairings(List<String> foodPairings) {
        this.foodPairings = foodPairings;
    }

    public List<String> getRecipeSuggestions() {
        return recipeSuggestions;
    }

    public void setRecipeSuggestions(List<String> recipeSuggestions) {
        this.recipeSuggestions = recipeSuggestions;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", price=" + price +
                ", packSize='" + packSize + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", unit='" + unit + '\'' +
                ", inStock=" + inStock +
                '}';
    }
}
