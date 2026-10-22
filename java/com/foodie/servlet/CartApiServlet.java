package com.foodie.servlet;

import com.foodie.dao.CouponDAO;
import com.foodie.dao.FoodDAO;
import com.foodie.model.Bill;
import com.foodie.model.Cart;
import com.foodie.model.Coupon;
import com.foodie.model.FoodItem;
import com.foodie.service.CartService;
import com.foodie.util.Json;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * The cart as an AJAX endpoint.
 *   GET  /api/cart                                  -> current bill
 *   POST /api/cart  action=add     id=5
 *   POST /api/cart  action=set     id=5 qty=3       (qty 0 removes the item)
 *   POST /api/cart  action=remove  id=5
 *   POST /api/cart  action=clear
 *   POST /api/cart  action=coupon  code=WELCOME20   (empty code removes the coupon)
 * Every reply is JSON: {"ok":true|false, "message"/"error":"...", "bill":{...}}
 */
@WebServlet("/api/cart")
public class CartApiServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cart cart = cart(req);
        CartService.syncWithMenu(cart);
        Json.send(resp, 200, Json.obj("ok", true, "bill", CartService.currentBill(cart)));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cart cart = cart(req);
        String action = param(req, "action");
        int id = intParam(req, "id", 0);
        String message = null;
        String error = null;

        switch (action) {
            case "add": {
                FoodItem food = new FoodDAO().findById(id);
                if (food == null || !food.isAvailable()) {
                    error = "Sorry, that dish is not available right now.";
                } else {
                    cart.add(food);
                    message = food.getName() + " added to your cart";
                }
                break;
            }
            case "set":
                cart.setQuantity(id, intParam(req, "qty", 1));
                break;
            case "remove":
                cart.remove(id);
                message = "Item removed";
                break;
            case "clear":
                cart.clear();
                message = "Cart cleared";
                break;
            case "coupon": {
                String code = param(req, "code").toUpperCase();
                if (code.isEmpty()) {
                    cart.setCouponCode(null);
                    message = "Coupon removed";
                } else {
                    Coupon coupon = new CouponDAO().findActiveByCode(code);
                    if (coupon == null) {
                        error = "That code is not valid or has expired.";
                    } else if (cart.getSubtotal() < coupon.getMinOrder()) {
                        error = "Add items worth Rs." + (int) Math.ceil(coupon.getMinOrder() - cart.getSubtotal())
                                + " more to use " + code + ".";
                    } else {
                        cart.setCouponCode(code);
                        message = code + " applied. You save " + coupon.getDiscountPercent() + "%.";
                    }
                }
                break;
            }
            default:
                error = "Unknown action.";
        }

        Bill bill = CartService.currentBill(cart);
        if (error != null) Json.send(resp, 400, Json.obj("ok", false, "error", error, "bill", bill));
        else Json.send(resp, 200, Json.obj("ok", true, "message", message, "bill", bill));
    }
}
