package com.foodie.servlet;

import com.foodie.dao.OrderDAO;
import com.foodie.model.Order;
import com.foodie.util.FileUtil;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** GET /admin/report?date=2026-10-11  writes the daily sales report file and downloads it (default: today). */
@WebServlet("/admin/report")
public class AdminReportServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LocalDate date;
        try {
            date = param(req, "date").isEmpty() ? LocalDate.now() : LocalDate.parse(param(req, "date"));
        } catch (DateTimeParseException e) {
            date = LocalDate.now();
        }
        OrderDAO dao = new OrderDAO();
        List<Order> orders = dao.findByDate(date);
        Path file = FileUtil.writeDailyReport(date, orders, dao.salesOn(date));

        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Content-Disposition", "attachment; filename=\"" + file.getFileName() + "\"");
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }
}
