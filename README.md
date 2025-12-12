# Aplikasi Kedai Kopi Cak Budibud - POS & Manajemen Inventaris

![Java](https://img.shields.io/badge/Java-17-orange)
![Maven](https://img.shields.io/badge/Maven-3.8%2B-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-14%2B-blue)
![Size](https://img.shields.io/badge/Size-~4MB-green)
![License](https://img.shields.io/badge/License-MIT-green)

**Aplikasi manajemen kedai kopi modern "Kedai Kopi Cak Budibud"** - Sistem Point of Sale (POS) dan manajemen inventaris lengkap yang dibangun dengan Java Swing. Mendukung multi-user dengan real-time data synchronization untuk operasional kedai kopi yang efisien dan profesional.

---

## 📖 Tentang Aplikasi

### Latar Belakang

Kedai Kopi Cak Budibud memerlukan sistem terintegrasi untuk mengelola penjualan, inventaris, dan operasional harian dengan efisien. Aplikasi ini hadir sebagai solusi all-in-one yang membantu pemilik dan staff kedai kopi dalam:

- **Melayani Transaksi dengan Cepat** - Sistem kasir yang user-friendly dengan auto-refresh stock
- **Mengelola Stok Real-Time** - Tracking stok otomatis dengan alert untuk stok menipis
- **Menganalisis Penjualan** - Dashboard interaktif dengan visualisasi grafik
- **Mendukung Multi-User** - Beberapa kasir dapat bekerja bersamaan tanpa konflik data
- **Mengelola Tim dengan Aman** - Sistem role-based access control (RBAC)

---

## ✨ Fitur Unggulan

### 🔄 **REAL-TIME Multi-User Support** ⚡ NEW!

**Sinkronisasi Data Otomatis:**

- ✅ **Dashboard**: Auto-refresh setiap 5 menit untuk semua role
- ✅ **Kasir Panel**: Auto-refresh setiap 1 menit untuk update stok terbaru (CRITICAL!)
- ✅ **Inventaris Panel**: Auto-refresh setiap 2 menit untuk monitoring stok
- ✅ **Multi-Kasir Safe**: Mencegah overselling dengan data real-time
- ✅ **Single Database Truth**: Semua user melihat data yang sama

**Scenario:**

```
Kasir A menjual 10 Latte (10:00 AM)
  ↓
Database: Stock updated dari 50 → 40
  ↓
Kasir B auto-refresh (10:01 AM)
  ↓
Kasir B lihat stock terbaru: 40 ✅
  ↓
Tidak bisa oversell! ✅
```

### 🔐 Sistem Autentikasi & Keamanan

- **Login Aman** dengan BCrypt password hashing (cost factor 12)
- **Role-Based Access Control (RBAC)** - Owner, Kasir, Stocker
- **Activity Logging** untuk audit trail
- **Session Management** dengan auto-update last login
- **SQL Injection Protection** dengan PreparedStatement

### 📊 Dashboard Analitik (Owner)

**Visualisasi dengan JFreeChart:**

- **Statistik Real-time**: Penjualan hari ini, total transaksi, stok menipis
- **Line Chart**: Tren penjualan 7 hari terakhir
- **Pie Chart**: Distribusi penjualan per kategori (30 hari)
- **Bar Chart**: Top 10 menu terlaris dengan ranking
- **Alert Table**: Daftar menu dengan status stok (HABIS/KRITIS/RENDAH)
- **Auto-Refresh**: Data dashboard di-refresh otomatis setiap 5 menit

### 💰 Point of Sale (Kasir)

**Transaksi Cepat & Akurat:**

- **Pencarian Pintar**: Real-time search dan filter by kategori
- **Keranjang Interaktif**: Add, update quantity, remove items
- **Auto-Calculate**: Total, pajak (10%), grand total, kembalian
- **Stock Validation**: Cek ketersediaan stok sebelum checkout
- **Auto-Reduce Stock**: Stok berkurang otomatis setelah transaksi

**🧾 Receipt Printer (Thermal Style):**

- **Professional Format**: Seperti struk thermal printer 58mm
- **Complete Info**: Nama kedai, alamat, no. transaksi, kasir, tanggal/jam
- **Detail Items**: Qty, nama menu, harga satuan, subtotal
- **Print Preview**: Lihat struk sebelum print atau simpan
- **Modal Dialog**: HARUS close struk sebelum transaksi berikutnya

**🎉 Toast Notifications:**

- Success (green), Error (red), Warning (orange), Info (blue)
- Animasi slide-in dan auto-dismiss
- Feedback instant untuk semua aksi

**🔄 Auto-Refresh (1 menit):** Menu dan stok di-update otomatis untuk multi-kasir!

### 📦 Manajemen Inventaris (Stocker & Owner)

**CRUD Menu Lengkap:**

- Create, Read, Update, Delete menu
- Update stok (tambah/kurangi) dengan tracking
- Filter by kategori dan status
- Search by nama menu
- Status aktif/non-aktif toggle

**🔄 Auto-Refresh (2 menit):** Stok di-monitor otomatis untuk semua user!

### 🏷️ Manajemen Kategori (Owner)

- CRUD kategori produk
- Dependency checking (cegah delete kategori yang dipakai)
- Status aktif/non-aktif

### 👥 Manajemen User (Owner Only)

- Tambah, edit, delete user
- Role assignment (Owner/Kasir/Stocker)
- Reset password dengan BCrypt auto-hashing
- Toggle active/inactive status

### 🎨 Modern UI/UX

- **FlatLaf Light Theme** - Tampilan modern dan clean
- **Responsive MigLayout** - Adaptif ke berbagai ukuran layar
- **Color Scheme Consistent** - Primary blue dengan accent colors
- **Smooth Animations** - Toast notifications dan hover effects
- **Icon System** - SVG-based dengan IconManager utility

---

## 👥 Role & Akses

| Role    | Dashboard  | Kasir | Inventaris   | Kategori     | User Mgmt |
| ------- | ---------- | ----- | ------------ | ------------ | --------- |
| Owner   | ✅ Full    | ✅    | ✅ Full      | ✅ Full      | ✅ Full   |
| Kasir   | ✅ Limited | ✅    | ✅ Read-only | ❌           | ❌        |
| Stocker | ✅ Limited | ❌    | ✅ Full      | ✅ Read-only | ❌        |

**Default Login:**

- Owner: `admin` / `admin123`
- Kasir: `kasir1` / `admin123`
- Stocker: `stocker1` / `admin123`

---

## 🛠️ Teknologi & Library

**Core:**

- Java 17 (LTS)
- Maven 3.8+
- PostgreSQL 14+

**UI:**

- Java Swing
- FlatLaf 3.0+ (Modern Look & Feel)
- MigLayout 11.0 (Responsive Layout)

**Database:**

- HikariCP 5.0+ (Connection Pooling)
- PostgreSQL JDBC Driver

**Security:**

- BCrypt (at.favre.lib) - Password hashing with salt

**Charting:**

- JFreeChart 1.5.4 - Professional charts

**Utilities:**

- SLF4J 2.0+ & Logback 1.4+ (Logging)

---

## 📋 System Requirements

### Minimum Hardware

| Component | Requirement           |
| --------- | --------------------- |
| CPU       | Dual-core 2.0 GHz     |
| RAM       | 4 GB                  |
| Storage   | **150 MB** free space |
| Display   | 1366x768              |

### Recommended Hardware

| Component | Requirement           |
| --------- | --------------------- |
| CPU       | Quad-core 2.5 GHz+    |
| RAM       | 8 GB+                 |
| Storage   | **300 MB** free space |
| Display   | 1920x1080 (Full HD)   |

**Catatan:**

- **App Size**: ~4 MB (source code + dependencies)
- **Installed Size**: ~50-100 MB (termasuk Java runtime jika belum ada)
- **Database Size**: ~10-50 MB (tergantung jumlah transaksi)

### Software Requirements

✅ **Java Development Kit (JDK) 17+**

- Download: [Adoptium Temurin](https://adoptium.net/)
- Verifikasi: `java -version`

✅ **Apache Maven 3.8+**

- Download: [Maven Official](https://maven.apache.org/download.cgi)
- Verifikasi: `mvn -version`

✅ **PostgreSQL 14+**

- Download: [PostgreSQL Official](https://www.postgresql.org/download/)
- Verifikasi: `psql --version`

---

## 🚀 Instalasi & Setup

### Quick Start (5 Menit!)

```bash
# 1. Clone repository
git clone https://github.com/athfizh/KedaiKopiApp-CakBudibud.git
cd kedai-kopi-app

# 2. Setup database
psql -U postgres
CREATE DATABASE db_kedai_kopi;
\c db_kedai_kopi
\i database_schema.sql
\i database_sample_data.sql
\q

# 3. Configure database connection
# Edit: src/main/java/com/kedaikopi/config/DatabaseConfig.java
# Set: DB_PASSWORD = "your_postgres_password"

# 4. Build & Run
mvn clean install
mvn exec:java -Dexec.mainClass="com.kedaikopi.Main"

# 5. Login
# Username: admin
# Password: admin123
```

### Instalasi Detail untuk PC Klien

#### Step 1: Install Java Runtime

```bash
# Download JDK 17 dari https://adoptium.net/
# Install dengan wizard installer
# Verifikasi instalasi:
java -version
# Output harus: openjdk version "17.x.x"
```

#### Step 2: Setup Database (Server/PC Utama)

```bash
# Install PostgreSQL dari https://www.postgresql.org/download/
# Jalankan installer, set password untuk user 'postgres'

# Buat database:
psql -U postgres
CREATE DATABASE db_kedai_kopi;
\c db_kedai_kopi

# Import schema dan data:
\i path/to/database_schema.sql
\i path/to/database_sample_data.sql
\q
```

#### Step 3: Configure Database Connection

Edit file: `src/main/java/com/kedaikopi/config/DatabaseConfig.java`

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/db_kedai_kopi";
private static final String DB_USER = "postgres";
private static final String DB_PASSWORD = "GANTI_DENGAN_PASSWORD_ANDA";  // ⚠️ UBAH INI!
```

**Untuk Multi-PC Setup** (kasir di PC berbeda):

- Ganti `localhost` dengan IP address PC server
- Contoh: `jdbc:postgresql://192.168.1.100:5432/db_kedai_kopi`
- Pastikan PostgreSQL allow remote connections (edit `postgresql.conf` & `pg_hba.conf`)
- Buka firewall port 5432

#### Step 4: Build JAR File

```bash
# Di folder root project:
mvn clean package

# Output JAR ada di:
target/kedai-kopi-app-1.0-SNAPSHOT.jar
# Size: ~3-4 MB
```

#### Step 5: Run Aplikasi

**Option A: Run JAR (Recommended untuk Production)**

```bash
java -jar target/kedai-kopi-app-1.0-SNAPSHOT.jar
```

**Option B: Run dengan Maven (Development)**

```bash
mvn exec:java -Dexec.mainClass="com.kedaikopi.Main"
```

**Option C: Run dari IDE**

1. Import project ke IntelliJ IDEA / Eclipse / NetBeans
2. Wait for Maven dependencies download
3. Run `Main.java`

---

## 📱 Cara Penggunaan

Lihat [QUICKSTART.md](QUICKSTART.md) untuk panduan lengkap setup multi-user dan deployment.

### Quick Usage Guide

**Owner:**

1. Login → Dashboard (lihat analytics)
2. Managemen Inventaris, Kategori, User
3. Monitoring penjualan real-time

**Kasir:**

1. Login → Kasir Panel
2. Search menu, add to cart
3. Input payment, print receipt
4. Auto-refresh stock setiap 1 menit!

**Stocker:**

1. Login → Inventaris
2. Add menu, update stock
3. Monitor alert stok menipis
4. Auto-refresh setiap 2 menit!

---

## 🗂️ Struktur Project

```
kedai-kopi-app/ (~4 MB)
├── src/main/java/com/kedaikopi/
│   ├── Main.java                     # Entry point
│   ├── config/
│   │   └── DatabaseConfig.java       # HikariCP config
│   ├── model/
│   │   ├── User.java                 # User model + BCrypt
│   │   ├── Kategori.java
│   │   ├── MenuKopi.java
│   │   ├── TransaksiHeader.java
│   │   └── TransaksiDetail.java
│   ├── ui/
│   │   ├── LoginForm.java
│   │   ├── MainFrame.java
│   │   ├── components/
│   │   │   └── UIComponents.java
│   │   ├── dialogs/
│   │   │   ├── PaymentDialog.java
│   │   │   ├── MonthlyTransactionDialog.java
│   │   │   └── StockStatusDialog.java
│   │   └── panels/
│   │       ├── DashboardPanel.java   # ⏱️ Auto-refresh: 5 min
│   │       ├── KasirPanel.java       # ⏱️ Auto-refresh: 1 min ⚡
│   │       ├── InventarisPanel.java  # ⏱️ Auto-refresh: 2 min
│   │       ├── KategoriPanel.java
│   │       └── UserManagementPanel.java
│   └── util/
│       ├── ColorScheme.java
│       ├── ChartFactory.java
│       ├── ToastNotification.java
│       └── ReceiptPrinter.java       # 🧾 Thermal printer style
├── pom.xml                           # Maven dependencies
├── README.md                         # This file
└── QUICKSTART.md                     # Quick setup guide
```

---

## 🔄 Real-Time Data Strategy

**Auto-Refresh Schedule:**

| Panel      | Interval  | Priority    | Reason                           |
| ---------- | --------- | ----------- | -------------------------------- |
| Dashboard  | 5 min     | Medium      | Business analytics & statistics  |
| **Kasir**  | **1 min** | **HIGH ⚡** | **Stock sync untuk multi-kasir** |
| Inventaris | 2 min     | Medium      | Stock monitoring                 |

**Benefits:**

- ✅ Prevents overselling in multi-user scenarios
- ✅ All roles see consistent, up-to-date data
- ✅ No manual refresh needed
- ✅ Optimized intervals - tidak overload database

---

## 🐛 Troubleshooting

### ❌ Error: "Connection refused"

```
org.postgresql.util.PSQLException: Connection refused
```

**Solution:**

- Check PostgreSQL is running: `sudo systemctl status postgresql` (Linux) atau cek Services (Windows)
- Check database credentials di `DatabaseConfig.java`
- Check firewall allow port 5432

### ❌ Build Error: "Cannot read or execute"

**Solution:**

```bash
# Clear Maven cache
mvn clean

# Force update dependencies
mvn clean install -U
```

### ❌ Auto-Refresh Tidak Jalan

**Solution:**

- Check logs di `logs/application.log`
- Verify database connection active
- Restart aplikasi

---

## 📚 Documentation

- **README.md** (this file) - Overview dan setup lengkap
- **QUICKSTART.md** - Quick start guide untuk deployment
- **Javadoc** - Generate with: `mvn javadoc:javadoc`

---

## 👨‍💻 Developer

**Developed by**: **Athaulla Hafizh**  
**Institution**: Politeknik Negeri Malang (POLINEMA)  
**Program**: Teknik Informatika  
**Year**: 2025

---

## 📄 License

MIT License - Free to use and modify for educational and commercial purposes.

---

## 🙏 Acknowledgments

- **JFreeChart** untuk charting library
- **FlatLaf** untuk modern Look & Feel
- **PostgreSQL** untuk robust database
- **HikariCP** untuk high-performance connection pooling
- **BCrypt** untuk secure password hashing

---

**Selamat Menggunakan Kedai Kopi Cak Budibud! ☕**

> 💡 **Pro Tip**: Gunakan multi-user setup untuk pengalaman real-time yang optimal!

---

**© 2025 Athaulla Hafizh | POLINEMA**
