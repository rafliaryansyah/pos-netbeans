# LAPORAN TUGAS KELOMPOK
## SISTEM POINT OF SALE (POS)

**Dosen: Niki Ratama, S.Kom., M.Kom**  
**Mata Kuliah: Pemrograman 1**  
**Semester 4 - UNPAM**

---

## 👥 ANGGOTA KELOMPOK

1. **MUHAMMAD RAFLI ARYANSYAH - 231011401531** 
2. **AHMAD FAIZ RAMDHANI - 231011401478**
3. **DAVA ARDIANSYAH - 231011401515** 
4. **FRANSISKUS RIANTO HARSEN - 231011401532**
5. **IZZEDIN SALMAN ALFARISI - 231011401487**

---

## 📋 DAFTAR ISI

1. [Pendahuluan](#pendahuluan)
2. [Rancangan Basis Data](#rancangan-basis-data)
3. [Struktur Aplikasi](#struktur-aplikasi)
4. [Desain Aplikasi](#desain-aplikasi)
5. [Struktur Projek](#struktur-projek)
6. [Tampilan Aplikasi](#tampilan-aplikasi)
7. [Instalasi dan Setup](#instalasi-dan-setup)
8. [Kesimpulan](#kesimpulan)

---

## 🚀 PENDAHULUAN

### Deskripsi Projek
Sistem Point of Sale (POS) adalah aplikasi desktop berbasis Java Swing yang dirancang untuk membantu bisnis retail dalam mengelola transaksi penjualan, inventori produk, dan manajemen pengguna. Aplikasi ini mengintegrasikan database MySQL untuk penyimpanan data yang aman dan efisien.

### Tujuan Pengembangan
- Mempermudah proses transaksi penjualan
- Mengelola inventori produk secara real-time
- Menyediakan sistem autentikasi multi-role
- Menghasilkan laporan transaksi yang akurat
- Menyediakan interface yang user-friendly

### Fitur Utama
- ✅ **Sistem Login Multi-Role** (Admin, User, Pegawai)
- ✅ **Management Produk** (CRUD dengan upload gambar)
- ✅ **Sistem Transaksi POS** dengan multiple payment methods
- ✅ **Management User** dengan role-based access
- ✅ **Laporan Transaksi** dengan detail lengkap
- ✅ **Print Receipt** untuk bukti transaksi
- ✅ **Search & Filter** produk
- ✅ **Real-time Stock Update**

---

## 🗄️ RANCANGAN BASIS DATA

### ERD (Entity Relationship Diagram)
![Halmaan Login!](/assets/ERD.png "Halaman Login")

### Struktur Database

**Nama Database:** `s4_p1_pos`

#### 1. Tabel `users`
Menyimpan informasi pengguna sistem dengan role-based access control.

| Field | Type | Constraint | Description |
|-------|------|------------|-------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | ID unik pengguna |
| full_name | VARCHAR(255) | NOT NULL | Nama lengkap pengguna |
| role | ENUM('User','Admin') | NOT NULL, DEFAULT 'User' | Peran pengguna |
| password | VARCHAR(255) | NOT NULL | Password ter-hash |
| email | VARCHAR(255) | NOT NULL, UNIQUE | Email login |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Waktu pembuatan |
| deleted_at | TIMESTAMP | NULL | Soft delete timestamp |

#### 2. Tabel `products`
Menyimpan informasi produk yang dijual dalam sistem POS.

| Field | Type | Constraint | Description |
|-------|------|------------|-------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | ID unik produk |
| name | VARCHAR(255) | NOT NULL | Nama produk |
| price | INT | NOT NULL | Harga produk |
| category | VARCHAR(255) | NOT NULL | Kategori produk |
| unit | VARCHAR(255) | NOT NULL | Satuan produk |
| stock | INT | NOT NULL | Stok tersedia |
| image_path | VARCHAR(512) | NULL | Path gambar produk |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Waktu pembuatan |
| deleted_at | TIMESTAMP | NULL | Soft delete timestamp |

#### 3. Tabel `transactions`
Menyimpan header informasi transaksi penjualan.

| Field | Type | Constraint | Description |
|-------|------|------------|-------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | ID unik transaksi |
| invoice | CHAR(36) | NOT NULL, UNIQUE | Nomor invoice (UUID) |
| amount_paid | DECIMAL(15,2) | NOT NULL | Jumlah dibayar customer |
| subtotal | DECIMAL(15,2) | NOT NULL | Subtotal sebelum pajak |
| tax_amount | DECIMAL(15,2) | NOT NULL, DEFAULT 0.00 | Jumlah pajak |
| total_amount | DECIMAL(15,2) | NOT NULL | Total setelah pajak |
| change_amount | DECIMAL(15,2) | NOT NULL, DEFAULT 0.00 | Kembalian |
| customer_name | VARCHAR(255) | DEFAULT '-' | Nama customer |
| payment_method | ENUM('Cash','Gopay','Shopeepay','Kartu Debit','Kartu Kredit') | NOT NULL, DEFAULT 'Cash' | Metode pembayaran |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Waktu transaksi |
| updated_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | Waktu update |

#### 4. Tabel `transaction_details`
Menyimpan detail item per transaksi (relasi one-to-many dengan transactions).

| Field | Type | Constraint | Description |
|-------|------|------------|-------------|
| id | INT | PRIMARY KEY, AUTO_INCREMENT | ID unik detail |
| transaction_id | INT | NOT NULL, FOREIGN KEY | Reference ke transactions.id |
| product_id | INT | NOT NULL, FOREIGN KEY | Reference ke products.id |
| product_name | VARCHAR(255) | NOT NULL | Nama produk saat transaksi |
| price | DECIMAL(15,2) | NOT NULL | Harga saat transaksi |
| quantity | INT | NOT NULL | Jumlah item |
| subtotal | DECIMAL(15,2) | NOT NULL | price × quantity |
| created_at | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | Waktu pembuatan |

### Relasi Antar Tabel
- `transactions` ← **1:N** → `transaction_details`
- `products` ← **1:N** → `transaction_details`
- Implementasi Foreign Key dengan CASCADE dan RESTRICT constraints

---

## 🏗️ STRUKTUR APLIKASI

### Arsitektur Aplikasi
Aplikasi POS menggunakan arsitektur **3-Layer Architecture**:

```
┌─────────────────────────────────────┐
│         PRESENTATION LAYER          │
│    (Java Swing GUI Components)     │
├─────────────────────────────────────┤
│          BUSINESS LAYER             │
│     (Application Logic & Rules)     │
├─────────────────────────────────────┤
│           DATA LAYER                │
│     (MySQL Database & JDBC)         │
└─────────────────────────────────────┘
```

### Flow Aplikasi

#### 1. Authentication Flow
```
LoginForm.java → Validasi User → Role Check → Redirect Dashboard
                     ↓
    ┌─────────────────┼─────────────────┐
    ▼                 ▼                 ▼
Admin/User       Pegawai           Error
Dashboard.java   DashboardPegawai.java   ↩ LoginForm
```

#### 2. Main Application Flow
```
Dashboard.java (Main POS Interface)
    ├── Menu Bar Navigation
    │   ├── FormProduct.java (Product Management)
    │   ├── FormUser.java (User Management)
    │   └── FormTransaksi.java (Transaction History)
    │
    ├── POS Transaction System
    │   ├── Product Selection
    │   ├── Cart Management
    │   ├── Payment Processing
    │   └── Receipt Printing
    │
    └── Real-time Updates
        ├── Stock Management
        ├── Transaction Recording
        └── Database Synchronization
```

### Komponen Utama

#### 1. **LoginForm.java**
- **Fungsi**: Entry point aplikasi dengan sistem autentikasi
- **Features**: 
  - Email/password validation
  - Password hashing (SHA-256)
  - Role-based redirect
  - Show/hide password toggle

#### 2. **Dashboard.java**  
- **Fungsi**: Interface utama POS untuk kasir
- **Features**:
  - Product grid display dengan search
  - Shopping cart management
  - Multiple payment methods
  - Real-time calculation (subtotal, tax, total)
  - Receipt printing
  - Menu navigation

#### 3. **FormProduct.java**
- **Fungsi**: CRUD management produk
- **Features**:
  - Add/Edit/Delete produk
  - Image upload support
  - Category management
  - Stock tracking
  - Data validation

#### 4. **FormUser.java**
- **Fungsi**: User management dengan role assignment
- **Features**:
  - User CRUD operations
  - Role assignment (Admin/User)
  - Password management
  - Email validation

#### 5. **FormTransaksi.java**
- **Fungsi**: View history dan detail transaksi
- **Features**:
  - Transaction listing
  - Detail view dengan breakdown
  - Date filtering
  - Export capabilities

---

## 🎨 DESAIN APLIKASI

### Design Principles
- **User-Centered Design**: Interface intuitif dan mudah digunakan
- **Consistency**: Konsistensi warna, font, dan layout
- **Accessibility**: Support keyboard navigation dan readable fonts
- **Responsive**: Adaptable dengan berbagai resolusi screen

### Color Scheme
- **Primary Blue**: `#3B59B6` - Header dan accent colors
- **Success Green**: `#228B22` - Positive actions (Save, Pay)
- **Warning Orange**: `#FF8C00` - Warning actions
- **Danger Red**: `#DC3545` - Delete dan error states
- **Neutral Gray**: `#F8F9FA` - Background dan borders

### Typography
- **Primary Font**: Arial, Sans-serif
- **Header**: Bold 24px
- **Subheader**: Bold 16px  
- **Body Text**: Regular 12px
- **Button Text**: Bold 12px

### Layout Design

#### Login Form
- **Clean & Minimal**: Focus pada form login
- **Centered Layout**: Login box di tengah screen
- **Brand Identity**: Logo dan nama aplikasi prominent

#### Dashboard POS
- **3-Panel Layout**:
  - **Left Panel**: Product grid dengan search
  - **Center Panel**: Selected products dan details  
  - **Right Panel**: Cart, calculation, dan payment

#### Management Forms
- **Standard CRUD Layout**:
  - **Top**: Title dan action buttons
  - **Center**: Data table atau form fields
  - **Bottom**: Navigation dan status

---

## 📁 STRUKTUR PROJEK

### Directory Structure
```
POS/
├── 📁 src/                          # Source code utama
│   ├── 📄 LoginForm.java            # Form login aplikasi
│   ├── 📄 LoginForm.form            # Design file login form
│   ├── 📄 Dashboard.java            # Main POS interface
│   ├── 📄 Dashboard.form            # Design file dashboard
│   ├── 📄 DashboardPegawai.java     # Dashboard untuk role Pegawai
│   ├── 📄 DashboardPegawai.form     # Design file dashboard pegawai
│   ├── 📄 FormProduct.java          # Product management form
│   ├── 📄 FormProduct.form          # Design file product form
│   ├── 📄 FormUser.java             # User management form
│   ├── 📄 FormUser.form             # Design file user form
│   ├── 📄 FormTransaksi.java        # Transaction history form
│   ├── 📄 DatabaseConnection.java   # Database connectivity
│   ├── 📄 HashUtil.java             # Password hashing utility
│   └── 📁 POS_Main/                 # Main application package
│       └── 📄 POS_Main.java         # Application entry point
├── 📁 nbproject/                    # NetBeans project files
│   ├── 📄 build-impl.xml            # Build implementation
│   ├── 📄 project.properties        # Project configuration
│   ├── 📄 project.xml               # Project metadata
│   └── 📁 private/                  # Private project settings
├── 📁 test/                         # Test files directory
├── 📄 database_update.sql           # Database schema dan updates
├── 📄 build.xml                     # Ant build configuration
├── 📄 manifest.mf                   # JAR manifest file
├── 📄 applet.policy                 # Security policy file
└── 📄 README.md                     # Project documentation
```

### Penjelasan File Utama

#### **Core Application Files**
- **LoginForm.java**: Entry point dengan autentikasi multi-role
- **Dashboard.java**: Interface POS utama (1000+ lines code)
- **FormProduct.java**: Management produk dengan image upload
- **FormUser.java**: User management dengan role assignment
- **FormTransaksi.java**: History dan detail transaksi

#### **Utility Classes**
- **DatabaseConnection.java**: Singleton connection ke MySQL
- **HashUtil.java**: SHA-256 password hashing

#### **Configuration Files**
- **database_update.sql**: Schema database dan sample data
- **build.xml**: Apache Ant build configuration
- **manifest.mf**: JAR packaging configuration

### Dependencies & Libraries
```xml
<!-- External Libraries yang digunakan -->
- MySQL Connector/J (JDBC Driver)
- Java Swing (GUI Framework)
- Java AWT (Graphics dan Events)
- Java SQL (Database Operations)
- Java Security (Password Hashing)
- Java I/O (File Operations)
```

---

## 📱 TAMPILAN APLIKASI

### 1. Login Form

![Halmaan Login!](/assets/Login.png "Halaman Login")

**Fitur Login Form:**
- Input email dan password
- Show/hide password toggle
- Validasi input (minimum 6 karakter password)
- Role-based redirect setelah login sukses
- Error handling untuk kredensial salah

### 2. Dashboard POS (Main Interface)
![Halman Dashboard!](/assets/Dashboard.png "Halman Dashboard")

**Fitur Dashboard:**
- **Product Grid**: Menampilkan produk dalam card layout
- **Search Bar**: Real-time product filtering
- **Shopping Cart**: Add/remove items dengan quantity control
- **Payment Panel**: Multiple payment methods (Cash, E-wallet, Cards)
- **Calculator**: Real-time subtotal, tax, total calculation
- **Menu Bar**: Navigate ke management forms

### 3. Product Management Form
![Product!](/assets/Product.png "Product")

**Fitur Product Management:**
- Data table dengan product listing
- CRUD operations (Create, Read, Update, Delete)
- Image upload untuk produk
- Category dan unit management
- Stock tracking dan validation
- Search dan filter functionality

### 4. User Management Form  
![Product!](/assets/User.png "Product")

**Fitur User Management:**
- User listing dengan role display
- Add/Edit/Delete users
- Role assignment (Admin/User)
- Password management dengan hashing
- Email validation

### 5. Transaction History Form
![Product!](/assets/Transactions.png "Product")

**Fitur Transaction History:**
- Transaction listing dengan pagination
- Detail view dengan item breakdown
- Date range filtering
- Payment method grouping
- Export to various formats

---

## ⚙️ INSTALASI DAN SETUP

### Prerequisites
- **Java Development Kit (JDK)** versi 8 atau lebih tinggi
- **MySQL Server** versi 5.7 atau lebih tinggi
- **NetBeans IDE** (recommended) atau IDE Java lainnya
- **MySQL Connector/J** library

### Database Setup

#### 1. Buat Database
```sql
CREATE DATABASE s4_p1_pos;
USE s4_p1_pos;
```

#### 2. Import Schema
```bash
mysql -u root -p s4_p1_pos < database_update.sql
```

#### 3. Konfigurasi Koneksi
Edit file `src/DatabaseConnection.java`:
```java
conn = DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/s4_p1_pos", 
    "your_username",    // Ganti dengan username MySQL Anda
    "your_password"     // Ganti dengan password MySQL Anda
);
```

### Application Setup

#### 1. Clone/Download Project
```bash
git clone [URL_REPOSITORY_GITHUB]
cd POS
```

#### 2. Open di NetBeans
- File → Open Project
- Pilih folder POS
- Build dan Run project

#### 3. Default Login Credentials
```
Admin Account:
Email: admin@pos.com
Password: admin123

User Account:  
Email: user@pos.com
Password: user123
```

### Build Instructions

#### Compile via Command Line
```bash
javac -cp ".:mysql-connector-java.jar" src/*.java
```

#### Run Application
```bash
java -cp ".:mysql-connector-java.jar:src" LoginForm
```

#### Create JAR File
```bash
ant jar
```

### Troubleshooting

#### Common Issues:
1. **Database Connection Failed**
   - Pastikan MySQL service running
   - Check username/password di DatabaseConnection.java
   - Verify database s4_p1_pos sudah ada

2. **ClassNotFoundException: MySQL Driver**
   - Download MySQL Connector/J
   - Add ke project libraries

3. **Login Failed**
   - Ensure database has user records
   - Check password hashing implementation

---

## 📊 KESIMPULAN

### Hasil Pengembangan
Sistem Point of Sale (POS) telah berhasil dikembangkan dengan fitur lengkap yang mencakup:

#### ✅ **Achievements Reached:**
1. **Sistem Autentikasi Multi-Role** - Login aman dengan pembagian akses
2. **Interface POS yang Intuitif** - Easy-to-use cashier interface
3. **Management Module Lengkap** - Product, User, Transaction management
4. **Database Design yang Normalized** - Efficient data storage
5. **Real-time Stock Updates** - Inventory tracking yang akurat
6. **Multiple Payment Methods** - Support berbagai metode pembayaran
7. **Receipt Printing System** - Professional receipt generation

#### 🎯 **Technical Accomplishments:**
- **Clean Architecture**: 3-layer separation (Presentation, Business, Data)
- **Security Implementation**: Password hashing dengan SHA-256
- **Database Integration**: Efficient MySQL operations dengan JDBC
- **Exception Handling**: Comprehensive error handling
- **User Experience**: Responsive dan user-friendly interface

#### 📈 **Business Value:**
- Mempercepat proses transaksi penjualan
- Meningkatkan akurasi inventory management
- Menyediakan audit trail untuk semua transaksi
- Mengurangi human error dalam calculation
- Mendukung business intelligence melalui reporting

### Pembelajaran Tim
Melalui projek ini, tim telah mempelajari:
- **Java Swing Development** untuk desktop applications
- **Database Design & Integration** dengan MySQL
- **Software Architecture** dan best practices
- **Team Collaboration** dalam software development
- **Version Control** menggunakan Git
- **Project Management** dan timeline execution

### Refleksi Kelompok atau Personal
Projek POS ini menjadi pembelajaran berharga pribadi:
- **Technical Skills**: Java programming, database design, UI/UX
- **Soft Skills**: <del>Teamwork, communication, problem-solving
- **Project Management**: Planning, execution, testing, documentation

---

## 📞 INFORMASI KONTAK

**Tim Pengembang:**
- 👨‍💻 **MUHAMMAD RAFLI ARYANSYAH - 231011401531** 
- 👨‍💻 **AHMAD FAIZ RAMDHANI - 231011401478**
- 👨‍💻 **DAVA ARDIANSYAH - 231011401515** 
- 👨‍💻 **FRANSISKUS RIANTO HARSEN - 231011401532**
- 👨‍💻 **IZZEDIN SALMAN ALFARISI - 231011401487**

**Repository:** [Github Repository](https://github.com/rafliaryansyah/pos-netbeans).
**Demo Video:** [Google Drive](https://drive.google.com/file/d/1B3CLfE0bwnr51c90usmUqCvutD_C60hW/view?usp=drive_link)

---

## 📄 LISENSI

Projek ini dikembangkan untuk keperluan akademis sebagai Tugas Kelompok Mata Kuliah Pemrograman 1, Semester 4, Universitas Pamulang (UNPAM).

---

*© 2025 Tim POS - Universitas Pamulang. All Rights Reserved.* 