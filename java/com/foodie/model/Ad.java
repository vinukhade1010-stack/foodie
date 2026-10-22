package com.foodie.model;

/** A promotional banner shown in the home page carousel. */
public class Ad {
    private int id;
    private String title;
    private String subtitle;
    private String theme;        // g1..g4, picks the banner colours in CSS
    private String emoji;
    private String image;
    private String couponCode;
    private Integer categoryId;  // optional: "Order now" opens this category
    private String startDate;    // yyyy-MM-dd
    private String endDate;      // yyyy-MM-dd
    private boolean active;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSubtitle() { return subtitle; }
    public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
    public String getTheme() { return theme; }
    public void setTheme(String theme) { this.theme = theme; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
    public String getImage() { return image; }
    public void setImage(String image) { this.image = image; }
    public String getCouponCode() { return couponCode; }
    public void setCouponCode(String couponCode) { this.couponCode = couponCode; }
    public Integer getCategoryId() { return categoryId; }
    public void setCategoryId(Integer categoryId) { this.categoryId = categoryId; }
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }
    public String getEndDate() { return endDate; }
    public void setEndDate(String endDate) { this.endDate = endDate; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
