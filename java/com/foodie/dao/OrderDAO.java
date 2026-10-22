package com.foodie.dao;

import com.foodie.model.Order;
import com.foodie.model.OrderItem;
import com.foodie.util.DBConnection;
import com.foodie.util.DaoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Orders, order items and the numbers shown on the admin dashboard. */
public class OrderDAO {

    private static final List<String> STATUSES =
            Arrays.asList(Order.PLACED, Order.PREPARING, Order.DELIVERED, Order.CANCELLED);

    private static final String SELECT_ORDER =
            "SELECT o.*, u.name AS customer_name FROM orders o JOIN users u ON u.id = o.user_id ";

    private Order map(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setUserId(rs.getInt("user_id"));
        o.setCustomerName(rs.getString("customer_name"));
        o.setSubtotal(rs.getDouble("subtotal"));
        o.setGst(rs.getDouble("gst"));
        o.setDiscount(rs.getDouble("discount"));
        o.setTotal(rs.getDouble("total"));
        o.setCouponCode(rs.getString("coupon_code"));
        o.setPaymentMethod(rs.getString("payment_method"));
        o.setAddress(rs.getString("address"));
        o.setStatus(rs.getString("status"));
        Timestamp ts = rs.getTimestamp("created_at");
        o.setCreatedAt(ts == null ? "" : new SimpleDateFormat("dd MMM yyyy, hh:mm a").format(ts));
        return o;
    }

    private void loadItems(Connection c, Order o) throws SQLException {
        String sql = "SELECT food_item_id, item_name, quantity, price FROM order_items WHERE order_id = ? ORDER BY id";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, o.getId());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem i = new OrderItem();
                    i.setFoodItemId(rs.getInt("food_item_id"));
                    i.setItemName(rs.getString("item_name"));
                    i.setQuantity(rs.getInt("quantity"));
                    i.setPrice(rs.getDouble("price"));
                    o.getItems().add(i);
                }
            }
        }
    }

    /**
     * Saves the order and all its items in ONE transaction:
     * either everything is saved, or (if anything fails) nothing is.
     */
    public int placeOrder(Order o) {
        String insertOrder = "INSERT INTO orders (user_id, subtotal, gst, discount, total, coupon_code, payment_method, address, status) "
                           + "VALUES (?,?,?,?,?,?,?,?, 'PLACED')";
        String insertItem = "INSERT INTO order_items (order_id, food_item_id, item_name, quantity, price) VALUES (?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection()) {
            c.setAutoCommit(false);
            try {
                int orderId;
                try (PreparedStatement ps = c.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, o.getUserId());
                    ps.setDouble(2, o.getSubtotal());
                    ps.setDouble(3, o.getGst());
                    ps.setDouble(4, o.getDiscount());
                    ps.setDouble(5, o.getTotal());
                    ps.setString(6, o.getCouponCode());
                    ps.setString(7, o.getPaymentMethod());
                    ps.setString(8, o.getAddress());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        orderId = keys.getInt(1);
                    }
                }
                try (PreparedStatement ps = c.prepareStatement(insertItem)) {
                    for (OrderItem i : o.getItems()) {
                        ps.setInt(1, orderId);
                        ps.setInt(2, i.getFoodItemId());
                        ps.setString(3, i.getItemName());
                        ps.setInt(4, i.getQuantity());
                        ps.setDouble(5, i.getPrice());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }
                c.commit();
                return orderId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public Order findById(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(SELECT_ORDER + "WHERE o.id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                Order o = map(rs);
                loadItems(c, o);
                return o;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private List<Order> list(String sql, Object param) {
        List<Order> orders = new ArrayList<>();
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) orders.add(map(rs));
            }
            for (Order o : orders) loadItems(c, o);
            return orders;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Order> findByUser(int userId) {
        return list(SELECT_ORDER + "WHERE o.user_id = ? ORDER BY o.id DESC", userId);
    }

    /** status may be null (all orders) or one of PLACED / PREPARING / DELIVERED / CANCELLED. */
    public List<Order> findAll(String status) {
        if (status == null || !STATUSES.contains(status)) return list(SELECT_ORDER + "ORDER BY o.id DESC", null);
        return list(SELECT_ORDER + "WHERE o.status = ? ORDER BY o.id DESC", status);
    }

    public List<Order> findByDate(LocalDate date) {
        return list(SELECT_ORDER + "WHERE DATE(o.created_at) = ? ORDER BY o.id", java.sql.Date.valueOf(date));
    }

    public void updateStatus(int orderId, String status) {
        if (!STATUSES.contains(status)) return;
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE orders SET status = ? WHERE id = ?")) {
            ps.setString(1, status);
            ps.setInt(2, orderId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    // ------------------------- dashboard numbers -------------------------

    private double scalarDouble(String sql, Object param) {
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            if (param != null) ps.setObject(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    /** Sales never include cancelled orders. */
    public double totalSales() {
        return scalarDouble("SELECT COALESCE(SUM(total),0) FROM orders WHERE status <> 'CANCELLED'", null);
    }

    public double salesOn(LocalDate date) {
        return scalarDouble("SELECT COALESCE(SUM(total),0) FROM orders WHERE status <> 'CANCELLED' AND DATE(created_at) = ?",
                java.sql.Date.valueOf(date));
    }

    public int orderCount() {
        return (int) scalarDouble("SELECT COUNT(*) FROM orders", null);
    }

    public int orderCountOn(LocalDate date) {
        return (int) scalarDouble("SELECT COUNT(*) FROM orders WHERE DATE(created_at) = ?", java.sql.Date.valueOf(date));
    }

    public int activeOrderCount() {
        return (int) scalarDouble("SELECT COUNT(*) FROM orders WHERE status IN ('PLACED','PREPARING')", null);
    }

    /** Best sellers: each row is {item name, quantity sold}. */
    public List<Object[]> topItems(int limit) {
        String sql = "SELECT oi.item_name, SUM(oi.quantity) AS qty FROM order_items oi "
                   + "JOIN orders o ON o.id = oi.order_id WHERE o.status <> 'CANCELLED' "
                   + "GROUP BY oi.item_name ORDER BY qty DESC LIMIT " + limit;
        List<Object[]> rows = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) rows.add(new Object[]{rs.getString(1), rs.getInt(2)});
            return rows;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
