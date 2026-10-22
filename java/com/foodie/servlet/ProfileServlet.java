package com.foodie.servlet;

import com.foodie.dao.UserDAO;
import com.foodie.model.User;
import com.foodie.util.Validate;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/profile")
public class ProfileServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        render(req, resp, "profile");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        String name = param(req, "name");
        String phone = param(req, "phone");
        String address = param(req, "address");

        String error = null;
        if (name.length() < 2) error = "Please enter your name.";
        else if (!Validate.phone(phone)) error = "Enter a 10-digit mobile number.";
        else if (address.length() > 250) error = "Address is too long (250 characters at most).";

        if (error != null) {
            req.setAttribute("error", error);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            render(req, resp, "profile");
            return;
        }
        new UserDAO().updateProfile(user.getId(), name, phone, address);
        user.setName(name);
        user.setPhone(phone);
        user.setAddress(address);
        flash(req, "Profile updated");
        resp.sendRedirect(ctx(req) + "/profile");
    }
}
