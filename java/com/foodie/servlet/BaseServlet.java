package com.foodie.servlet;

import com.foodie.model.Cart;
import com.foodie.model.User;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Shared helpers so each servlet stays short. */
public abstract class BaseServlet extends HttpServlet {

    /** Forwards to /WEB-INF/views/<view>.jsp (files inside WEB-INF cannot be opened directly from the browser). */
    protected void render(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }

    protected User currentUser(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        return s == null ? null : (User) s.getAttribute("user");
    }

    protected Cart cart(HttpServletRequest req) {
        HttpSession s = req.getSession();
        Cart cart = (Cart) s.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            s.setAttribute("cart", cart);
        }
        return cart;
    }

    /** A message shown once as a toast on the next page. */
    protected void flash(HttpServletRequest req, String message) {
        req.getSession().setAttribute("flash", message);
    }

    protected String param(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return v == null ? "" : v.trim();
    }

    protected int intParam(HttpServletRequest req, String name, int fallback) {
        try {
            return Integer.parseInt(param(req, name));
        } catch (NumberFormatException e) {
            return fallback;
        }
    }

    protected String ctx(HttpServletRequest req) { return req.getContextPath(); }
}
