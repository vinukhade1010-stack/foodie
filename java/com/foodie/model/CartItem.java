package com.foodie.model;

import java.io.Serializable;

/** One line in the cart: a dish and how many of it. */
public class CartItem implements Serializable {
    private int foodId;
    private String name;
    private double price;
    private int quantity;
    private boolean veg;
    private String emoji;
    private String image;
    private double lineTotal;

    public CartItem(FoodItem f) {
        this.foodId = f.getId();
        this.name = f.getName();
        this.price = f.getPrice();
        this.veg = f.isVeg();
        this.emoji = f.getEmoji();
        this.image = f.getImage();
        this.quantity = 1;
        recalc();
    }

    private void recalc() { lineTotal = Math.round(price * quantity * 100.0) / 100.0; }

    public int getFoodId() { return foodId; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; recalc(); }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; recalc(); }
    public boolean isVeg() { return veg; }
    public String getEmoji() { return emoji; }
    public String getImage() { return image; }
    public double getLineTotal() { return lineTotal; }
}
