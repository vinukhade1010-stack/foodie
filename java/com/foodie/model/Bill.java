package com.foodie.model;

import java.util.List;

/** The priced version of a cart. Sent to the browser as JSON. */
public class Bill {
    private List<CartItem> items;
    private int count;
    private double subtotal;
    private double discount;
    private double gst;
    private double total;
    private String couponCode;
    private String note;        // e.g. "Flat Rs.50 off applied"

    public List<CartItem> getItems() { return items; }
    public void setItems(List<CartItem> items) { this.items = items; }
    public int getCount() { return count; }
    public void setCount(int count) { this.count = count; }
    public double getSubtotal() { return subtotal; }
    public void setSubtotal(double subtotal) { this.subtotal = subtotal; }
    public double getDiscount() { return discount; }
    public void setDiscount(double discount) { this.discount = discount; }
    public double getGst() { return gst; }
    public void setGst(double gst) { this.gst = gst; }
    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
