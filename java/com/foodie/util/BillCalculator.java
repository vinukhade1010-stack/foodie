package com.foodie.util;

import com.foodie.model.Bill;
import com.foodie.model.Cart;
import com.foodie.model.Coupon;

/**
 * All money rules live here, in one place:
 *   discount = coupon percent of subtotal, OR a flat Rs.50 off when the subtotal is Rs.800 or more
 *              (only when no coupon is applied)
 *   GST      = 5% of (subtotal - discount)
 *   total    = subtotal - discount + GST
 */
public final class BillCalculator {
    public static final double GST_RATE = 0.05;
    public static final double FLAT_DISCOUNT_THRESHOLD = 800;
    public static final double FLAT_DISCOUNT = 50;

    private BillCalculator() { }

    public static Bill calculate(Cart cart, Coupon coupon) {
        double subtotal = cart.getSubtotal();
        double discount = 0;
        String note = null;

        if (coupon != null && subtotal >= coupon.getMinOrder()) {
            discount = subtotal * coupon.getDiscountPercent() / 100.0;
        } else if (subtotal >= FLAT_DISCOUNT_THRESHOLD) {
            discount = FLAT_DISCOUNT;
            note = "Flat Rs." + (int) FLAT_DISCOUNT + " off on orders above Rs." + (int) FLAT_DISCOUNT_THRESHOLD;
        }
        discount = round(discount);
        double gst = round((subtotal - discount) * GST_RATE);
        double total = round(subtotal - discount + gst);

        Bill b = new Bill();
        b.setItems(cart.getItems());
        b.setCount(cart.getCount());
        b.setSubtotal(subtotal);
        b.setDiscount(discount);
        b.setGst(gst);
        b.setTotal(total);
        b.setCouponCode(coupon != null && discount > 0 && note == null ? coupon.getCode() : null);
        b.setNote(note);
        return b;
    }

    public static double round(double v) { return Math.round(v * 100.0) / 100.0; }
}
