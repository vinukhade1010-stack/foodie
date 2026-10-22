package com.foodie.model;

public class OrderItem {
    private int foodItemId;
    private String itemName;
    private int quantity;
    private double price;

    public double getLineTotal() { return Math.round(price * quantity * 100.0) / 100.0; }

    public int getFoodItemId() { return foodItemId; }
    public void setFoodItemId(int foodItemId) { this.foodItemId = foodItemId; }
    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
}
