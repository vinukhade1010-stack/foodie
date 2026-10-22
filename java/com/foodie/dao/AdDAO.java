package com.foodie.dao;

import com.foodie.model.Ad;
import com.foodie.util.DBConnection;
import com.foodie.util.DaoException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

/** Banners for the home page carousel. */
public class AdDAO {

    private Ad map(ResultSet rs) throws SQLException {
        Ad a = new Ad();
        a.setId(rs.getInt("id"));
        a.setTitle(rs.getString("title"));
        a.setSubtitle(rs.getString("subtitle"));
        a.setTheme(rs.getString("theme"));
        a.setEmoji(rs.getString("emoji"));
        a.setImage(rs.getString("image"));
        a.setCouponCode(rs.getString("coupon_code"));
        int cat = rs.getInt("category_id");
        a.setCategoryId(rs.wasNull() ? null : cat);
        a.setStartDate(rs.getDate("start_date").toString());
        a.setEndDate(rs.getDate("end_date").toString());
        a.setActive(rs.getBoolean("active"));
        return a;
    }

    private List<Ad> query(String sql) {
        List<Ad> list = new ArrayList<>();
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
            return list;
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    /** Only banners that are switched on AND whose dates include today. Expired ones vanish by themselves. */
    public List<Ad> activeAds() {
        return query("SELECT * FROM ads WHERE active = TRUE AND CURDATE() BETWEEN start_date AND end_date ORDER BY id DESC");
    }

    public List<Ad> all() {
        return query("SELECT * FROM ads ORDER BY id DESC");
    }

    public Ad findById(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM ads WHERE id = ?")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void insert(Ad a) {
        String sql = "INSERT INTO ads (title, subtitle, theme, emoji, image, coupon_code, category_id, start_date, end_date, active) "
                   + "VALUES (?,?,?,?,?,?,?,?,?,?)";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, a);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void update(Ad a) {
        String sql = "UPDATE ads SET title=?, subtitle=?, theme=?, emoji=?, image=?, coupon_code=?, category_id=?, "
                   + "start_date=?, end_date=?, active=? WHERE id=?";
        try (Connection c = DBConnection.getConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            bind(ps, a);
            ps.setInt(11, a.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    private void bind(PreparedStatement ps, Ad a) throws SQLException {
        ps.setString(1, a.getTitle());
        ps.setString(2, a.getSubtitle());
        ps.setString(3, a.getTheme());
        ps.setString(4, a.getEmoji());
        ps.setString(5, a.getImage());
        ps.setString(6, a.getCouponCode());
        if (a.getCategoryId() == null) ps.setNull(7, Types.INTEGER); else ps.setInt(7, a.getCategoryId());
        ps.setDate(8, Date.valueOf(a.getStartDate()));
        ps.setDate(9, Date.valueOf(a.getEndDate()));
        ps.setBoolean(10, a.isActive());
    }

    public void setActive(int id, boolean active) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("UPDATE ads SET active = ? WHERE id = ?")) {
            ps.setBoolean(1, active);
            ps.setInt(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }

    public void delete(int id) {
        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM ads WHERE id = ?")) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException(e);
        }
    }
}
