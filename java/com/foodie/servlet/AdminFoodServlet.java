package com.foodie.servlet;

import com.foodie.dao.FoodDAO;
import com.foodie.model.FoodItem;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Admin menu manager: list, add, edit (?edit=ID), delete, and the sold-out switch. */
@WebServlet("/admin/foods")
public class AdminFoodServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FoodDAO dao = new FoodDAO();
        int editId = intParam(req, "edit", 0);
        if (editId > 0) req.setAttribute("editItem", dao.findById(editId));
        req.setAttribute("foods", dao.search(null, null, null, null, false, false, 0));
        req.setAttribute("categories", dao.categories());
        render(req, resp, "admin/foods");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        FoodDAO dao = new FoodDAO();
        String action = param(req, "action");
        int id = intParam(req, "id", 0);

        switch (action) {
            case "delete":
                dao.delete(id);
                flash(req, "Dish deleted");
                break;
            case "toggle": {
                FoodItem f = dao.findById(id);
                if (f != null) {
                    dao.setAvailable(id, !f.isAvailable());
                    flash(req, f.getName() + (f.isAvailable() ? " marked sold out" : " is available again"));
                }
                break;
            }
            case "save": {
                String name = param(req, "name");
                double price;
                try {
                    price = Double.parseDouble(param(req, "price"));
                } catch (NumberFormatException e) {
                    price = -1;
                }
                int categoryId = intParam(req, "categoryId", 0);
                if (name.isEmpty() || price <= 0 || price > 100000 || categoryId <= 0) {
                    flash(req, "Dish not saved: enter a name, a category and a price above 0.");
                    resp.sendRedirect(ctx(req) + "/admin/foods" + (id > 0 ? "?edit=" + id : ""));
                    return;
                }
                FoodItem f = new FoodItem();
                f.setId(id);
                f.setName(name);
                f.setCategoryId(categoryId);
                f.setDescription(param(req, "description"));
                f.setPrice(price);
                f.setVeg(req.getParameter("veg") != null);
                f.setEmoji(param(req, "emoji"));
                String image = param(req, "image");
                f.setImage(image.isEmpty() ? null : image);
                f.setFeatured(req.getParameter("featured") != null);
                f.setAvailable(req.getParameter("available") != null);
                if (id > 0) dao.update(f); else dao.insert(f);
                flash(req, id > 0 ? "Dish updated" : "Dish added");
                break;
            }
            default:
                break;
        }
        resp.sendRedirect(ctx(req) + "/admin/foods");
    }
}
