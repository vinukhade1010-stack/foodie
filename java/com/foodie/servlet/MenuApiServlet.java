package com.foodie.servlet;

import com.foodie.dao.FoodDAO;
import com.foodie.model.FoodItem;
import com.foodie.model.User;
import com.foodie.util.Json;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * GET /api/menu?q=pizza&cat=1&type=veg&max=300&featured=true&fav=true&limit=6
 * Every parameter is optional. Replies with JSON: {"items":[...], "loggedIn":true}
 */
@WebServlet("/api/menu")
public class MenuApiServlet extends BaseServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = param(req, "q");
        int cat = intParam(req, "cat", 0);
        String type = param(req, "type");
        Boolean veg = "veg".equals(type) ? Boolean.TRUE : "nonveg".equals(type) ? Boolean.FALSE : null;

        Double max = null;
        try {
            if (!param(req, "max").isEmpty()) max = Double.valueOf(param(req, "max"));
        } catch (NumberFormatException ignored) { }

        boolean featured = "true".equals(param(req, "featured"));
        boolean favOnly = "true".equals(param(req, "fav"));
        int limit = Math.min(intParam(req, "limit", 0), 100);

        FoodDAO dao = new FoodDAO();
        List<FoodItem> items = dao.search(q, cat > 0 ? cat : null, veg, max, featured, false, limit);

        User user = currentUser(req);
        Set<Integer> favorites = user == null ? Collections.<Integer>emptySet() : dao.favoriteIds(user.getId());
        for (FoodItem f : items) f.setFavorite(favorites.contains(f.getId()));
        if (favOnly) items.removeIf(f -> !f.isFavorite());

        Json.send(resp, 200, Json.obj("items", items, "loggedIn", user != null));
    }
}
