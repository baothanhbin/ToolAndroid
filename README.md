# ToolAndroid

Ứng dụng desktop dành cho Windows, hỗ trợ thao tác file, đổi tên mã nguồn Android và tự động cấu hình project. Giao diện được viết bằng Python/Tkinter và có thể chạy trực tiếp bằng file `.exe`.

## Tính năng

### Copy Tool

- Liệt kê cấu trúc thư mục và thống kê số file, thư mục, dung lượng.
- Sao chép file hoặc toàn bộ thư mục.
- Xem thông tin chi tiết của file/thư mục.
- Bỏ qua các file và thư mục không cần thiết như `.git`, `.gradle`, `.idea` và `build`.

### Rename Tool

- Thêm suffix vào tên class Java/Kotlin và đổi tên file tương ứng.
- Cập nhật import, tham chiếu class và custom view trong XML.
- Đổi XML ID và cập nhật View Binding/Data Binding tương ứng.
- Rename local variable, private function và parameter của private function bằng symbol resolution của Kotlin compiler.
- Bỏ qua thư mục generated/build để tránh sửa nhầm mã nguồn sinh tự động.
- Không sửa nội dung chuỗi thông thường.

Rename Tool kết hợp hai cơ chế:

- Kotlin compiler frontend xác định declaration thật và chỉ đổi reference resolve về đúng declaration đó.
- Bộ rename build-safe xử lý class/file, XML ID và binding field.

Public API, hàm `override`, inherited member, property qua receiver và member của Android/Kotlin/thư viện ngoài được giữ nguyên. Tool tự chạy `clean assembleDebug`; nếu build lỗi, toàn bộ nội dung và tên file trong lần rename được rollback.

### Android Setup

- Cập nhật cấu hình Gradle của project và module app.
- Cập nhật Gradle wrapper và `gradle.properties`.
- Cấu hình signing, build types, product flavors và source sets.
- Thêm các dependency/module `B9Ads`, `B9Default`, `B9UI`, `B9Util`.
- Sao chép tài nguyên `for_dev_` và cấu trúc thư mục cần thiết.
- Cập nhật strings và Android Manifest theo cấu hình của tool.

## Chạy ứng dụng

### Cách nhanh nhất: dùng file EXE

Mở file sau bằng double-click:

```text
copy_and_rename_tool\dist\ToolAndroid.exe
```

File EXE đã đóng gói Python, Tkinter và các tài nguyên cần thiết nên không yêu cầu cài Python trên máy đích.

### Chạy từ source

Yêu cầu Python 3.10 trở lên có hỗ trợ Tkinter và Android Studio (tool dùng Kotlin compiler đi kèm).

```powershell
cd copy_and_rename_tool
python -m pip install -r requirements.txt
python main.py
```

## Hướng dẫn sử dụng

### Copy Tool

1. Mở tab **Copy Tool**.
2. Chọn đường dẫn nguồn tại **Source**.
3. Chọn đường dẫn đích tại **Destination** nếu dùng chức năng Copy.
4. Chọn một thao tác:
   - **List**: liệt kê nội dung thư mục.
   - **Copy**: sao chép dữ liệu.
   - **Info**: xem thông tin đường dẫn.
5. Nhấn **Execute** và kiểm tra kết quả trong khung log.

### Rename Tool

> Nên commit hoặc sao lưu project trước khi rename. Tool có rollback tự động khi build lỗi, nhưng không thay thế version control.

1. Mở tab **Rename Tool**.
2. Tại **Thư mục**, chọn thư mục source set của module app, thông thường là:

   ```text
   <project>\app\src\main
   ```

3. Nhập package của ứng dụng, ví dụ `com.example.app`.
4. Nhập suffix hợp lệ, ví dụ `New` hoặc `B9`.
5. Kiểm tra Class pattern. Giá trị mặc định là:

   ```regex
   class\s+(\w+)
   ```

6. Giữ tùy chọn **Semantic rename (Kotlin compiler)** nếu muốn đổi local variable và private function.
7. Nhấn **Bắt đầu Rename**.
8. Tool tự chạy `clean assembleDebug`. Nếu build thất bại, thay đổi rename được rollback tự động.

Rename chỉ áp dụng trong thư mục đã chọn. Nếu chọn `app\src\main`, các module khác như `security` sẽ không bị sửa.

### Android Setup

1. Mở tab **Android Setup**.
2. Chọn thư mục root của Android project — nơi chứa `app`, `gradle` và file Gradle cấp project.
3. Nhấn **Run All Setup Steps**.
4. Theo dõi từng bước trong khung Output.
5. Kiểm tra lại các dữ liệu riêng của project, đặc biệt:
   - `google-services.json`;
   - keystore và mật khẩu signing;
   - activity trong `AndroidManifest.xml`;
   - class `Application`;
   - package/namespace của ứng dụng.

## Build lại file EXE

PyInstaller spec đã được lưu tại `copy_and_rename_tool/ToolAndroid.spec`.

```powershell
cd copy_and_rename_tool
python -m PyInstaller --noconfirm --clean ToolAndroid.spec
```

File kết quả được tạo tại:

```text
copy_and_rename_tool\dist\ToolAndroid.exe
```

## Cấu trúc chính

```text
ToolAndroid/
├── copy_and_rename_tool/
│   ├── main.py                          # Giao diện và entry point
│   ├── scanFolderForRenameCandidates.py # Logic scan/rename
│   ├── android_setup.py                 # Tự động cấu hình Android project
│   ├── kotlin_ast_renamer.py             # Python bridge tới Kotlin semantic engine
│   ├── kotlin-symbol-engine/             # Compiler frontend và packaged helper JAR
│   ├── rename_transaction.py             # Build verification và rollback
│   ├── test_rename_tool.py              # Regression tests cho Rename Tool
│   ├── requirements.txt                  # Python dependencies
│   ├── ToolAndroid.spec                 # Cấu hình build EXE
│   ├── B9Ads/
│   ├── B9Default/
│   ├── B9UI/
│   ├── B9Util/
│   └── dist/ToolAndroid.exe
└── README.md
```

## Kiểm thử

Chạy regression tests của Rename Tool:

```powershell
cd copy_and_rename_tool
python -m unittest -v test_rename_tool
```

Khi thay đổi logic rename, nên kiểm tra trên bản sao sạch của project và chạy `clean assembleDebug` trước khi sử dụng trên source chính.

## Xử lý sự cố

### Không tìm thấy project

- Kiểm tra đường dẫn có tồn tại và tài khoản hiện tại có quyền đọc/ghi.
- Với Android Setup, phải chọn thư mục root của project.
- Với Rename Tool, nên chọn `<project>\app\src\main`.

### Rename không tìm thấy class

- Kiểm tra package đã nhập đúng.
- Kiểm tra Class pattern có group capture cho tên class.
- Đảm bảo mã nguồn nằm trong thư mục đã chọn.

### Build lỗi sau rename

- Không chạy rename nhiều lần với cùng một suffix.
- Kiểm tra log để xác định symbol hoặc resource bị đổi sai.
- Khôi phục bản sao/commit trước đó, cập nhật tool rồi chạy lại từ source sạch.

### Windows cảnh báo khi mở EXE

File được đóng gói cục bộ và chưa được ký code-signing. Windows SmartScreen có thể hiển thị cảnh báo với ứng dụng chưa có chữ ký số.

## Giấy phép

MIT License.
