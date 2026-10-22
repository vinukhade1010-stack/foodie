package com.foodie.servlet;

import com.foodie.dao.AdDAO;
import com.foodie.dao.CouponDAO;
import com.foodie.dao.FoodDAO;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/home")
public class HomeServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("ads", new AdDAO().activeAds());
        req.setAttribute("categories", new FoodDAO().categories());
        req.setAttribute("coupons", new CouponDAO().activeCoupons());
        render(req, resp, "home");
    }
}
