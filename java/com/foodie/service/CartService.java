package com.foodie.service;

import com.foodie.dao.CouponDAO;
import com.foodie.dao.FoodDAO;
import com.foodie.model.Bill;
import com.foodie.model.Cart;
import com.foodie.model.CartItem;
import com.foodie.model.Coupon;
import com.foodie.model.FoodItem;
import com.foodie.util.BillCalculator;

/** Glue between the session cart, the menu in the database and the billing rules. */
public final class CartService {
    private CartService() { }

    /**
     * Re-checks every cart line against the database: removes dishes that were deleted or
     * switched to sold-out, and refreshes prices. The browser is never trusted for prices.
     */
    public static void syncWithMenu(Cart cart) {
        FoodDAO dao = new FoodDAO();
        for (CartItem i : cart.getItems()) {
            FoodItem f = dao.findById(i.getFoodId());
            if (f == null || !f.isAvailable()) cart.remove(i.getFoodId());
            else i.setPrice(f.getPrice());
        }
    }

    /** Prices the cart. A coupon that is no longer valid (or no longer meets its minimum) is dropped. */
    public static Bill currentBill(Cart cart) {
        Coupon coupon = null;
        if (cart.getCouponCode() != null) {
            coupon = new CouponDAO().findActiveByCode(cart.getCouponCode());
            if (coupon == null || cart.getSubtotal() < coupon.getMinOrder()) {
                cart.setCouponCode(null);
                coupon = null;
            }
        }
        return BillCalculator.calculate(cart, coupon);
    }
}
