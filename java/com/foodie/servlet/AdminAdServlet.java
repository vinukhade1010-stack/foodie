package com.foodie.servlet;

import com.foodie.dao.AdDAO;
import com.foodie.dao.CouponDAO;
import com.foodie.dao.FoodDAO;
import com.foodie.model.Ad;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Admin ads manager: banners for the home page carousel. */
@WebServlet("/admin/ads")
public class AdminAdServlet extends BaseServlet {

    private static final List<String> THEMES = Arrays.asList("g1", "g2", "g3", "g4");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AdDAO dao = new AdDAO();
        int editId = intParam(req, "edit", 0);
        if (editId > 0) req.setAttribute("editAd", dao.findById(editId));
        req.setAttribute("ads", dao.all());
        req.setAttribute("categories", new FoodDAO().categories());
        req.setAttribute("coupons", new CouponDAO().activeCoupons());
        req.setAttribute("today", LocalDate.now().toString());
        render(req, resp, "admin/ads");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        AdDAO dao = new AdDAO();
        String action = param(req, "action");
        int id = intParam(req, "id", 0);

        switch (action) {
            case "delete":
                dao.delete(id);
                flash(req, "Banner deleted");
                break;
            case "toggle": {
                Ad a = dao.findById(id);
                if (a != null) {
                    dao.setActive(id, !a.isActive());
                    flash(req, a.isActive() ? "Banner switched off" : "Banner switched on");
                }
                break;
            }
            case "save": {
                String title = param(req, "title");
                String start = param(req, "startDate");
                String end = param(req, "endDate");
                boolean datesOk;
                try {
                    datesOk = !LocalDate.parse(end).isBefore(LocalDate.parse(start));
                } catch (DateTimeParseException e) {
                    datesOk = false;
                }
                if (title.isEmpty() || !datesOk) {
                    flash(req, "Banner not saved: add a title and make sure the end date is not before the start date.");
                    resp.sendRedirect(ctx(req) + "/admin/ads" + (id > 0 ? "?edit=" + id : ""));
                    return;
                }
                Ad a = new Ad();
                a.setId(id);
                a.setTitle(title);
                a.setSubtitle(param(req, "subtitle"));
                String theme = param(req, "theme");
                a.setTheme(THEMES.contains(theme) ? theme : "g1");
                String emoji = param(req, "emoji");
                a.setEmoji(emoji.isEmpty() ? "🍽️" : emoji);
                String image = param(req, "image");
                a.setImage(image.isEmpty() ? null : image);
                String code = param(req, "couponCode").toUpperCase();
                a.setCouponCode(code.isEmpty() ? null : code);
                int cat = intParam(req, "categoryId", 0);
                a.setCategoryId(cat > 0 ? cat : null);
                a.setStartDate(start);
                a.setEndDate(end);
                a.setActive(req.getParameter("active") != null);
                if (id > 0) dao.update(a); else dao.insert(a);
                flash(req, id > 0 ? "Banner updated" : "Banner added");
                break;
            }
            default:
                break;
        }
        resp.sendRedirect(ctx(req) + "/admin/ads");
    }
}
