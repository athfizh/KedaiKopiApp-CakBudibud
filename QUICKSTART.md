# 🚀 QUICK START GUIDE - Kedai Kopi Cak Budibud

**Panduan cepat untuk setup dan menjalankan aplikasi dalam 10 menit!**

---

## 📋 Prerequisites

Pastikan sudah terinstall:

- ✅ **Java 17+** - [Download](https://adoptium.net/)
- ✅ **PostgreSQL 14+** - [Download](https://www.postgresql.org/download/)
- ✅ **Maven 3.8+** (opsional untuk build) - [Download](https://maven.apache.org/)

---

## ⚡ Quick Setup (5 Menit!)

### Step 1: Setup Database

```bash
# Login ke PostgreSQL
psql -U postgres

# Buat database
CREATE DATABASE db_kedai_kopi;

# Connect ke database
\c db_kedai_kopi

# Import schema (dari folder project)
\i database_schema.sql

# Import sample data (opsional)
\i database_sample_data.sql

# Exit
\q
```

### Step 2: Configure Database Connection

Edit file: `src/main/java/com/kedaikopi/config/DatabaseConfig.java`

```java
private static final String DB_URL = "jdbc:postgresql://localhost:5432/db_kedai_kopi";
private static final String DB_USER = "postgres";
private static final String DB_PASSWORD = "YOUR_PASSWORD"; // ⚠️ UBAH INI!
```

### Step 3: Run Aplikasi

**Option A: Dengan Maven (Recommended)**

```bash
cd kedai-kopi-app
mvn clean compile
mvn exec:java -Dexec.mainClass="com.kedaikopi.Main"
```

**Option B: Dari IDE**

1. Import project ke IntelliJ IDEA / Eclipse / NetBeans
2. Wait Maven dependencies download
3. Run `Main.java`

---

## 🔐 Login Credentials

| Username   | Password   | Role    | Access                                                          |
| ---------- | ---------- | ------- | --------------------------------------------------------------- |
| `admin`    | `admin123` | Owner   | Full access - Dashboard, Kasir, Inventaris, Kategori, User Mgmt |
| `kasir1`   | `admin123` | Kasir   | Dashboard (limited), Kasir panel                                |
| `stocker1` | `admin123` | Stocker | Dashboard (limited), Inventaris (full)                          |

---

## 🎯 Usage Guide

### For Owner (admin)

1. **View Dashboard**

   - Login → Otomatis ke Dashboard
   - Lihat chart penjualan, top items, stock alerts
   - Auto-refresh setiap 5 menit

2. **Manage Inventory**

   - Sidebar → Inventaris
   - CRUD menu items
   - Adjust stock levels
   - Auto-refresh setiap 2 menit

3. **Manage Categories**

   - Sidebar → Kategori
   - Add/edit/delete categories

4. **Manage Users**
   - Sidebar → User Management
   - Add kasir/stocker accounts
   - Reset passwords
   - Toggle active status

### For Kasir (kasir1)

1. **Process Transactions**

   - Login → Kasir Panel
   - Search/filter menu items
   - Add items to cart
   - Process payment
   - Print receipt (modal blocking)
   - Auto-refresh menu setiap 1 menit ⚡

2. **View Dashboard**
   - Sidebar → Dashboard
   - View sales summary (limited)

### For Stocker (stocker1)

1. **Manage Stock**

   - Login → Inventaris
   - View all menu items
   - Adjust stock (tambah/kurangi)
   - Monitor stock alerts
   - Auto-refresh setiap 2 menit

2. **View Dashboard**
   - Sidebar → Dashboard
   - View stock status (limited)

---

## 🔄 Multi-User Setup (Network)

### Server PC (Database):

1. **Configure PostgreSQL untuk allow remote connections**

Edit `postgresql.conf`:

```
listen_addresses = '*'
```

Edit `pg_hba.conf`:

```
# IPv4 local connections:
host    all    all    0.0.0.0/0    md5
```

2. **Restart PostgreSQL**

```bash
# Windows
services.msc → PostgreSQL → Restart

# Linux
sudo systemctl restart postgresql
```

3. **Firewall: Allow port 5432**

```bash
# Windows Firewall
netsh advfirewall firewall add rule name="PostgreSQL" dir=in action=allow protocol=TCP localport=5432

# Linux (UFW)
sudo ufw allow 5432/tcp
```

### Client PC (Kasir/Stocker):

1. **Edit DatabaseConfig.java**

```java
// Ganti localhost dengan IP server
private static final String DB_URL = "jdbc:postgresql://192.168.1.100:5432/db_kedai_kopi";
```

2. **Build & Deploy**

```bash
mvn clean package
# Copy target/KedaiKopi.jar ke client PC
```

3. **Run di Client**

```bash
java -jar KedaiKopi.jar
```

---

## 📊 Real-Time Features

**Auto-Refresh Schedule:**

| Panel      | Interval        | Purpose                      |
| ---------- | --------------- | ---------------------------- |
| Dashboard  | 5 minutes       | Business analytics           |
| **Kasir**  | **1 minute** ⚡ | **Stock sync (multi-kasir)** |
| Inventaris | 2 minutes       | Stock monitoring             |

**Benefits:**

- ✅ Multi-kasir can work simultaneously
- ✅ Real-time stock updates prevent overselling
- ✅ No manual refresh needed
- ✅ Consistent data across all users

---

## 🧾 Receipt Printer Features

**Setelah payment:**

1. Receipt pop-up muncul (MODAL - must close!)
2. Preview struk thermal 58mm format
3. Options:
   - **Print** - Print ke printer
   - **Save as TXT** - Export untuk email/backup
   - **Close** - Continue to next transaction

**Struk includes:**

- Store info & contact
- Transaction details
- Item list with qty & prices
- Subtotal, tax (10%), grand total
- Payment & change
- Timestamp & transaction ID

---

## 🛠️ Troubleshooting

### ❌ "Connection refused"

**Solusi:**

- Check PostgreSQL running: `sudo systemctl status postgresql`
- Check database credentials
- Check firewall allow port 5432

### ❌ "Out of memory"

**Solusi:**

```bash
# Increase Java heap
java -Xmx512m -jar KedaiKopi.jar
```

### ❌ Auto-refresh tidak jalan

**Solusi:**

- Check logs: `logs/application.log`
- Verify database connection active
- Restart aplikasi

### ❌ Receipt tidak muncul

**Solusi:**

- Pastikan transaksi sukses tersimpan
- Check logs untuk error
- Verify Menu object di TransaksiDetail

---

## 📱 Application Info

**Current Version**: 1.0.0  
**Size**: ~4 MB (source code)  
**Installed Size**: 50-100 MB (with Java runtime)  
**Database Size**: 10-50 MB (depends on transactions)

**Minimum Requirements:**

- CPU: Dual-core 2.0 GHz
- RAM: 4 GB
- Storage: 150 MB
- Display: 1366x768

**Recommended:**

- CPU: Quad-core 2.5 GHz+
- RAM: 8 GB+
- Storage: 300 MB
- Display: 1920x1080 (Full HD)

---

## 📚 Additional Resources

- **Full Documentation**: See [README.md](README.md)
- **Database Schema**: `database_schema.sql`
- **Sample Data**: `database_sample_data.sql`
- **Logs**: `logs/application.log`

---

## 👨‍💻 Developer

**Developed by**: Athaulla Hafizh  
**Institution**: Politeknik Negeri Malang (POLINEMA)  
**Year**: 2025

---

## 📄 License

MIT License - Free to use and modify for educational and commercial purposes.

---

**Selamat Menggunakan! ☕**

> 💡 **Pro Tip**: Untuk multi-kasir, setup 1 PC sebagai database server dan client PC lainnya connect ke server tersebut!
