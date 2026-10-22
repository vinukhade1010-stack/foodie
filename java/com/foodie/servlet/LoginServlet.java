package com.foodie.servlet;

import com.foodie.dao.UserDAO;
import com.foodie.model.User;
import com.foodie.util.PasswordUtil;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (currentUser(req) != null) {
            resp.sendRedirect(ctx(req) + "/home");
            return;
        }
        req.setAttribute("next", param(req, "next"));
        render(req, resp, "login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = param(req, "email").toLowerCase();
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String next = param(req, "next");

        User user = email.isEmpty() ? null : new UserDAO().findByEmail(email);
        if (user == null || !PasswordUtil.verify(password, user.getPasswordHash())) {
            // same message for "no such email" and "wrong password" so nobody can probe which emails exist
            req.setAttribute("error", "Email or password is incorrect.");
            req.setAttribute("email", email);
            req.setAttribute("next", next);
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            render(req, resp, "login");
            return;
        }

        user.setPasswordHash(null);                 // do not keep the hash in the session
        HttpSession session = req.getSession();
        req.changeSessionId();                       // new session id after login (prevents session fixation); cart is kept
        session.setAttribute("user", user);
        flash(req, "Welcome back, " + user.getFirstName() + "!");

        if (user.isAdmin() && next.isEmpty()) {
            resp.sendRedirect(ctx(req) + "/admin/dashboard");
        } else {
            resp.sendRedirect(ctx(req) + safeNext(next));
        }
    }

    /** Only allow redirects to a path inside this site, never to another website. */
    static String safeNext(String next) {
        if (next != null && next.startsWith("/") && !next.startsWith("//") && !next.contains(":")) return next;
        return "/home";
    }
}
