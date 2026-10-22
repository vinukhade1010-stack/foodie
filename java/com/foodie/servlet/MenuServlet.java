package com.foodie.servlet;

import com.foodie.dao.FoodDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** The menu page shell. The dishes themselves are loaded by JavaScript from /api/menu. */
@WebServlet("/menu")
public class MenuServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FoodDAO dao = new FoodDAO();
        req.setAttribute("categories", dao.categories());
        // slider maximum: highest price rounded up to the next 50
        int max = (int) (Math.ceil(dao.maxPrice() / 50.0) * 50);
        req.setAttribute("maxPrice", Math.max(max, 100));
        render(req, resp, "menu");
    }
}
