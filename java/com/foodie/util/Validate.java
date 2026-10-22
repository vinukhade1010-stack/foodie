package com.foodie.util;

import java.util.regex.Pattern;

/** Server-side checks. The browser validates too, but the server never trusts the browser. */
public final class Validate {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[6-9][0-9]{9}$");

    private Validate() { }

    public static boolean email(String s) { return s != null && EMAIL.matcher(s).matches(); }
    public static boolean phone(String s) { return s != null && PHONE.matcher(s).matches(); }

    /** At least 8 characters with a letter and a digit. */
    public static boolean password(String s) {
        return s != null && s.length() >= 8
                && s.chars().anyMatch(Character::isLetter)
                && s.chars().anyMatch(Character::isDigit);
    }
}
