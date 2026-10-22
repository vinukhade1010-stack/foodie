package com.foodie.servlet;

import com.foodie.dao.OrderDAO;
import com.foodie.model.Order;
import com.foodie.model.User;
import com.foodie.util.FileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /receipt?id=12  downloads the .txt receipt. Only the order's owner (or an admin) may download it. */
@WebServlet("/receipt")
public class ReceiptServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        Order order = new OrderDAO().findById(intParam(req, "id", 0));
        if (order == null || (order.getUserId() != user.getId() && !user.isAdmin())) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        Path file = FileUtil.writeReceipt(order);   // always regenerate so the status on it is current
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"Foodie-Receipt-" + order.getId() + ".txt\"");
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }
}
