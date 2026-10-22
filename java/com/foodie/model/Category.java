package com.foodie.model;

public class Category {
    private int id;
    private String name;
    private String emoji;

    public Category() { }
    public Category(int id, String name, String emoji) { this.id = id; this.name = name; this.emoji = emoji; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmoji() { return emoji; }
    public void setEmoji(String emoji) { this.emoji = emoji; }
}
