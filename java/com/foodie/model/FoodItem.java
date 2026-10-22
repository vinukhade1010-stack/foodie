package com.foodie.model;

/**
 * One dish on the menu. The field names here are also the JSON keys
 * the browser receives from /api/menu (Gson reads the fields).
 */
public class FoodItem {
    private int id;
    private String name;
    private int categoryId;
    private String categoryName;
    private String description;
    private double price;
    private boolean veg;
    private String emoji;      // shown when there is no image
    private String image;      // optional image URL
    private boolean featured;
    private boolean available;
    private boolean favorite;  // filled per logged-in user, not stored in food_items

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getCategoryId() { return categoryId; }
    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public boolean isVeg() { return veg; }
    public void setVeg(boolean veg) { this.veg = veg; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public boolean isFeatured() { return featured; }
    public void setFeatured(boolean featured) { this.featured = featured; }
    public boolean isAvailable() { return available; }
    public void setAvailable(boolean available) { this.available = available; }
    public boolean isFavorite() { return favorite; }
    public void setFavorite(boolean favorite) { this.favorite = favorite; }
}
