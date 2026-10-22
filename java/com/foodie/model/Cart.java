package com.foodie.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * The shopping cart. It lives in the HttpSession, not in the database.
 * A LinkedHashMap keeps items in the order they were added and lets us
 * find an item by its food id in constant time.
 */
public class Cart implements Serializable {
    public static final int MAX_QTY_PER_ITEM = 20;

    private final LinkedHashMap<Integer, CartItem> items = new LinkedHashMap<>();
    private String couponCode;

    public void add(FoodItem food) {
        CartItem existing = items.get(food.getId());
        if (existing == null) {
            items.put(food.getId(), new CartItem(food));
        } else if (existing.getQuantity() < MAX_QTY_PER_ITEM) {
            existing.setQuantity(existing.getQuantity() + 1);
        }
    }

    /** Sets the quantity. Zero or less removes the item. */
    public void setQuantity(int foodId, int qty) {
        CartItem item = items.get(foodId);
        if (item == null) return;
        if (qty <= 0) items.remove(foodId);
        else item.setQuantity(Math.min(qty, MAX_QTY_PER_ITEM));
    }

    public void remove(int foodId) { items.remove(foodId); }

    public void clear() { items.clear(); couponCode = null; }

    public List<CartItem> getItems() { return new ArrayList<>(items.values()); }

    public boolean isEmpty() { return items.isEmpty(); }

    /** Total number of plates (sum of quantities), shown in the navbar badge. */
    public int getCount() {
        int n = 0;
        for (CartItem i : items.values()) n += i.getQuantity();
        return n;
    }

    public double getSubtotal() {
        double s = 0;
        for (CartItem i : items.values()) s += i.getLineTotal();
        return Math.round(s * 100.0) / 100.0;
    }

    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
}
