package com.foodie.servlet;

import com.foodie.dao.OrderDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/orders")
public class AdminOrderServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String status = param(req, "status");
        req.setAttribute("orders", new OrderDAO().findAll(status.isEmpty() ? null : status));
        req.setAttribute("filter", status);
        render(req, resp, "admin/orders");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        int id = intParam(req, "id", 0);
        String status = param(req, "status");
        if (java.util.Arrays.asList("PLACED", "PREPARING", "DELIVERED", "CANCELLED").contains(status)) {
            new OrderDAO().updateStatus(id, status);
            flash(req, "Order #" + id + " is now " + status.substring(0, 1) + status.substring(1).toLowerCase());
        }
        String back = param(req, "filter");
        resp.sendRedirect(ctx(req) + "/admin/orders" + (back.isEmpty() ? "" : "?status=" + back));
    }
}
