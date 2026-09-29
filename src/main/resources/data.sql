-- Sample realistic data for Handloom & Artisan Marketplace
USE handloom_marketplace;

-- 1. Insert Initial Users (Passwords: admin123, artisan123, customer123)
-- Admin
INSERT INTO users (id, name, email, phone, password, role) VALUES
(1, 'System Administrator', 'admin@handloom.com', '9876500001', '$2a$10$55JvCt.HlbZ18.YY.9DgMuiOrlpb0Y3sFN92kq9go.X941mcOJrSy', 'ADMIN');

-- Artisans
INSERT INTO users (id, name, email, phone, password, role) VALUES
(2, 'Lakshmi Devi', 'lakshmi@artisan.com', '9443123456', '$2a$10$so5aT6IENYS/UZFNEzPELenhoWxrmFUrNb7lGh/jcYtLmgHg3tkHO', 'ARTISAN'),
(3, 'Ramesh Chandra Sharma', 'ramesh@artisan.com', '9414098765', '$2a$10$so5aT6IENYS/UZFNEzPELenhoWxrmFUrNb7lGh/jcYtLmgHg3tkHO', 'ARTISAN'),
(4, 'Shanti Devi Jha', 'shanti@artisan.com', '9835012345', '$2a$10$so5aT6IENYS/UZFNEzPELenhoWxrmFUrNb7lGh/jcYtLmgHg3tkHO', 'ARTISAN'),
(5, 'Abdul Rashid Mir', 'abdul@artisan.com', '9906543210', '$2a$10$so5aT6IENYS/UZFNEzPELenhoWxrmFUrNb7lGh/jcYtLmgHg3tkHO', 'ARTISAN');

-- Customers
INSERT INTO users (id, name, email, phone, password, role) VALUES
(6, 'Priya Sharma', 'priya@customer.com', '9876543210', '$2a$10$UVvvHTmgvbTOSvYKotPJsuZ9oc9hu7GgyQjYgYO14kb764Wrc7aRG', 'CUSTOMER'),
(7, 'Arun Patel', 'arun@customer.com', '9823456789', '$2a$10$UVvvHTmgvbTOSvYKotPJsuZ9oc9hu7GgyQjYgYO14kb764Wrc7aRG', 'CUSTOMER');

-- 2. Insert Artisans
INSERT INTO artisans (id, user_id, business_name, craft_type, location, description, status) VALUES
(1, 2, 'Lakshmi Handlooms', 'Handwoven Silk & Cotton Sarees', 'Coimbatore, Tamil Nadu', 'Master weaver heritage family with over 28 years in authentic Coimbatore soft silk and Kanchipuram weave traditions.', 'ACTIVE'),
(2, 3, 'Jaipur Craft Heritage', 'Hand Block Printing & Natural Dyes', 'Sanganer, Jaipur, Rajasthan', '5th generation artisan studio crafting vegetable-dyed block prints on pure cotton and chanderi fabrics.', 'ACTIVE'),
(3, 4, 'Mithila Folk Art Studio', 'Authentic Madhubani Painting & Crafts', 'Madhubani, Bihar', 'National award-winning artisan collective specializing in GI-tagged Madhubani artwork and natural crafts.', 'ACTIVE'),
(4, 5, 'Kashmir Valley Weaves', 'Pure Wool & Pashmina Shawls', 'Srinagar, Jammu & Kashmir', 'Preserving royal Sozni needle embroidery and traditional hand-spun wool shawl weaving of the Kashmir valley.', 'ACTIVE');

-- 3. Insert Product Categories
INSERT INTO categories (id, name, description, image_url) VALUES
(1, 'Handloom Sarees', 'Authentic handwoven silk and cotton sarees crafted on traditional pit looms.', '/images/categories/sarees.jpg'),
(2, 'Handwoven Clothing', 'Naturally dyed handspun kurtas, shirts, dupattas, and ethnic apparel.', '/images/categories/clothing.jpg'),
(3, 'Shawls', 'Luxurious pure wool, Pashmina, and hand-embroidered regional shawls and stoles.', '/images/categories/shawls.jpg'),
(4, 'Handicrafts', 'Traditional lost-wax brass, terracotta, and handcrafted artisan artifacts.', '/images/categories/handicrafts.jpg'),
(5, 'Home Decor', 'Hand-block printed bedspreads, quilted cushion covers, and natural home accents.', '/images/categories/homedecor.jpg'),
(6, 'Bags', 'Handcrafted embroidered potli pouches, block-printed totes, and natural jute bags.', '/images/categories/bags.jpg'),
(7, 'Jewellery', 'Eco-friendly terracotta jewellery and traditional tribal artisan ornaments.', '/images/categories/jewellery.jpg'),
(8, 'Traditional Art', 'Heritage Madhubani, Warli, and Pattachitra folk art created by master painters.', '/images/categories/art.jpg');

-- 4. Insert Realistic Artisan Products
INSERT INTO products (id, name, description, category_id, artisan_id, price, stock_quantity, image_url, material, origin_location, status) VALUES
(1, 'Kanchipuram Pure Mulberry Silk Saree', 'Exquisite pure mulberry silk saree featuring traditional peacock motifs and gold zari border. Hand-woven on ancestral pit looms by Lakshmi Handlooms over a span of 18 days.', 1, 1, 7499.00, 12, '/images/products/kanchipuram_saree.jpg', 'Pure Mulberry Silk & Zari', 'Coimbatore, Tamil Nadu', 'ACTIVE'),

(2, 'Handwoven Mangalagiri Cotton Saree with Nizam Border', 'Breathable 80s count combed cotton saree with distinctive golden zari Nizam temple border. Lightweight and comfortable for formal and casual occasions.', 1, 1, 2250.00, 18, '/images/products/mangalagiri_saree.jpg', '100% Combed Handloom Cotton', 'Coimbatore, Tamil Nadu', 'ACTIVE'),

(3, 'Jaipur Indigo Dabu Hand Block Print Kurta', 'Handcrafted men and unisex ethnic kurta dyed with 100% organic fermented indigo using the mud-resist Dabu block printing technique from Bagru.', 2, 2, 1450.00, 25, '/images/products/indigo_kurta.jpg', 'Pure Organic Cotton', 'Jaipur, Rajasthan', 'ACTIVE'),

(4, 'Bagru Block Printed Chanderi Silk Dupatta', 'Lightweight sheer Chanderi silk cotton dupatta adorned with intricate floral and geometric wooden block impressions in natural madder red and black.', 2, 2, 1199.00, 15, '/images/products/chanderi_dupatta.jpg', 'Chanderi Silk Cotton', 'Jaipur, Rajasthan', 'ACTIVE'),

(5, 'Authentic Sozni Embroidered Kashmiri Wool Shawl', 'Masterpiece fine merino wool shawl enriched with delicate Kashmiri needlework sozni embroidery along the four-sided borders. Provides exceptional warmth and elegance.', 3, 4, 4800.00, 8, '/images/products/kashmiri_shawl.jpg', 'Fine Merino Wool', 'Srinagar, Jammu & Kashmir', 'ACTIVE'),

(6, 'Handcrafted Kullu Geometric Border Stole', 'Pure Himalayan sheep wool stole hand-loomed with vibrant traditional Himachal geometric temple patterns at the fringes.', 3, 4, 1850.00, 14, '/images/products/kullu_stole.jpg', 'Himalayan Sheep Wool', 'Srinagar, Jammu & Kashmir', 'ACTIVE'),

(7, 'Dokra Lost-Wax Brass Tribal Figurine', 'Ancient 4,000-year-old lost-wax casting technique hand-cast brass sculpture depicting traditional village musicians and folk dancers.', 4, 3, 1650.00, 10, '/images/products/dokra_figurine.jpg', 'Bell Metal & Brass', 'Madhubani, Bihar', 'ACTIVE'),

(8, 'Terracotta Handcrafted Floral Decorative Vase', 'Wheel-thrown natural river clay flower vase with hand-carved floral openwork and terracotta burnished finish.', 5, 2, 899.00, 20, '/images/products/terracotta_vase.jpg', 'Natural Riverbed Clay', 'Jaipur, Rajasthan', 'ACTIVE'),

(9, 'Hand Block Printed Cotton Kantha Cushion Covers (Set of 2)', 'Pair of 16x16 inch reversible cushion covers with traditional Sanganeri botanical prints and detailed hand-stitched running Kantha quilting.', 5, 2, 750.00, 30, '/images/products/kantha_cushions.jpg', '100% Cotton with Hand Quilting', 'Jaipur, Rajasthan', 'ACTIVE'),

(10, 'Embroidered Raw Silk Festive Potli Bag', 'Opulent raw silk drawstring potli pouch embellished with zari work, bead fringes, and braided drawstring with matching fabric latkans.', 6, 1, 650.00, 22, '/images/products/silk_potli.jpg', 'Raw Silk & Metallic Thread', 'Coimbatore, Tamil Nadu', 'ACTIVE'),

(11, 'Eco-friendly Handwoven Braided Jute Tote Bag', 'Durable, sustainable everyday tote bag woven from unbleached natural golden jute fiber with reinforced cotton webbed handles.', 6, 3, 550.00, 40, '/images/products/jute_tote.jpg', 'Natural Golden Jute', 'Madhubani, Bihar', 'ACTIVE'),

(12, 'Original Handmade Madhubani Tree of Life Painting', 'Authentic GI-certified Madhubani painting created on handmade paper with bamboo nibs using organic pigments derived from turmeric, indigo, and soot.', 8, 3, 3200.00, 5, '/images/products/madhubani_art.jpg', 'Natural Mineral Dyes on Handmade Paper', 'Madhubani, Bihar', 'ACTIVE');

-- 5. Seed Customer Carts
INSERT INTO carts (id, customer_id) VALUES
(1, 6),
(2, 7);

-- Seed a cart item for Priya
INSERT INTO cart_items (cart_id, product_id, quantity) VALUES
(1, 2, 1);

-- 6. Seed Sample Order and Items
INSERT INTO orders (id, order_number, customer_id, total_amount, order_status, customer_name, phone, delivery_address, city, state, pin_code, created_at) VALUES
(1, 'ORD-2026-0001', 6, 2250.00, 'CONFIRMED', 'Priya Sharma', '9876543210', 'Flat 402, Lotus Greens, Indiranagar', 'Bengaluru', 'Karnataka', '560038', '2026-09-25 10:30:00'),
(2, 'ORD-2026-0002', 7, 1450.00, 'SHIPPED', 'Arun Patel', '9823456789', '12, Sunrise Avenue, Race Course Road', 'Coimbatore', 'Tamil Nadu', '641018', '2026-09-26 14:15:00');

INSERT INTO order_items (id, order_id, product_id, artisan_id, product_name, unit_price, quantity, subtotal) VALUES
(1, 1, 2, 1, 'Handwoven Mangalagiri Cotton Saree with Nizam Border', 2250.00, 1, 2250.00),
(2, 2, 3, 2, 'Jaipur Indigo Dabu Hand Block Print Kurta', 1450.00, 1, 1450.00);

-- 7. Seed Payments
INSERT INTO payments (id, order_id, amount, payment_method, payment_status, payment_date) VALUES
(1, 1, 2250.00, 'COD', 'PENDING', '2026-09-25 10:30:00'),
(2, 2, 1450.00, 'ONLINE_DEMO', 'PAID', '2026-09-26 14:15:00');

-- 8. Seed Customer Reviews
INSERT INTO reviews (id, product_id, customer_id, rating, comment, review_date) VALUES
(1, 1, 6, 5, 'The Kanchipuram silk quality is unmatched! The weave is authentic and the colors are stunning.', '2026-09-20 11:20:00'),
(2, 3, 7, 5, 'Truly pure indigo block print. Extremely breathable fabric and perfect stitching. Kudos to the artisan!', '2026-09-21 16:45:00'),
(3, 5, 6, 5, 'Exquisite Sozni embroidery. It arrived beautifully packed directly from the artisan in Srinagar.', '2026-09-22 09:15:00');
