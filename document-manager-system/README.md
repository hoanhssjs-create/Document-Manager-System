# Document Manager System

Ứng dụng desktop quản lý tài liệu cá nhân/sinh viên, được xây dựng bằng Java 17, JavaFX, Maven, JDBC và SQL Server.

Ứng dụng cho phép người dùng đăng ký, đăng nhập, tải lên tài liệu, quản lý thư mục, gắn thẻ, tìm kiếm, xem thống kê và theo dõi lịch sử thao tác.

## Công nghệ sử dụng

- Java 17
- JavaFX 21
- Maven
- JDBC
- Microsoft SQL Server
- Mô hình tổ chức mã nguồn theo hướng MVC/Service/DAO

## Chức năng chính

- Đăng ký và đăng nhập tài khoản
- Quản lý thông tin hồ sơ cá nhân
- Đổi mật khẩu
- Tải lên tài liệu định dạng `PDF`, `DOCX`, `TXT`
- Mở tài liệu trực tiếp từ ứng dụng
- Quản lý thư mục
- Quản lý thẻ tài liệu
- Tìm kiếm tài liệu theo tên, thư mục, thẻ
- Xem dashboard thống kê số lượng tài liệu, thư mục, thẻ và dung lượng lưu trữ
- Ghi nhận lịch sử hoạt động như đăng nhập, đăng xuất, tải lên, xem, sửa, xóa

## Cấu trúc thư mục

```text
document-manager-system/
├── documents/                         # Nơi lưu file người dùng tải lên
├── src/main/java/
│   ├── com/documentmanager/
│   │   ├── Main.java                   # Điểm khởi chạy ứng dụng
│   │   ├── config/                     # Cấu hình database
│   │   ├── controller/                 # Điều khiển giao diện
│   │   ├── dao/                        # Truy vấn dữ liệu
│   │   ├── database/                   # Script tạo database
│   │   ├── model/                      # Lớp dữ liệu
│   │   ├── service/                    # Xử lý nghiệp vụ
│   │   └── util/                       # Tiện ích dùng chung
│   └── module-info.java
├── src/main/resources/css/app.css      # Giao diện JavaFX
├── pom.xml                             # Cấu hình Maven
└── README.md
```

## Yêu cầu trước khi chạy

Cài đặt các phần mềm sau:

- JDK 17 hoặc mới hơn
- Maven 3.8 hoặc mới hơn
- Microsoft SQL Server
- SQL Server Management Studio hoặc Azure Data Studio
- Một IDE Java nếu muốn mở bằng giao diện, ví dụ IntelliJ IDEA, Eclipse hoặc NetBeans

Kiểm tra Java và Maven bằng PowerShell:

```powershell
java -version
mvn -version
```

Nếu hai lệnh trên chưa chạy được, hãy kiểm tra lại biến môi trường `JAVA_HOME` và `Path`.

## Cài đặt database

### Bước 1: Mở SQL Server

Đảm bảo SQL Server đang chạy và cho phép kết nối qua cổng `1433`.

Nếu dùng SQL Server Configuration Manager, hãy kiểm tra:

- SQL Server service đang chạy
- TCP/IP đã được bật
- Cổng TCP là `1433`
- SQL Server Authentication đã được bật nếu bạn dùng tài khoản `sa`

### Bước 2: Tạo database

Mở SQL Server Management Studio hoặc Azure Data Studio, sau đó chạy toàn bộ script:

```text
src/main/java/com/documentmanager/database/schema.sql
```

Script này sẽ tạo database:

```text
DocumentManagerDB
```

và các bảng:

- `Users`
- `Folders`
- `Documents`
- `Tags`
- `DocumentTags`
- `ActivityLogs`

## Cấu hình kết nối database

Ứng dụng đọc cấu hình database từ biến môi trường. Nếu không đặt biến môi trường, ứng dụng dùng cấu hình mặc định:

```text
URL:      jdbc:sqlserver://localhost:1433;databaseName=DocumentManagerDB;encrypt=true;trustServerCertificate=true
User:     sa
Password: 7845fa19
```

Nếu SQL Server của bạn dùng tài khoản hoặc mật khẩu khác, đặt biến môi trường trong PowerShell trước khi chạy:

```powershell
$env:DMS_DB_URL="jdbc:sqlserver://localhost:1433;databaseName=DocumentManagerDB;encrypt=true;trustServerCertificate=true"
$env:DMS_DB_USER="sa"
$env:DMS_DB_PASSWORD="mat_khau_sql_server_cua_ban"
```

Các biến môi trường được hỗ trợ:

- `DMS_DB_URL`
- `DMS_DB_USER`
- `DMS_DB_PASSWORD`

## Chạy ứng dụng bằng terminal

Mở PowerShell tại thư mục dự án:

```powershell
cd D:\DACS1\document-manager-system
```

Tải thư viện, biên dịch và kiểm tra nhanh:

```powershell
mvn -DskipTests package
```

Chạy ứng dụng:

```powershell
mvn javafx:run
```

Nếu chạy thành công, cửa sổ `Document Manager System` sẽ mở ra.

## Mở và chạy bằng IntelliJ IDEA

1. Mở IntelliJ IDEA.
2. Chọn `Open`.
3. Chọn thư mục:

```text
D:\DACS1\document-manager-system
```

4. Chờ IntelliJ import Maven project.
5. Đảm bảo Project SDK là JDK 17 hoặc mới hơn.
6. Mở cửa sổ Maven ở bên phải.
7. Chạy:

```text
Plugins > javafx > javafx:run
```

Hoặc mở file `src/main/java/com/documentmanager/Main.java` và chạy class `Main`.

Nếu chạy từ IntelliJ nhưng không kết nối được database, hãy cấu hình biến môi trường trong Run Configuration:

- `DMS_DB_URL`
- `DMS_DB_USER`
- `DMS_DB_PASSWORD`

## Cách sử dụng sau khi mở ứng dụng

1. Ở màn hình đăng nhập, chọn `Create new account`.
2. Nhập họ tên, email, username và password.
3. Đăng nhập bằng tài khoản vừa tạo.
4. Vào `Upload File` để tải lên tài liệu.
5. Vào `Folders` để tạo và quản lý thư mục.
6. Vào `Tags` để tạo và quản lý thẻ.
7. Vào `Documents` để xem, tìm kiếm, mở, chỉnh sửa hoặc xóa tài liệu.
8. Vào `Dashboard` để xem thống kê tổng quan.
9. Vào `Activity Logs` để xem lịch sử thao tác.
10. Vào `Profile` để cập nhật thông tin cá nhân hoặc đổi mật khẩu.

File được tải lên sẽ được lưu trong thư mục:

```text
documents/user-{id}
```

Ví dụ:

```text
documents/user-1
```

## Lưu ý về file tài liệu

Ứng dụng chỉ hỗ trợ các định dạng:

- `PDF`
- `DOCX`
- `TXT`

Khi mở tài liệu, ứng dụng dùng chương trình mặc định của Windows. Vì vậy máy tính cần có phần mềm đọc file tương ứng, ví dụ trình đọc PDF hoặc Microsoft Word/WPS/LibreOffice cho file `DOCX`.

## Lỗi thường gặp và cách xử lý

### Lỗi không kết nối được SQL Server

Kiểm tra các điểm sau:

- SQL Server đã chạy chưa
- Database `DocumentManagerDB` đã được tạo chưa
- User và password có đúng không
- Cổng `1433` đã mở chưa
- Chuỗi `DMS_DB_URL` có đúng tên server/database không

Nếu dùng SQL Server Express, URL có thể cần đổi theo instance của máy bạn. Ví dụ:

```powershell
$env:DMS_DB_URL="jdbc:sqlserver://localhost\SQLEXPRESS;databaseName=DocumentManagerDB;encrypt=true;trustServerCertificate=true"
```

### Lỗi Maven không nhận lệnh `mvn`

Maven chưa được cài hoặc chưa được thêm vào `Path`.

Kiểm tra:

```powershell
mvn -version
```

Nếu PowerShell báo không tìm thấy lệnh, hãy cài Maven và thêm thư mục `bin` của Maven vào biến môi trường `Path`.

### Lỗi Java version

Dự án cần JDK 17 hoặc mới hơn.

Kiểm tra:

```powershell
java -version
```

Nếu máy đang dùng Java thấp hơn 17, hãy cài JDK 17+ và đặt lại `JAVA_HOME`.

### Lỗi không mở được file sau khi upload

Kiểm tra:

- File vẫn còn tồn tại trong thư mục `documents/user-{id}`
- Windows có phần mềm mặc định để mở định dạng file đó
- File không bị khóa bởi chương trình khác

### Lỗi upload file trùng tên

Trong cùng một tài khoản, ứng dụng không cho lưu hai file có cùng tên trong thư mục lưu trữ. Hãy đổi tên file rồi upload lại.

## Lệnh hữu ích cho lập trình viên

Biên dịch dự án:

```powershell
mvn -DskipTests package
```

Chạy ứng dụng:

```powershell
mvn javafx:run
```

Xóa file build cũ:

```powershell
mvn clean
```

Build lại từ đầu:

```powershell
mvn clean package
```

## Ghi chú bảo mật

Không nên đưa mật khẩu database thật vào mã nguồn khi dùng trong môi trường thật. Nên cấu hình bằng biến môi trường:

```powershell
$env:DMS_DB_PASSWORD="mat_khau_that"
```

Nếu chia sẻ dự án cho người khác, mỗi người nên tự cấu hình SQL Server và mật khẩu trên máy của mình.
