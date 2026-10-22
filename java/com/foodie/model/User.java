package com.foodie.model;

import java.io.Serializable;

/** A registered person. Role is CUSTOMER or ADMIN. */
public class User implements Serializable {
    private int id;
    private String name;
    private String email;
    private String passwordHash;
    private String phone;
    private String address;
    private String role;

    public boolean isAdmin() { return "ADMIN".equals(role); }
    /** First word of the name, used in the navbar greeting. */
    public String getFirstName() { return name == null ? "" : name.trim().split("\\s+")[0]; }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
