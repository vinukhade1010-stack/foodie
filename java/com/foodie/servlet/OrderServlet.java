package com.foodie.servlet;

import com.foodie.dao.OrderDAO;
import com.foodie.model.Order;
import com.foodie.model.User;
import com.foodie.util.Json;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * One servlet, three URLs:
 *   /orders              my order history
 *   /order-success?id=   confirmation page after checkout
 *   /api/order-status    JSON {"statuses":{"12":"PREPARING"}} - the orders page polls this to update the tracker
 */
@WebServlet(urlPatterns = {"/orders", "/order-success", "/api/order-status"})
public class OrderServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String path = req.getServletPath();
        OrderDAO dao = new OrderDAO();

        if ("/api/order-status".equals(path)) {
            User user = currentUser(req);
            if (user == null) {
                Json.send(resp, 401, Json.obj("ok", false));
                return;
            }
            Map<Integer, String> statuses = new LinkedHashMap<>();
            for (Order o : dao.findByUser(user.getId())) statuses.put(o.getId(), o.getStatus());
            Json.send(resp, 200, Json.obj("ok", true, "statuses", statuses));
            return;
        }

        User user = currentUser(req);   // AuthFilter guarantees a user for the other two URLs
        if ("/order-success".equals(path)) {
            Order order = dao.findById(intParam(req, "id", 0));
            if (order == null || order.getUserId() != user.getId()) {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            req.setAttribute("order", order);
            render(req, resp, "order-success");
            return;
        }

        List<Order> orders = dao.findByUser(user.getId());
        req.setAttribute("orders", orders);
        render(req, resp, "orders");
    }
}
