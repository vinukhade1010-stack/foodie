package com.foodie.dao;

import com.foodie.model.Category;
import com.foodie.model.FoodItem;
import com.foodie.util.DBConnection;
import com.foodie.util.DaoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Menu items, categories and favorites. */
public class FoodDAO {

    private static final String SELECT_FOOD =
            "SELECT f.*, c.name AS cat_name, c.emoji AS cat_emoji "
          + "FROM food_items f JOIN categories c ON c.id = f.category_id ";

    private FoodItem map(ResultSet rs) throws SQLException {
        FoodItem f = new FoodItem();
        f.setId(rs.getInt("id"));
        f.setName(rs.getString("name"));
        f.setCategoryId(rs.getInt("category_id"));
        f.setCategoryName(rs.getString("cat_name"));
        f.setDescription(rs.getString("description"));
        f.setPrice(rs.getDouble("price"));
        f.setVeg(rs.getBoolean("is_veg"));
        String emoji = rs.getString("emoji");
        f.setEmoji(emoji == null || emoji.isEmpty() ? rs.getString("cat_emoji") : emoji);
        f.setImage(rs.getString("image"));
        f.setFeatured(rs.getBoolean("featured"));
        f.setAvailable(rs.getBoolean("available"));
        return f;
    }

    public List<Category> categories() {
        String sql = "SELECT id, name, emoji FROM categories ORDER BY id";
        List<Category> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(new Category(rs.getInt("id"), rs.getString("name"), rs.getString("emoji")));
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    /**
     * Builds the WHERE clause piece by piece, only for the filters the customer used.
     * Values are never glued into the SQL text; they go in as ? parameters (no SQL injection).
     * Pass null for a filter you do not want. limit 0 means no limit.
     */
    public List<FoodItem> search(String q, Integer categoryId, Boolean veg, Double maxPrice,
                                 boolean featuredOnly, boolean availableOnly, int limit) {
        StringBuilder sql = new StringBuilder(SELECT_FOOD).append("WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (q != null && !q.isEmpty()) {
            sql.append("AND (f.name LIKE ? OR f.description LIKE ? OR c.name LIKE ?) ");
            String like = "%" + q + "%";
            params.add(like);
            params.add(like);
            params.add(like);
        }
        if (categoryId != null) { sql.append("AND f.category_id = ? "); params.add(categoryId); }
        if (veg != null)        { sql.append("AND f.is_veg = ? ");      params.add(veg); }
        if (maxPrice != null)   { sql.append("AND f.price <= ? ");      params.add(maxPrice); }
        if (featuredOnly)       { sql.append("AND f.featured = TRUE "); }
        if (availableOnly)      { sql.append("AND f.available = TRUE "); }
        sql.append("ORDER BY f.featured DESC, f.category_id, f.name ");
        if (limit > 0) sql.append("LIMIT ").append(limit);   // limit is an int, safe to append

        List<FoodItem> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) ps.setObject(i + 1, params.get(i));
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public FoodItem findById(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_FOOD + "WHERE f.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public double maxPrice() {
        String sql = "SELECT COALESCE(MAX(price), 0) FROM food_items";
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void insert(FoodItem f) {
        String sql = "INSERT INTO food_items (name, category_id, description, price, is_veg, emoji, image, featured, available) "
                   + "VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, f);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void update(FoodItem f) {
        String sql = "UPDATE food_items SET name=?, category_id=?, description=?, price=?, is_veg=?, emoji=?, image=?, "
                   + "featured=?, available=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, f);
            ps.setInt(10, f.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private void bind(PreparedStatement ps, FoodItem f) throws SQLException {
        ps.setString(1, f.getName());
        ps.setInt(2, f.getCategoryId());
        ps.setString(3, f.getDescription());
        ps.setDouble(4, f.getPrice());
        ps.setBoolean(5, f.isVeg());
        ps.setString(6, f.getEmoji());
        ps.setString(7, f.getImage());
        ps.setBoolean(8, f.isFeatured());
        ps.setBoolean(9, f.isAvailable());
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM food_items WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void setAvailable(int id, boolean available) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE food_items SET available = ? WHERE id = ?")) {
            ps.setBoolean(1, available);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    // ------------------------------ favorites ------------------------------

    public Set<Integer> favoriteIds(int userId) {
        Set<Integer> ids = new HashSet<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT food_item_id FROM favorites WHERE user_id = ?")) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) ids.add(rs.getInt(1));
            }
            return ids;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    /** Adds the favorite if missing, removes it if present. Returns true if it is now a favorite. */
    public boolean toggleFavorite(int userId, int foodId) {
        try (Connection c = DBConnection.getConnection()) {
            try (PreparedStatement del = c.prepareStatement("DELETE FROM favorites WHERE user_id = ? AND food_item_id = ?")) {
                del.setInt(1, userId);
                del.setInt(2, foodId);
                if (del.executeUpdate() > 0) return false;
            }
            try (PreparedStatement ins = c.prepareStatement("INSERT INTO favorites (user_id, food_item_id) VALUES (?, ?)")) {
                ins.setInt(1, userId);
                ins.setInt(2, foodId);
                ins.executeUpdate();
                return true;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
