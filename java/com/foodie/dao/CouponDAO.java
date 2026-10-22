package com.foodie.dao;

import com.foodie.model.Coupon;
import com.foodie.util.DBConnection;
import com.foodie.util.DaoException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CouponDAO {

    private Coupon map(ResultSet rs) throws SQLException {
        Coupon c = new Coupon();
        c.setId(rs.getInt("id"));
        c.setCode(rs.getString("code"));
        c.setDescription(rs.getString("description"));
        c.setDiscountPercent(rs.getInt("discount_percent"));
        c.setMinOrder(rs.getDouble("min_order"));
        c.setActive(rs.getBoolean("active"));
        return c;
    }

    /** Returns the coupon only if it exists and is active. */
    public Coupon findActiveByCode(String code) {
        String sql = "SELECT * FROM coupons WHERE code = ? AND active = TRUE";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, code);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public List<Coupon> activeCoupons() {
        List<Coupon> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM coupons WHERE active = TRUE ORDER BY discount_percent DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
