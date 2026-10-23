# � Copy & Rename & Android Setup Tool

## 🚀 Giới thiệu
Bộ công cụ **GUI** hỗ trợ quản lý file, đổi tên code, và tự động setup Android project.  
Viết bằng **Python + Tkinter**, cung cấp giao diện thân thiện và dễ sử dụng.

---

## ✨ 3 Chức năng chính

### 1️⃣ **Copy Tool** 📂
- ✔ **List**: Liệt kê tất cả file/folder trong source
- ✔ **Copy**: Sao chép file từ source → destination
- ✔ **Info**: Hiển thị thông tin chi tiết (kích thước, số lượng)
- 🚫 Hỗ trợ ignore file theo danh sách

### 2️⃣ **Rename Tool** 🔄
- 🧩 **Đổi tên Class** tự động (Java/Kotlin)
- ⚙️ **Đổi tên Function** và Methods
- 🔤 **Đổi tên Variables** an toàn
- 📝 **Đổi tên XML IDs** thành Binding fields
- 🎯 **Smart Pattern**: Tùy chỉnh regex pattern
- 🚫 Tránh đổi tên bên trong string literals

### 3️⃣ **Android Setup** 🚀
- ✔ Cập nhật `build.gradle` (Project + App)
- ✔ Cập nhật `gradle.properties`
- ✔ Cập nhật `gradle-wrapper.properties`
- ✔ Tạo folder structure (`for_dev_`, `key`)
- ✔ Thêm strings.xml entries
- ✔ Copy folder từ project (for_dev_, key)

---

## 🛠️ Công nghệ sử dụng
- **Python 3.14.3**
- **Tkinter** (GUI framework)
- **pathlib** (file operations)
- **regex** (pattern matching)

---

## 📦 Cài đặt & chạy

### 1️⃣ Yêu cầu
- Python 3.8+
- Tkinter (thường có sẵn với Python)

### 2️⃣ Chạy ứng dụng
```bash
cd copy_and_rename_tool
python main.py
```

### 3️⃣ Sử dụng Copy Tool
1. Chọn **Tab "Copy Tool"**
2. Nhập **Source folder** (hoặc click Browse)
3. Nhập **Destination folder** (hoặc click Browse)
4. Chọn hành động:
   - **List**: Xem danh sách file
   - **Copy**: Sao chép file
   - **Info**: Xem thông tin
5. Click **Execute** → xem kết quả

### 4️⃣ Sử dụng Rename Tool
1. Chọn **Tab "Rename Tool"**
2. Nhập **Project folder** (Android project)
3. Nhập **Package name** (vd: `com.example.app`)
4. Nhập **Suffix** để thêm vào cuối (vd: `NEW`)
5. *Tùy chỉnh regex (nếu cần):*
   - Class pattern: `\b([A-Z][a-zA-Z0-9]*)\b`
   - Function pattern: `fun\s+(\w+)\s*\(`
6. Click **Scan & Rename** → review & confirm

### 5️⃣ Sử dụng Android Setup
1. Chọn **Tab "Android Setup"**
2. Chọn **Project folder** (Android project root)
3. Click **Run All Setup Steps** 
4. Xem output để kiểm tra từng bước
5. **Manual steps** (sau khi run):
   - Copy `google-services.json` vào `for_dev_`
   - Copy keystore file vào `key/`
   - Chỉnh sửa `AndroidManifest.xml` (activities)
   - Chỉnh sửa `MyApplication.kt`

---

## 📋 File cấu hình

### `ignore.txt`
Danh sách file/folder bỏ qua khi copy/rename:
```
.git
.gradle
build
.idea
*.iml
node_modules
```

---

## 🐛 Troubleshooting

**❌ Lỗi: "Project folder not found"**
- Kiểm tra đường dẫn tuyệt đối có tồn tại không

**❌ Lỗi: "build.gradle not found"**
- Đảm bảo chọn thư mục **project root** (chứa `app/`, `gradle/`)

**❌ Rename không hoạt động**
- Kiểm tra **Package name** đúng
- Kiểm tra **regex pattern** hợp lệ
- Xem log để biết file bị skip

---

## 📄 License
MIT License

