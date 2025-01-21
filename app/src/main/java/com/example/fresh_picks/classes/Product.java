package com.example.fresh_picks.classes;

import java.util.List;

public class Product {

    // Basic Information
    private String id; // Product ID
    private String name; // Product Name in English
    private String nameAr; // Product Name in Arabic
    private List<String> category; // Product Category (single category)

    // Pricing and Stock
    private double price; // Product Price
    private String packSize; // Packaging size (e.g., "1 kg", "500 g")
    private int stockQuantity; // Stock Quantity
    private String unit; // Unit of measurement (e.g., kg, pcs)

    // Dietary Information
    private List<String> dietaryInfo; // List of dietary tags (e.g., Vegan, Gluten-Free)

    // Cuisine Tags
    private List<String> cuisineTags; // List of cuisine categories

    // Availability
    private boolean inStock; // If the product is currently in stock
    private boolean seasonal; // If the product is seasonal

    // AI Tags and Suggestions
    private List<String> foodPairings; // Suggested food pairings
    private List<String> recipeSuggestions; // Suggested recipes

    // Image
    private String imageUrl; // Image URL

    // Popularity Score
    private int popularityScore; // Popularity Score of the product

    // Constructors
    public Product() {}

    public Product(String id, String name, String nameAr, List<String> category, double price, String packSize, int stockQuantity,
                   String unit, List<String> dietaryInfo, List<String> cuisineTags, boolean inStock, boolean seasonal,
                   List<String> foodPairings, List<String> recipeSuggestions, String imageUrl, int popularityScore) {
        this.id = id;
        this.name = name;
        this.nameAr = nameAr;
        this.category = category;
        this.price = price;
        this.packSize = packSize;
        this.stockQuantity = stockQuantity;
        this.unit = unit;
        this.dietaryInfo = dietaryInfo;
        this.cuisineTags = cuisineTags;
        this.inStock = inStock;
        this.seasonal = seasonal;
        this.foodPairings = foodPairings;
        this.recipeSuggestions = recipeSuggestions;
        this.imageUrl = imageUrl;
        this.popularityScore = popularityScore;
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

    public String getNameAr() {
        return nameAr;
    }

    public void setNameAr(String nameAr) {
        this.nameAr = nameAr;
    }

    public List<String> getCategory() {return category;}

    public void setCategory(List<String> category) {
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

    public int getPopularityScore() {
        return popularityScore;
    }

    public void setPopularityScore(int popularityScore) {
        this.popularityScore = popularityScore;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", nameAr='" + nameAr + '\'' +
                ", category=" + category +
                ", price=" + price +
                ", packSize='" + packSize + '\'' +
                ", stockQuantity=" + stockQuantity +
                ", unit='" + unit + '\'' +
                ", dietaryInfo=" + dietaryInfo +
                ", cuisineTags=" + cuisineTags +
                ", inStock=" + inStock +
                ", seasonal=" + seasonal +
                ", foodPairings=" + foodPairings +
                ", recipeSuggestions=" + recipeSuggestions +
                ", imageUrl='" + imageUrl + '\'' +
                ", popularityScore=" + popularityScore +
                '}';
    }
}
