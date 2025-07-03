# Sistem Point of Sale (POS) - Dokumentasi Lengkap

## 📋 Deskripsi Aplikasi

Sistem Point of Sale (POS) adalah aplikasi desktop berbasis Java Swing yang dirancang untuk mengelola transaksi penjualan, inventori produk, dan manajemen pengguna. Aplikasi ini menyediakan antarmuka yang user-friendly untuk kasir dan admin dalam menjalankan operasi toko sehari-hari.

## 🏗️ Arsitektur Sistem

### Tech Stack
- **Bahasa Pemrograman**: Java (Swing)
- **Database**: MySQL
- **IDE**: NetBeans
- **Build Tool**: Ant (build.xml)

### Struktur Project
```
POS/
├── src/
│   ├── Dashboard.java           # Dashboard utama POS
│   ├── LoginForm.java          # Form autentikasi
│   ├── FormProduct.java        # Manajemen produk
│   ├── FormUser.java           # Manajemen pengguna
│   ├── FormTransaksi.java      # Riwayat transaksi
│   ├── DashboardPegawai.java   # Dashboard khusus pegawai
│   ├── DatabaseConnection.java # Koneksi database
│   ├── HashUtil.java           # Utilitas enkripsi password
│   └── POS_Main/
│       └── POS_Main.java       # Entry point aplikasi
├── database_update.sql         # Schema database
├── build.xml                   # Build configuration
└── nbproject/                  # NetBeans project files
```

## 🗄️ Database Schema

### ERD (Entity Relationship Diagram)
**Instruksi untuk Screenshot**: *Tambahkan screenshot ERD database di sini*

### Tabel Database

#### 1. Tabel `users`
```sql
CREATE TABLE `users` (
  `id` int NOT NULL AUTO_INCREMENT,
  `full_name` varchar(255) NOT NULL,
  `role` enum('User','Admin') NOT NULL DEFAULT 'User',
  `password` varchar(255) NOT NULL,  -- SHA-256 hash
  `email` varchar(255) NOT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `email` (`email`)
);
```

**Deskripsi**: Menyimpan data pengguna sistem dengan role-based access control.

#### 2. Tabel `products`
```sql
CREATE TABLE `products` (
  `id` int NOT NULL AUTO_INCREMENT,
  `name` varchar(255) NOT NULL,
  `price` int NOT NULL,
  `category` varchar(255) NOT NULL,
  `unit` varchar(255) NOT NULL,
  `stock` int NOT NULL,
  `image_path` varchar(512) DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `deleted_at` timestamp NULL DEFAULT NULL,  -- Soft delete
  PRIMARY KEY (`id`)
);
```

**Deskripsi**: Menyimpan data produk dengan fitur soft delete dan support gambar produk.

#### 3. Tabel `transactions`
```sql
CREATE TABLE `transactions` (
  `id` int NOT NULL AUTO_INCREMENT,
  `invoice` char(36) NOT NULL,  -- UUID
  `amount_paid` decimal(15,2) NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,
  `tax_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `total_amount` decimal(15,2) NOT NULL,
  `change_amount` decimal(15,2) NOT NULL DEFAULT '0.00',
  `customer_name` varchar(255) DEFAULT '-',
  `payment_method` enum('Cash','Gopay','Shopeepay','Kartu Debit','Kartu Kredit') NOT NULL DEFAULT 'Cash',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `invoice` (`invoice`),
  KEY `idx_transactions_invoice` (`invoice`),
  KEY `idx_transactions_payment_method` (`payment_method`),
  KEY `idx_transactions_created_at` (`created_at`)
);
```

**Deskripsi**: Menyimpan data header transaksi dengan berbagai metode pembayaran.

#### 4. Tabel `transaction_details`
```sql
CREATE TABLE `transaction_details` (
  `id` int NOT NULL AUTO_INCREMENT,
  `transaction_id` int NOT NULL,
  `product_id` int NOT NULL,
  `product_name` varchar(255) NOT NULL,  -- Snapshot nama produk
  `price` decimal(15,2) NOT NULL,        -- Snapshot harga saat transaksi
  `quantity` int NOT NULL,
  `subtotal` decimal(15,2) NOT NULL,     -- price * quantity
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `fk_transaction_details_to_transaction` (`transaction_id`),
  KEY `fk_transaction_details_to_product` (`product_id`),
  CONSTRAINT `fk_transaction_details_to_product` FOREIGN KEY (`product_id`) REFERENCES `products` (`id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_transaction_details_to_transaction` FOREIGN KEY (`transaction_id`) REFERENCES `transactions` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
);
```

**Deskripsi**: Menyimpan detail item dalam setiap transaksi dengan snapshot data produk.

### Relasi Antar Tabel
- `transactions` 1:N `transaction_details`
- `products` 1:N `transaction_details`
- Menggunakan snapshot data pada `transaction_details` untuk menjaga konsistensi historical data

## 🔐 Sistem Autentikasi

### Role-Based Access Control
1. **Admin**: Full access ke semua fitur
2. **User**: Access ke dashboard POS dan transaksi
3. **Pegawai**: Limited access (dashboard khusus)

### Security Features
- Password di-hash menggunakan SHA-256
- Session management dengan role verification
- Input validation untuk email dan password

**Instruksi untuk Screenshot**: *Tambahkan screenshot halaman login di sini*

## 🎯 Flow Aplikasi

### 1. Authentication Flow
```
Start → Login Form → Validasi Kredensial → Role Check → Redirect ke Dashboard
```

### 2. Transaction Flow  
```
Dashboard → Pilih Produk → Add to Cart → Input Payment → Process Transaction → Print Receipt
```

### 3. Product Management Flow
```
Dashboard → Menu Products → CRUD Operations (Create/Read/Update/Delete)
```

### 4. User Management Flow
```
Dashboard → Menu Users → CRUD Operations (Admin Only)
```

## 💡 Fitur Utama

### 1. Dashboard POS (Main Feature)
**Instruksi untuk Screenshot**: *Tambahkan screenshot dashboard utama di sini*

**Fitur:**
- Product catalog dengan gambar
- Shopping cart functionality
- Multiple payment methods (Cash, Gopay, Shopeepay, Kartu Debit/Kredit)
- Automatic tax calculation (10%)
- Change calculation
- Numpad untuk input cash
- Print receipt functionality

**Komponen UI:**
- Panel produk (kiri): Grid produk dengan gambar dan informasi
- Panel transaksi (kanan): Cart, kalkulasi, dan payment
- Search bar untuk filter produk
- Menu bar untuk navigasi ke form lain

### 2. Product Management
**Instruksi untuk Screenshot**: *Tambahkan screenshot form product management di sini*

**Fitur:**
- CRUD operations untuk produk
- Upload dan preview gambar produk
- Kategori dan unit management
- Stock tracking
- Soft delete (data bisa di-restore)
- Search dan filter produk

**Validasi:**
- Harga harus berupa angka
- Stok harus berupa angka
- Nama produk wajib diisi
- Support format gambar (JPG, PNG, GIF)

### 3. User Management  
**Instruksi untuk Screenshot**: *Tambahkan screenshot form user management di sini*

**Fitur:**
- CRUD operations untuk user (Admin only)
- Role assignment (User/Admin)
- Email validation
- Password encryption
- Confirm password validation

**Validasi:**
- Email format validation
- Password minimal 6 karakter
- Unique email constraint
- Role-based access control

### 4. Transaction History
**Instruksi untuk Screenshot**: *Tambahkan screenshot riwayat transaksi di sini*

**Fitur:**
- View all transactions
- Transaction details popup
- Filter dan sorting
- Export capabilities
- Transaction summary

**Data yang ditampilkan:**
- Invoice number
- Customer name
- Payment method
- Amounts (subtotal, tax, total)
- Date and time

### 5. Receipt Printing
**Instruksi untuk Screenshot**: *Tambahkan screenshot preview receipt di sini*

**Fitur:**
- Generate printable receipt
- Include transaction details
- Company branding area
- Professional layout

## 🚀 Instalasi dan Setup

### Prerequisites
- Java JDK 8 atau lebih tinggi
- MySQL 5.7 atau lebih tinggi
- NetBeans IDE (opsional, untuk development)

### Langkah Instalasi

#### 1. Setup Database
```sql
-- Buat database
CREATE DATABASE s4_p1_pos;

-- Import schema
USE s4_p1_pos;
-- Run script dari database_update.sql
```

#### 2. Konfigurasi Database Connection
Edit file `src/DatabaseConnection.java`:
```java
conn = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/s4_p1_pos", 
    "your_username",    // Ganti dengan username MySQL Anda
    "your_password"     // Ganti dengan password MySQL Anda
);
```

#### 3. Setup Data Awal
```sql
-- Insert admin user default
INSERT INTO users (full_name, role, password, email) VALUES 
('Administrator', 'Admin', 'hashed_password_here', 'admin@pos.com');

-- Insert sample products
INSERT INTO products (name, price, category, unit, stock) VALUES 
('Contoh Produk', 15000, 'Makanan', 'pcs', 100);
```

#### 4. Build dan Run
```bash
# Menggunakan NetBeans
1. Open project di NetBeans
2. Clean and Build (F11)
3. Run (F6)

# Menggunakan command line
ant clean
ant compile
ant run
```

## 📱 User Interface Guide

### Login Screen
**Instruksi untuk Screenshot**: *Screenshot halaman login dengan field username/password*

**Cara penggunaan:**
1. Masukkan email dan password
2. Centang "Show Password" jika diperlukan
3. Klik tombol "Login"

### Main Dashboard
**Instruksi untuk Screenshot**: *Screenshot dashboard utama dengan layout 2 panel*

**Panel kiri - Product Catalog:**
- Tampilan grid produk 3 kolom
- Setiap produk menampilkan gambar, nama, dan harga
- Search bar di atas untuk filter produk
- Klik produk untuk menambah ke cart

**Panel kanan - Transaction:**
- Cart table dengan daftar item
- Summary (Subtotal, Tax, Total)
- Payment method selection
- Cash input dengan numpad
- Action buttons (Pay, Print, Reset, Remove)

### Product Management
**Instruksi untuk Screenshot**: *Screenshot form tambah/edit produk*

**Form fields:**
- Nama Produk
- Harga
- Kategori  
- Satuan
- Stok
- Upload Gambar
- Preview gambar

### Transaction History
**Instruksi untuk Screenshot**: *Screenshot tabel riwayat transaksi*

**Kolom tabel:**
- ID, Invoice, Customer, Payment Method
- Subtotal, Tax, Total, Amount Paid, Change
- Date/Time

## 🔧 Troubleshooting

### Common Issues

#### 1. Database Connection Error
**Error**: `Connection failed`
**Solution**: 
- Periksa MySQL service berjalan
- Verifikasi username/password di DatabaseConnection.java
- Pastikan database s4_p1_pos sudah dibuat

#### 2. Image Upload Error
**Error**: Upload gambar gagal
**Solution**: 
- Buat folder `uploads/products/` di root project
- Pastikan permission folder readable/writable

#### 3. Print Tidak Berfungsi
**Error**: Print receipt error
**Solution**: 
- Install printer driver
- Pastikan default printer tersedia
- Test print dari aplikasi lain

## 📊 Performance Monitoring

### Key Metrics
- Transaction processing time
- Database query performance
- Memory usage aplikasi
- User session management

### Optimization Tips
- Index database pada kolom yang sering di-query
- Implement connection pooling
- Optimize image file size
- Regular database maintenance

## 🔮 Future Enhancements

### Planned Features
1. **Reporting Module**
   - Sales report harian/bulanan
   - Product performance analysis
   - User activity logs

2. **Inventory Management**
   - Low stock alerts
   - Automatic reorder points
   - Supplier management

3. **Customer Management**
   - Customer database
   - Loyalty program
   - Customer purchase history

4. **Advanced POS Features**
   - Barcode scanning
   - Discount system
   - Multi-location support

5. **Integration**
   - Payment gateway integration
   - Accounting software sync
   - E-commerce platform sync

## 🏷️ Version History

### v1.0.0 (Current)
- Basic POS functionality
- Product management
- User management  
- Transaction processing
- Receipt printing

## 👥 Team & Credits

**Developer**: Muhammad Rafli Aryansyah
**Institution**: Universitas Pamulang - Semester 4  
**Course**: Pemrograman 1  
**Project Type**: Kelompok (Group Project)

## 📄 License

This project is for educational purposes as part of university coursework.

---

## 📞 Support

Untuk pertanyaan teknis atau bantuan, silakan hubungi tim development atau buat issue di repository ini.

**Note**: Dokumentasi ini mencakup semua aspek sistem POS dari database hingga user interface. Pastikan untuk mengikuti instruksi screenshot yang telah diberikan untuk melengkapi dokumentasi visual. 