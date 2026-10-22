-- =====================================================================
--  Foodie  |  MySQL 8  |  run this whole file once (MySQL Workbench or CLI)
--  mysql -u root -p < sql/foodie.sql
-- =====================================================================
DROP DATABASE IF EXISTS foodie_db;
CREATE DATABASE foodie_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE foodie_db;

CREATE TABLE users (
  id            INT AUTO_INCREMENT PRIMARY KEY,
  name          VARCHAR(80)  NOT NULL,
  email         VARCHAR(120) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone         VARCHAR(15),
  address       VARCHAR(255),
  role          ENUM('CUSTOMER','ADMIN') NOT NULL DEFAULT 'CUSTOMER',
  created_at    TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categories (
  id    INT AUTO_INCREMENT PRIMARY KEY,
  name  VARCHAR(50) NOT NULL UNIQUE,
  emoji VARCHAR(8)  NOT NULL
);

CREATE TABLE food_items (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  category_id INT NOT NULL,
  description VARCHAR(255) NOT NULL DEFAULT '',
  price       DECIMAL(8,2) NOT NULL,
  is_veg      BOOLEAN NOT NULL DEFAULT TRUE,
  emoji       VARCHAR(8),
  image       VARCHAR(500),
  featured    BOOLEAN NOT NULL DEFAULT FALSE,
  available   BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES categories(id)
);

CREATE TABLE coupons (
  id               INT AUTO_INCREMENT PRIMARY KEY,
  code             VARCHAR(30) NOT NULL UNIQUE,
  description      VARCHAR(120) NOT NULL,
  discount_percent INT NOT NULL,
  min_order        DECIMAL(8,2) NOT NULL DEFAULT 0,
  active           BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE orders (
  id             INT AUTO_INCREMENT PRIMARY KEY,
  user_id        INT NOT NULL,
  subtotal       DECIMAL(10,2) NOT NULL,
  gst            DECIMAL(10,2) NOT NULL,
  discount       DECIMAL(10,2) NOT NULL DEFAULT 0,
  total          DECIMAL(10,2) NOT NULL,
  coupon_code    VARCHAR(30),
  payment_method VARCHAR(20) NOT NULL DEFAULT 'COD',
  address        VARCHAR(255) NOT NULL,
  status         ENUM('PLACED','PREPARING','DELIVERED','CANCELLED') NOT NULL DEFAULT 'PLACED',
  created_at     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);

-- item_name and price are copied into the order so old orders stay correct
-- even if the menu item is renamed, re-priced or deleted later.
CREATE TABLE order_items (
  id           INT AUTO_INCREMENT PRIMARY KEY,
  order_id     INT NOT NULL,
  food_item_id INT NULL,
  item_name    VARCHAR(100) NOT NULL,
  quantity     INT NOT NULL,
  price        DECIMAL(8,2) NOT NULL,
  FOREIGN KEY (order_id)     REFERENCES orders(id) ON DELETE CASCADE,
  FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE SET NULL
);

CREATE TABLE favorites (
  user_id      INT NOT NULL,
  food_item_id INT NOT NULL,
  PRIMARY KEY (user_id, food_item_id),
  FOREIGN KEY (user_id)      REFERENCES users(id)      ON DELETE CASCADE,
  FOREIGN KEY (food_item_id) REFERENCES food_items(id) ON DELETE CASCADE
);

CREATE TABLE ads (
  id          INT AUTO_INCREMENT PRIMARY KEY,
  title       VARCHAR(100) NOT NULL,
  subtitle    VARCHAR(200) NOT NULL DEFAULT '',
  theme       VARCHAR(4)   NOT NULL DEFAULT 'g1',   -- g1..g4 = banner colour theme
  emoji       VARCHAR(8)   NOT NULL DEFAULT '🍽️',
  image       VARCHAR(500),
  coupon_code VARCHAR(30),
  category_id INT NULL,
  start_date  DATE NOT NULL,
  end_date    DATE NOT NULL,
  active      BOOLEAN NOT NULL DEFAULT TRUE,
  FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE SET NULL
);

-- ----------------------------- sample data -----------------------------
-- Admin login:  admin@foodie.com  /  Admin@123
INSERT INTO users (name, email, password_hash, phone, address, role) VALUES
('Foodie Admin', 'admin@foodie.com', '65536:a1b2c3d4e5f60718293a4b5c6d7e8f90:36bb2fcee61e58a6feef9fd4e24c3cc6c56a49dfecaa6f1860710bfce54824bc', '9999999999', 'Foodie HQ, Pune', 'ADMIN');

INSERT INTO categories (name, emoji) VALUES
('Pizza','🍕'), ('Burgers','🍔'), ('Biryani','🍛'), ('Drinks','🥤'), ('Desserts','🍰'), ('Combos','🎁');

INSERT INTO food_items (name, category_id, description, price, is_veg, emoji, featured) VALUES
-- Pizza (1)
('Margherita',            1, 'Classic tomato sauce, mozzarella and fresh basil.',            199, TRUE,  '🍕', TRUE),
('Paneer Tikka Pizza',    1, 'Tandoori paneer, onion and capsicum on a smoky base.',          279, TRUE,  '🍕', TRUE),
('Farmhouse Pizza',       1, 'Mushroom, corn, olives and capsicum with extra cheese.',        299, TRUE,  '🍕', FALSE),
('Chicken Sausage Pizza', 1, 'Chicken sausage, jalapeno and mozzarella.',                     329, FALSE, '🍕', FALSE),
('Pepperoni Feast',       1, 'Loaded with spicy pepperoni and double cheese.',                349, FALSE, '🍕', FALSE),
-- Burgers (2)
('Aloo Tikki Burger',     2, 'Crispy potato patty, mint mayo and fresh veggies.',              99, TRUE,  '🍔', FALSE),
('Crispy Veg Burger',     2, 'Golden veg patty with lettuce, tomato and cheese slice.',       129, TRUE,  '🍔', FALSE),
('Paneer Zinger',         2, 'Crunchy paneer fillet with peri-peri sauce.',                    169, TRUE,  '🍔', FALSE),
('Classic Chicken Burger',2, 'Juicy fried chicken fillet with creamy mayo.',                   159, FALSE, '🍔', TRUE),
('Double Patty Chicken',  2, 'Two grilled chicken patties, cheese and smoky sauce.',           219, FALSE, '🍔', FALSE),
('French Fries',          2, 'Salted, crispy and served hot.',                                  89, TRUE,  '🍟', FALSE),
-- Biryani (3)
('Veg Dum Biryani',       3, 'Basmati rice slow-cooked with vegetables and whole spices.',     189, TRUE,  '🍛', FALSE),
('Paneer Biryani',        3, 'Fragrant dum biryani layered with marinated paneer.',            229, TRUE,  '🍛', FALSE),
('Chicken Dum Biryani',   3, 'Hyderabadi style, sealed and cooked on slow heat.',              249, FALSE, '🍛', TRUE),
('Mutton Biryani',        3, 'Tender mutton pieces with saffron rice and raita.',              329, FALSE, '🍛', FALSE),
('Egg Biryani',           3, 'Spiced rice with boiled eggs and fried onions.',                 179, FALSE, '🥚', FALSE),
-- Drinks (4)
('Masala Chaas',          4, 'Spiced buttermilk with roasted cumin.',                           49, TRUE,  '🥛', FALSE),
('Fresh Lime Soda',       4, 'Sweet or salted, your choice.',                                   59, TRUE,  '🍋', FALSE),
('Cold Coffee',           4, 'Thick, chilled and lightly sweet.',                              119, TRUE,  '☕', FALSE),
('Mango Shake',           4, 'Alphonso mango blended with cold milk.',                         129, TRUE,  '🥭', TRUE),
('Cola',                  4, 'Chilled 300 ml bottle.',                                          60, TRUE,  '🥤', FALSE),
-- Desserts (5)
('Gulab Jamun (2 pcs)',   5, 'Warm, soft and soaked in cardamom syrup.',                        69, TRUE,  '🍡', FALSE),
('Brownie with Ice Cream',5, 'Fudgy brownie topped with a vanilla scoop.',                     149, TRUE,  '🍫', FALSE),
('Chocolate Lava Cake',   5, 'Molten chocolate centre, served warm.',                          129, TRUE,  '🍰', TRUE),
('Rasmalai (2 pcs)',      5, 'Soft cheese dumplings in saffron milk.',                          99, TRUE,  '🥣', FALSE),
('Kulfi Falooda',         5, 'Pistachio kulfi with vermicelli and rose syrup.',                109, TRUE,  '🍧', FALSE),
-- Combos (6)
('Burger Buddy Combo',    6, 'Classic Chicken Burger + French Fries + Cola.',                  249, FALSE, '🎁', TRUE),
('Veg Feast Combo',       6, 'Paneer Zinger + French Fries + Fresh Lime Soda.',                279, TRUE,  '🎁', FALSE),
('Pizza Party for Two',   6, 'Two medium pizzas (Margherita + Farmhouse) + two drinks.',       549, TRUE,  '🍕', FALSE),
('Biryani Box',           6, 'Chicken Dum Biryani + Gulab Jamun + Cola.',                      329, FALSE, '🍱', FALSE),
('Sweet Treat Duo',       6, 'Chocolate Lava Cake + Brownie with Ice Cream.',                  189, TRUE,  '🍰', FALSE);

INSERT INTO coupons (code, description, discount_percent, min_order) VALUES
('WELCOME20', 'New here? Take 20% off your order.',        20, 250),
('PIZZA15',   'Pizza night: 15% off on orders above 300.', 15, 300),
('BIRYANI10', '10% off when your order is above 400.',     10, 400),
('SWEET10',   '10% off for the sweet tooth.',              10, 150);

INSERT INTO ads (title, subtitle, theme, emoji, coupon_code, category_id, start_date, end_date) VALUES
('20% off your first order',  'Use WELCOME20 on orders above ₹250.',          'g1', '🍔', 'WELCOME20', NULL, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
('Pizza night, 15% cheaper',  'Hot, cheesy and lighter on the wallet with PIZZA15.', 'g2', '🍕', 'PIZZA15', 1, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
('Dum biryani, slow-cooked',  'Get 10% off with BIRYANI10 on orders above ₹400.', 'g3', '🍛', 'BIRYANI10', 3, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY)),
('Dessert o''clock',          'Lava cake, kulfi and more. 10% off with SWEET10.', 'g4', '🍰', 'SWEET10', 5, CURDATE(), DATE_ADD(CURDATE(), INTERVAL 90 DAY));
