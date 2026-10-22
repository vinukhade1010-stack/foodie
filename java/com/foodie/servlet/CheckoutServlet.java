package com.foodie.servlet;

import com.foodie.dao.OrderDAO;
import com.foodie.model.Bill;
import com.foodie.model.Cart;
import com.foodie.model.CartItem;
import com.foodie.model.Order;
import com.foodie.model.OrderItem;
import com.foodie.model.User;
import com.foodie.service.CartService;
import com.foodie.util.FileUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET  /checkout  address + payment form (the bill is drawn by JavaScript from /api/cart)
 * POST /checkout  re-prices the cart on the server, saves the order, writes the receipt file
 */
@WebServlet("/checkout")
public class CheckoutServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Cart cart = cart(req);
        if (cart.isEmpty()) {
            flash(req, "Your cart is empty. Add something tasty first.");
            resp.sendRedirect(ctx(req) + "/menu");
            return;
        }
        render(req, resp, "checkout");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        Cart cart = cart(req);
        CartService.syncWithMenu(cart);
        if (cart.isEmpty()) {
            flash(req, "Your cart is empty. Add something tasty first.");
            resp.sendRedirect(ctx(req) + "/menu");
            return;
        }

        String address = param(req, "address");
        String payment = param(req, "payment");
        if (!"COD".equals(payment) && !"UPI_DEMO".equals(payment)) payment = "COD";
        if (address.length() < 10 || address.length() > 250) {
            req.setAttribute("error", "Please enter a full delivery address (10 to 250 characters).");
            req.setAttribute("address", address);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            render(req, resp, "checkout");
            return;
        }

        Bill bill = CartService.currentBill(cart);
        Order order = new Order();
        order.setUserId(user.getId());
        order.setCustomerName(user.getName());
        order.setSubtotal(bill.getSubtotal());
        order.setDiscount(bill.getDiscount());
        order.setGst(bill.getGst());
        order.setTotal(bill.getTotal());
        order.setCouponCode(bill.getCouponCode());
        order.setPaymentMethod("COD".equals(payment) ? "Cash on Delivery" : "UPI (demo)");
        order.setAddress(address);
        for (CartItem ci : cart.getItems()) {
            OrderItem oi = new OrderItem();
            oi.setFoodItemId(ci.getFoodId());
            oi.setItemName(ci.getName());
            oi.setQuantity(ci.getQuantity());
            oi.setPrice(ci.getPrice());
            order.getItems().add(oi);
        }

        int orderId = new OrderDAO().placeOrder(order);
        cart.clear();

        // remember the address for next time
        if (user.getAddress() == null || user.getAddress().isEmpty()) {
            new com.foodie.dao.UserDAO().updateProfile(user.getId(), user.getName(), user.getPhone(), address);
            user.setAddress(address);
        }

        // Write the receipt file now. If the disk write fails, the order is still safe in the database.
        try {
            FileUtil.writeReceipt(new OrderDAO().findById(orderId));
        } catch (IOException e) {
            log("Could not write receipt file for order " + orderId, e);
        }
        resp.sendRedirect(ctx(req) + "/order-success?id=" + orderId);
    }
}
