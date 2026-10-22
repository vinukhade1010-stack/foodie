package com.foodie.servlet;

import com.foodie.dao.OrderDAO;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/admin/dashboard")
public class AdminDashboardServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        OrderDAO dao = new OrderDAO();
        LocalDate today = LocalDate.now();

        req.setAttribute("totalSales", dao.totalSales());
        req.setAttribute("orderCount", dao.orderCount());
        req.setAttribute("todaySales", dao.salesOn(today));
        req.setAttribute("todayCount", dao.orderCountOn(today));
        req.setAttribute("activeCount", dao.activeOrderCount());
        req.setAttribute("todayOrders", dao.findByDate(today));
        req.setAttribute("today", today.toString());

        List<Object[]> top = dao.topItems(5);
        int best = top.isEmpty() ? 1 : (Integer) top.get(0)[1];
        req.setAttribute("topItems", top);
        req.setAttribute("topBest", best);
        render(req, resp, "admin/dashboard");
    }
}
