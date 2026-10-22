package com.foodie.servlet;

import com.foodie.dao.FoodDAO;
import com.foodie.model.User;
import com.foodie.util.Json;
import java.io.IOException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** POST /api/favorite  id=5  -> toggles the heart for the logged-in user. */
@WebServlet("/api/favorite")
public class FavoriteApiServlet extends BaseServlet {
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User user = currentUser(req);
        if (user == null) {
            Json.send(resp, 401, Json.obj("ok", false, "loginRequired", true, "error", "Log in to save favorites."));
            return;
        }
        int id = intParam(req, "id", 0);
        if (new FoodDAO().findById(id) == null) {
            Json.send(resp, 404, Json.obj("ok", false, "error", "Dish not found."));
            return;
        }
        boolean now = new FoodDAO().toggleFavorite(user.getId(), id);
        Json.send(resp, 200, Json.obj("ok", true, "favorite", now));
    }
}
