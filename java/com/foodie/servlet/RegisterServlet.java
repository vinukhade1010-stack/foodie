package com.foodie.servlet;

import com.foodie.dao.UserDAO;
import com.foodie.model.User;
import com.foodie.util.PasswordUtil;
import com.foodie.util.Validate;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet("/register")
public class RegisterServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (currentUser(req) != null) {
            resp.sendRedirect(ctx(req) + "/home");
            return;
        }
        render(req, resp, "register");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String name = param(req, "name");
        String email = param(req, "email").toLowerCase();
        String phone = param(req, "phone");
        String password = req.getParameter("password") == null ? "" : req.getParameter("password");
        String confirm = req.getParameter("confirm") == null ? "" : req.getParameter("confirm");

        String error = null;
        UserDAO dao = new UserDAO();
        if (name.length() < 2) error = "Please enter your name.";
        else if (!Validate.email(email)) error = "Enter a valid email address.";
        else if (!Validate.phone(phone)) error = "Enter a 10-digit mobile number.";
        else if (!Validate.password(password)) error = "Password needs 8 or more characters, with letters and numbers.";
        else if (!password.equals(confirm)) error = "The two passwords do not match.";
        else if (dao.findByEmail(email) != null) error = "An account with this email already exists. Try logging in.";

        if (error != null) {
            req.setAttribute("error", error);
            req.setAttribute("name", name);
            req.setAttribute("email", email);
            req.setAttribute("phone", phone);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            render(req, resp, "register");
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPhone(phone);
        user.setAddress("");
        user.setPasswordHash(PasswordUtil.hash(password));
        user.setId(dao.register(user));
        user.setRole("CUSTOMER");
        user.setPasswordHash(null);

        HttpSession session = req.getSession();
        req.changeSessionId();
        session.setAttribute("user", user);
        flash(req, "Account created. Welcome to Foodie, " + user.getFirstName() + "!");
        resp.sendRedirect(ctx(req) + "/menu");
    }
}
