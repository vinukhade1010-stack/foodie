package com.foodie.util;

import com.foodie.model.Order;
import com.foodie.model.OrderItem;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;

/**
 * File handling: receipts and daily sales reports are written as .txt files
 * under  <your home folder>/foodie-data/receipts  and  .../reports
 * The same files are then streamed to the browser as downloads.
 */
public final class FileUtil {
    private static final String LINE = "------------------------------------------------\n";

    private FileUtil() { }

    private static Path folder(String sub) throws IOException {
        Path p = Paths.get(System.getProperty("user.home"), "foodie-data", sub);
        Files.createDirectories(p);
        return p;
    }

    public static Path writeReceipt(Order o) throws IOException {
        Path file = folder("receipts").resolve("receipt-" + o.getId() + ".txt");
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(LINE);
            w.write("                 FOODIE - RECEIPT\n");
            w.write(LINE);
            w.write(String.format("Order ID : #%d%n", o.getId()));
            w.write(String.format("Date     : %s%n", o.getCreatedAt()));
            w.write(String.format("Customer : %s%n", o.getCustomerName()));
            w.write(String.format("Deliver to: %s%n", o.getAddress()));
            w.write(String.format("Payment  : %s%n", o.getPaymentMethod()));
            w.write(String.format("Status   : %s%n", o.getStatusLabel()));
            w.write(LINE);
            w.write(String.format("%-26s %3s %8s %9s%n", "Item", "Qty", "Price", "Amount"));
            w.write(LINE);
            for (OrderItem i : o.getItems()) {
                w.write(String.format("%-26s %3d %8.2f %9.2f%n",
                        shorten(i.getItemName(), 26), i.getQuantity(), i.getPrice(), i.getLineTotal()));
            }
            w.write(LINE);
            w.write(String.format("%-39s %9.2f%n", "Subtotal", o.getSubtotal()));
            if (o.getDiscount() > 0) {
                String label = o.getCouponCode() != null ? "Discount (" + o.getCouponCode() + ")" : "Discount";
                w.write(String.format("%-39s -%8.2f%n", label, o.getDiscount()));
            }
            w.write(String.format("%-39s %9.2f%n", "GST (5%)", o.getGst()));
            w.write(String.format("%-39s %9.2f%n", "TOTAL (Rs.)", o.getTotal()));
            w.write(LINE);
            w.write("          Thank you for ordering with Foodie!\n");
        }
        return file;
    }

    public static Path writeDailyReport(LocalDate date, List<Order> orders, double sales) throws IOException {
        Path file = folder("reports").resolve("sales-report-" + date + ".txt");
        try (BufferedWriter w = Files.newBufferedWriter(file, StandardCharsets.UTF_8)) {
            w.write(LINE);
            w.write("            FOODIE - DAILY SALES REPORT\n");
            w.write(LINE);
            w.write("Date         : " + date + "\n");
            w.write("Orders       : " + orders.size() + "\n");
            w.write(String.format("Sales (Rs.)  : %.2f%n", sales));
            w.write("(cancelled orders are listed but not counted in sales)\n");
            w.write(LINE);
            w.write(String.format("%-6s %-18s %-10s %10s%n", "ID", "Customer", "Status", "Total"));
            w.write(LINE);
            for (Order o : orders) {
                w.write(String.format("%-6s %-18s %-10s %10.2f%n",
                        "#" + o.getId(), shorten(o.getCustomerName(), 18), o.getStatusLabel(), o.getTotal()));
            }
            w.write(LINE);
        }
        return file;
    }

    private static String shorten(String s, int max) {
        if (s == null) return "";
        return s.length() <= max ? s : s.substring(0, max - 1) + ".";
    }
}
