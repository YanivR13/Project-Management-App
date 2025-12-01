# How to Run the Project and Initialize the Database

## 1. Install MySQL
Download & install MySQL Server + MySQL Workbench.

## 2. Create the database schema
Inside the project you will find:   Server/data_base/prototypedb_orders_schema.sql

This file contains the DDL of the system.

### How to run it:
1. Open MySQL Workbench
2. Open `schema.sql`
3. Press the "Run all" lightning button.
4. The database and all its tables will be created automatically.

## 3. Update your DB connection details
Inside `Server/src/server/DBController.java` update:

```java
DriverManager.getConnection(
    "jdbc:mysql://localhost:3306/tirgol5db?serverTimezone=Asia/Jerusalem",
    "root",
    "YOUR_PASSWORD"
);
