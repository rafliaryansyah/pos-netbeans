-- SQL script to add image_path column to products table
-- Run this script in your MySQL database

USE s4_p1_pos;

-- Add image_path column to products table
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `price` int NOT NULL,
  `category` varchar(255) NOT NULL,
  `unit` varchar(255) NOT NULL,
  `stock` int NOT NULL,
  `image_path` varchar(512) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transaction_details` (
  `id` int NOT NULL AUTO_INCREMENT,
  `transaction_id` int NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(255) NOT NULL COMMENT 'Nama produk saat transaksi',
  `price` decimal(15,2) NOT NULL COMMENT 'Harga saat transaksi',
  `quantity` int NOT NULL,
  `subtotal` decimal(15,2) NOT NULL COMMENT 'price * quantity',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_transaction_details_to_transaction` (`transaction_id`),
  KEY `fk_transaction_details_to_product` (`product_id`),
  KEY `idx_transaction_details_transaction_id` (`transaction_id`),
  CONSTRAINT `fk_transaction_details_to_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_details_to_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `invoice` char(36) NOT NULL,
  `amount_paid` decimal(15,2) NOT NULL COMMENT 'Jumlah yang dibayar customer',
  `subtotal` decimal(15,2) NOT NULL COMMENT 'Subtotal sebelum pajak',
  `tax_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT 'Jumlah pajak',
  `total_amount` decimal(15,2) NOT NULL COMMENT 'Total setelah pajak',
  `change_amount` decimal(15,2) NOT NULL DEFAULT '0.00' COMMENT 'Kembalian',
  `customer_name` varchar(255) DEFAULT '-',
  `payment_method` enum('Cash','Gopay','Shopeepay','Kartu Debit','Kartu Kredit') NOT NULL DEFAULT 'Cash',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `invoice` (`invoice`),
  KEY `idx_transactions_invoice` (`invoice`),
  KEY `idx_transactions_payment_method` (`payment_method`),
  KEY `idx_transactions_created_at` (`created_at`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) NOT NULL,
  `role` enum('User','Admin') NOT NULL DEFAULT 'User',
  `password` varchar(255) NOT NULL,
  `email` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;