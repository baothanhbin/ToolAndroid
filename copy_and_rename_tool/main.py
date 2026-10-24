import os
import re
import time
from pathlib import Path
from tkinter import END, BooleanVar, StringVar, Tk, ttk
from tkinter import filedialog, messagebox
from tkinter.scrolledtext import ScrolledText

from scanFolderForRenameCandidates import (
    scan_folder_for_rename_candidates,
    scan_xml_folder_for_binding_map,
    rename_folder_using_candidates,
)
from android_setup import AndroidSetupHelper
from kotlin_ast_renamer import rename_kotlin_locals_and_private_members
from rename_transaction import RenameTransaction, find_gradle_project_root, run_verification_build

# ── Thống kê ───────────────────────────────────────────────────────────────────
class Stats:
    def __init__(self):
        self.dirs_copied = 0
        self.files_copied = 0
        self.bytes_copied = 0
        self.errors = 0

    def format_bytes(self) -> str:
        b = self.bytes_copied
        if b >= 1_073_741_824:
            return f"{b / 1_073_741_824:.2f} GB"
        if b >= 1_048_576:
            return f"{b / 1_048_576:.2f} MB"
        if b >= 1_024:
            return f"{b / 1_024:.2f} KB"
        return f"{b} B"

def fmt_bytes(n: int) -> str:
    if n >= 1_073_741_824:
        return f"{n / 1_073_741_824:.1f} GB"
    if n >= 1_048_576:
        return f"{n / 1_048_576:.1f} MB"
    if n >= 1_024:
        return f"{n / 1_024:.1f} KB"
    return f"{n} B"

# ── Core: đọc & liệt kê ───────────────────────────────────────────────────────
GIT_EXCLUDES = {
    ".git", ".gitattributes", ".gitmodules", ".gitkeep",
    ".gradle", ".idea", ".kotlin", "B9UI", "B9Default", "B9Util", "B9Ads"
}

EXCLUDED_LINES = {
    'implementation(project(":B9Ads"))',
    'implementation(project(":B9Util"))',
    'implementation(project(":B9Default"))',
    'implementation(project(":B9UI"))',
    "include ':B9UI'",
    "include ':B9Default'",
    "include ':B9Util'",
    "include ':B9Ads'",
}

def is_git_related(path: Path) -> bool:
    return path.name in GIT_EXCLUDES

def should_skip(path: Path) -> bool:
    name = path.name
    if path.is_dir() and name == "build":
        return True
    if name == "local.properties":
        return True
    if name.startswith("hs_err_pid") and name.endswith(".log"):
        return True
    return False

def should_filter_lines(path: Path) -> bool:
    return path.suffix in (".gradle", ".kts")

def copy_file(src: Path, dst: Path, stats: Stats, overwrite: bool, log_func):
    try:
        if dst.exists() and not overwrite:
            log_func(f"Đã tồn tại, bỏ qua: {dst}")
            return
        dst.parent.mkdir(parents=True, exist_ok=True)

        # Xóa file cũ trước để tạo hoàn toàn mới
        if dst.exists():
            dst.unlink()
        dst.touch()  # tạo file rỗng mới

        if should_filter_lines(src):
            lines = src.read_text(encoding="utf-8", errors="replace").splitlines()
            filtered = "\n".join(l for l in lines if l.strip() not in EXCLUDED_LINES)
            dst.write_text(filtered, encoding="utf-8")
        else:
            dst.write_bytes(src.read_bytes())

        stats.files_copied += 1
        stats.bytes_copied += src.stat().st_size
        log_func(f"✔ File: {src.name} → {dst}")
    except Exception as e:
        stats.errors += 1
        log_func(f"✘ Lỗi copy file '{src.name}': {e}")

def copy_directory(src: Path, dst: Path, stats: Stats, overwrite: bool, log_func):
    try:
        if not dst.exists():
            dst.mkdir(parents=True, exist_ok=True)
            log_func(f"✔ Tạo thư mục: {dst}")
        stats.dirs_copied += 1
        for child in src.iterdir():
            copy_path(child, dst / child.name, stats, overwrite, log_func)
    except Exception as e:
        stats.errors += 1
        log_func(f"✘ Lỗi copy thư mục '{src.name}': {e}")

def copy_path(src: Path, dst: Path, stats: Stats, overwrite: bool, log_func):
    if should_skip(src):
        log_func(f"⚠ Bỏ qua: {src}")
        return
    if is_git_related(src):
        log_func(f"⚠ Bỏ qua (liên quan Git): {src}")
        return
    if src.is_file():
        copy_file(src, dst, stats, overwrite, log_func)
    elif src.is_dir():
        copy_directory(src, dst, stats, overwrite, log_func)
    else:
        log_func(f"⚠ Bỏ qua (không phải file/thư mục): {src}")

def list_directory(path: Path, widget, indent: str = "", is_last: bool = True):
    prefix = "" if not indent else ("└── " if is_last else "├── ")
    icon = "📁" if path.is_dir() else "📄"
    size = f" ({fmt_bytes(path.stat().st_size)})" if path.is_file() else ""
    widget.insert(END, f"{indent}{prefix}{icon} {path.name}{size}\n")

    if path.is_dir():
        children = sorted(path.iterdir(), key=lambda f: (not f.is_dir(), f.name))
        for i, child in enumerate(children):
            next_indent = indent + ("    " if is_last else "│   ")
            list_directory(child, widget, next_indent, i == len(children) - 1)

# ── GUI ───────────────────────────────────────────────────────────────────────
class App:
    def __init__(self):
        self.root = Tk()
        self.root.title("Copy & Rename & Android Setup Tool")
        self.root.geometry("1050x750")
        self.root.minsize(950, 620)
        self.root.configure(bg="#f5f5f5")

        self.source_var = StringVar()
        self.dest_var = StringVar()
        self.action_var = StringVar(value="list")
        self.overwrite_var = BooleanVar(value=False)
        
        # Rename fields
        self.package_var = StringVar()
        self.suffix_var = StringVar()
        self.class_pattern_var = StringVar(value=r"class\s+(\w+)")
        self.fun_pattern_var = StringVar(value=r"fun\s+(\w+)\s*\(")
        self.strong_rename_var = BooleanVar(value=True)

        self.build_ui()

    def build_ui(self):
        main = ttk.Frame(self.root, padding=12)
        main.pack(fill="both", expand=True)

        # Header
        ttk.Label(
            main,
            text="� Copy & Rename & Android Setup Tool",
            font=("Segoe UI", 16, "bold")
        ).pack(anchor="w", pady=(0, 10))

        # Notebook for tabs
        notebook = ttk.Notebook(main)
        notebook.pack(fill="both", expand=True, pady=(0, 10))

        # ── TAB 1: Copy Tool
        copy_frame = ttk.Frame(notebook, padding=10)
        notebook.add(copy_frame, text="Copy Tool")
        self.build_copy_tab(copy_frame)

        # ── TAB 2: Rename Tool
        rename_frame = ttk.Frame(notebook, padding=10)
        notebook.add(rename_frame, text="Rename Tool")
        self.build_rename_tab(rename_frame)

        # ── TAB 3: Android Setup
        android_frame = ttk.Frame(notebook, padding=10)
        notebook.add(android_frame, text="Android Setup")
        self.build_android_setup_tab(android_frame)

    def build_copy_tab(self, parent):
        # Source row
        row1 = ttk.Frame(parent)
        row1.pack(fill="x", pady=4)
        ttk.Label(row1, text="Nguồn:").pack(side="left")
        self.source_entry = ttk.Entry(row1, textvariable=self.source_var)
        self.source_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))
        ttk.Button(row1, text="Browse", command=self.browse_source).pack(side="left")

        # Destination row
        row2 = ttk.Frame(parent)
        row2.pack(fill="x", pady=4)
        ttk.Label(row2, text="Đích:").pack(side="left")
        self.dest_entry = ttk.Entry(row2, textvariable=self.dest_var)
        self.dest_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))
        ttk.Button(row2, text="Browse", command=self.browse_dest).pack(side="left")

        # Options row
        row3 = ttk.Frame(parent)
        row3.pack(fill="x", pady=(10, 6))
        ttk.Label(row3, text="Hành động:").pack(side="left")
        actions = [
            ("Liệt kê nội dung", "list"),
            ("Copy", "copy"),
            ("Thông tin", "info")
        ]
        for text, value in actions:
            ttk.Radiobutton(
                row3,
                text=text,
                variable=self.action_var,
                value=value
            ).pack(side="left", padx=(6, 12))

        ttk.Checkbutton(
            row3,
            text="Ghi đè nếu đã tồn tại",
            variable=self.overwrite_var
        ).pack(side="left")

        # Buttons
        btn_row = ttk.Frame(parent)
        btn_row.pack(fill="x", pady=(8, 10))
        ttk.Button(btn_row, text="Thực hiện", command=self.run_copy_action).pack(side="left")
        ttk.Button(btn_row, text="Xóa kết quả", command=self.clear_output).pack(side="left", padx=(8, 0))

        # Output area
        out_label = ttk.Label(parent, text="Kết quả:")
        out_label.pack(anchor="w")
        self.output = ScrolledText(
            parent,
            wrap="word",
            height=20,
            font=("Consolas", 10)
        )
        self.output.pack(fill="both", expand=True)

    def build_rename_tab(self, parent):
        # Folder selection
        row1 = ttk.Frame(parent)
        row1.pack(fill="x", pady=4)
        ttk.Label(row1, text="Thư mục:").pack(side="left")
        self.rename_folder_var = StringVar()
        self.rename_folder_entry = ttk.Entry(row1, textvariable=self.rename_folder_var)
        self.rename_folder_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))
        ttk.Button(row1, text="Browse", command=self.browse_rename_folder).pack(side="left")

        # Package name
        row2 = ttk.Frame(parent)
        row2.pack(fill="x", pady=4)
        ttk.Label(row2, text="Package:").pack(side="left")
        self.package_entry = ttk.Entry(row2, textvariable=self.package_var)
        self.package_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))

        # Suffix
        row3 = ttk.Frame(parent)
        row3.pack(fill="x", pady=4)
        ttk.Label(row3, text="Suffix:").pack(side="left")
        self.suffix_entry = ttk.Entry(row3, textvariable=self.suffix_var)
        self.suffix_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))
        ttk.Label(row3, text="(ví dụ: New)").pack(side="left")

        # Class pattern
        row4 = ttk.Frame(parent)
        row4.pack(fill="x", pady=4)
        ttk.Label(row4, text="Class pattern:").pack(side="left")
        self.class_pattern_entry = ttk.Entry(row4, textvariable=self.class_pattern_var)
        self.class_pattern_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))

        # Function pattern
        row5 = ttk.Frame(parent)
        row5.pack(fill="x", pady=4)
        ttk.Label(row5, text="Function pattern:").pack(side="left")
        self.fun_pattern_entry = ttk.Entry(row5, textvariable=self.fun_pattern_var)
        self.fun_pattern_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))
        self.fun_pattern_entry.configure(state="disabled")
        ttk.Label(row5, text="(public/override vẫn được bảo vệ)").pack(side="left")

        row6 = ttk.Frame(parent)
        row6.pack(fill="x", pady=4)
        ttk.Checkbutton(
            row6,
            text="Semantic rename (Kotlin compiler): local variables, private parameters/functions",
            variable=self.strong_rename_var,
        ).pack(side="left")

        # Buttons
        btn_row = ttk.Frame(parent)
        btn_row.pack(fill="x", pady=(8, 10))
        ttk.Button(btn_row, text="Bắt đầu Rename", command=self.run_rename_action).pack(side="left")
        ttk.Button(btn_row, text="Xóa kết quả", command=self.clear_output).pack(side="left", padx=(8, 0))

        # Output area
        out_label = ttk.Label(parent, text="Kết quả:")
        out_label.pack(anchor="w")
        self.rename_output = ScrolledText(
            parent,
            wrap="word",
            height=18,
            font=("Consolas", 10)
        )
        self.rename_output.pack(fill="both", expand=True)

    def build_android_setup_tab(self, parent):
        # Project folder selection
        row1 = ttk.Frame(parent)
        row1.pack(fill="x", pady=4)
        ttk.Label(row1, text="Project Folder:").pack(side="left")
        self.android_project_var = StringVar()
        self.android_project_entry = ttk.Entry(row1, textvariable=self.android_project_var)
        self.android_project_entry.pack(side="left", fill="x", expand=True, padx=(8, 6))
        ttk.Button(row1, text="Browse", command=self.browse_android_project).pack(side="left")

        # Info text
        info_frame = ttk.LabelFrame(parent, text="Available Steps", padding=8)
        info_frame.pack(fill="x", pady=(8, 10))
        info_text = """• build.gradle (Project):
  ✔ 4.1 - Add plugins (com.android.application, kotlin, ksp, etc.)
  ✔ 4.2 - Add key/iv definitions & imports (Cipher, KeyStore, etc.)
  ✔ Update classpath versions (gradle 8.13.0, kotlin 2.1.0, etc.)
• gradle.properties - org.gradle.jvmargs=-Xmx6g
• gradle-wrapper.properties - distributionUrl=gradle-8.14.3
• build.gradle (App):
  ✔ compileSdk→compileSdkVersion (36)
  ✔ signingConfigs (debug + prd_release)
  ✔ buildTypes (release + debug with minify)
  ✔ bundle, productFlavors, sourceSets, compileOptions
  ✔ update dependencies versions
  ✔ add helper functions (getSHA256, calculateSHA256, encrypt)
  ✔ add module dependencies (B9Ads, B9Util, B9Default, B9UI)
• Create folder structure (copy for_dev_, key)
• Add strings.xml entries (UI text)
• Copy modules (B9Default, B9Ads, B9UI, B9Util)
• Update settings.gradle (include ':B9*')"""
        ttk.Label(info_frame, text=info_text, justify="left").pack(fill="x")

        # Buttons
        btn_row = ttk.Frame(parent)
        btn_row.pack(fill="x", pady=(8, 10))
        ttk.Button(btn_row, text="Run All Setup Steps", command=self.run_android_setup).pack(side="left", padx=(0, 8))
        ttk.Button(btn_row, text="Clear Output", command=lambda: self.android_setup_output.delete("1.0", END)).pack(side="left")

        # Output area
        out_label = ttk.Label(parent, text="Output:")
        out_label.pack(anchor="w")
        self.android_setup_output = ScrolledText(
            parent,
            wrap="word",
            height=20,
            font=("Consolas", 10)
        )
        self.android_setup_output.pack(fill="both", expand=True)

    def browse_android_project(self):
        folder = filedialog.askdirectory(title="Chọn Android Project Folder")
        if folder:
            self.android_project_var.set(folder)

    def log_android_setup(self, text):
        self.android_setup_output.insert(END, f"{text}\n")
        self.android_setup_output.see(END)
        self.root.update()

    def run_android_setup(self):
        project_raw = self.android_project_var.get().strip()
        if not project_raw:
            messagebox.showerror("Thiếu dữ liệu", "Vui lòng chọn Android project folder.")
            return

        project = Path(project_raw)
        if not project.exists() or not project.is_dir():
            messagebox.showerror("Lỗi", f"Folder không tồn tại:\n{project_raw}")
            return

        self.android_setup_output.delete("1.0", END)

        try:
            helper = AndroidSetupHelper(project, self.log_android_setup)
            success = helper.run_all_steps()
            
            if success:
                messagebox.showinfo("Thành công", "Android Setup hoàn tất!")
            else:
                messagebox.showwarning("Cảnh báo", "Một số bước không thành công. Xem output để chi tiết.")
        except Exception as e:
            self.log_android_setup(f"✘ Lỗi: {e}")
            messagebox.showerror("Lỗi", f"Có lỗi xảy ra:\n{e}")

    def browse_source(self):
        folder = filedialog.askdirectory(title="Chọn thư mục nguồn")
        if folder:
            self.source_var.set(folder)

    def browse_dest(self):
        folder = filedialog.askdirectory(title="Chọn thư mục đích")
        if folder:
            self.dest_var.set(folder)

    def browse_rename_folder(self):
        folder = filedialog.askdirectory(title="Chọn thư mục để rename")
        if folder:
            self.rename_folder_var.set(folder)

    def clear_output(self):
        self.output.delete("1.0", END)
        if hasattr(self, 'rename_output'):
            self.rename_output.delete("1.0", END)
        if hasattr(self, 'android_setup_output'):
            self.android_setup_output.delete("1.0", END)

    def log(self, text, is_rename=False):
        widget = self.rename_output if is_rename else self.output
        widget.insert(END, f"{text}\n")
        widget.see(END)
        self.root.update()

    def run_copy_action(self):
        src_raw = self.source_var.get().strip()
        dst_raw = self.dest_var.get().strip()
        action = self.action_var.get()
        overwrite = self.overwrite_var.get()

        if not src_raw:
            messagebox.showerror("Thiếu dữ liệu", "Vui lòng chọn/nhập đường dẫn nguồn.")
            return

        self.output.delete("1.0", END)
        src = Path(src_raw)
        if not src.exists():
            messagebox.showerror("Lỗi", f"Đường dẫn nguồn không tồn tại:\n{src_raw}")
            return

        if action == "list":
            if not src.is_dir():
                messagebox.showerror("Lỗi", "Chức năng Liệt kê chỉ hỗ trợ thư mục.")
                return
            self.log(f"Cấu trúc thư mục: {src.resolve()}")
            list_directory(src, self.output)
            total_files = total_dirs = total_size = 0
            for p in src.rglob("*"):
                if p.is_file():
                    total_files += 1
                    total_size += p.stat().st_size
                elif p.is_dir():
                    total_dirs += 1
            self.log("")
            self.log(f"Tổng: {total_dirs} thư mục, {total_files} file, {fmt_bytes(total_size)}")

        elif action == "info":
            self.show_info(src)

        else:
            if not dst_raw:
                messagebox.showerror("Thiếu dữ liệu", "Vui lòng chọn/nhập đường dẫn đích.")
                return
            dst = Path(dst_raw)
            self.log("Bắt đầu copy...")
            t0 = time.time()
            stats = Stats()

            if src.is_file() and dst.is_dir():
                real_dst = dst / src.name
            elif src.is_file() and not dst.suffix and not dst.exists():
                real_dst = dst
            else:
                real_dst = dst

            if src.is_dir():
                for child in src.iterdir():
                    copy_path(child, real_dst / child.name, stats, overwrite, self.log)
            else:
                copy_path(src, real_dst, stats, overwrite, self.log)

            elapsed = int((time.time() - t0) * 1000)
            self.log("")
            self.log(f"Kết quả:")
            self.log(f"- Thư mục đã tạo: {stats.dirs_copied}")
            self.log(f"- File đã copy: {stats.files_copied}")
            self.log(f"- Dung lượng: {stats.format_bytes()}")
            self.log(f"- Lỗi: {stats.errors}")
            self.log(f"- Thời gian: {elapsed} ms")

    def show_info(self, path: Path):
        if not path.exists():
            messagebox.showerror("Lỗi", f"Đường dẫn không tồn tại:\n{path}")
            return

        st = path.stat()
        self.log(f"Tên: {path.name}")
        self.log(f"Đường dẫn: {path.resolve()}")
        self.log(f"Loại: {'Thư mục' if path.is_dir() else 'File'}")
        self.log(f"Đọc được: {os.access(path, os.R_OK)}")
        self.log(f"Ghi được: {os.access(path, os.W_OK)}")
        self.log(f"Tạo lúc: {time.ctime(st.st_ctime)}")
        self.log(f"Sửa lần cuối: {time.ctime(st.st_mtime)}")

        if path.is_file():
            self.log(f"Kích thước: {fmt_bytes(st.st_size)}")
        else:
            files = dirs = size = 0
            for p in path.rglob("*"):
                if p.is_file():
                    files += 1
                    size += p.stat().st_size
                elif p.is_dir():
                    dirs += 1
            self.log(f"Bên trong: {dirs} thư mục con, {files} file")
            self.log(f"Tổng size: {fmt_bytes(size)}")

    def run_rename_action(self):
        folder_raw = self.rename_folder_var.get().strip()
        package = self.package_var.get().strip()
        suffix = self.suffix_var.get().strip()
        class_pattern_str = self.class_pattern_var.get().strip()
        fun_pattern_str = self.fun_pattern_var.get().strip()

        # Validate inputs
        if not folder_raw:
            messagebox.showerror("Thiếu dữ liệu", "Vui lòng chọn thư mục.")
            return
        if not package:
            messagebox.showerror("Thiếu dữ liệu", "Vui lòng nhập package name.")
            return
        if not suffix:
            messagebox.showerror("Thiếu dữ liệu", "Vui lòng nhập suffix.")
            return
        if not re.fullmatch(r"[A-Za-z_][A-Za-z0-9_]*", suffix):
            messagebox.showerror(
                "Invalid suffix",
                "Suffix must contain only letters, digits, or underscores and cannot start with a digit.",
            )
            return

        folder = Path(folder_raw)
        if not folder.exists() or not folder.is_dir():
            messagebox.showerror("Lỗi", f"Thư mục không tồn tại:\n{folder_raw}")
            return

        self.rename_output.delete("1.0", END)

        transaction = None
        try:
            # Compile regex patterns
            try:
                class_pattern = re.compile(class_pattern_str)
                fun_pattern = re.compile(fun_pattern_str)
            except re.error as e:
                messagebox.showerror("Lỗi Regex", f"Regex không hợp lệ:\n{e}")
                return

            ignore_names = {"main", "MainActivity", "Application"}

            self.log("Bắt đầu scan candidates...", is_rename=True)
            self.log(
                "Chế độ build-safe: public/override và API thư viện được bảo vệ.",
                is_rename=True,
            )
            t0 = time.time()
            project_root = find_gradle_project_root(folder)
            transaction = RenameTransaction(folder)

            # Scan for candidates
            candidates = scan_folder_for_rename_candidates(
                package, folder, class_pattern, class_pattern, fun_pattern, ignore_names
            )

            self.log(f"✔ Tìm thấy:", is_rename=True)
            self.log(f"  - {len(candidates.classes)} classes: {', '.join(sorted(candidates.classes))}", is_rename=True)
            self.log(f"  - {len(candidates.vars)} variables: {', '.join(sorted(candidates.vars)[:10])}" + 
                    ("..." if len(candidates.vars) > 10 else ""), is_rename=True)
            self.log(f"  - {len(candidates.functions)} functions: {', '.join(sorted(candidates.functions))}", is_rename=True)

            # Scan XML for binding map
            # Binding IDs can be referenced from every resource qualifier and
            # from res/xml, not only the default layout directory.
            xml_folder = folder / "res"
            if xml_folder.exists():
                candidates.xml_binding_map = scan_xml_folder_for_binding_map(xml_folder, suffix)
                self.log(f"  - {len(candidates.xml_binding_map)} XML binding maps", is_rename=True)

            self.log("", is_rename=True)
            self.log("Bắt đầu rename...", is_rename=True)

            if self.strong_rename_var.get():
                ast_stats = rename_kotlin_locals_and_private_members(
                    folder, suffix, package, project_root
                )
                self.log(
                    f"  - Semantic rename: {ast_stats.variables} variables/parameters, "
                    f"{ast_stats.functions} private functions trong {ast_stats.files} files",
                    is_rename=True,
                )

            # Rename files
            rename_folder_using_candidates(folder, candidates, suffix, package)

            transaction.commit()
            self.log("✔ Đã hoàn tất rename (bỏ qua bước build Gradle).", is_rename=True)

            elapsed = time.time() - t0
            self.log("", is_rename=True)
            self.log(f"✔ Hoàn tất! Thời gian: {elapsed:.2f}s", is_rename=True)
            messagebox.showinfo("Thành công", f"Rename hoàn tất trong {elapsed:.2f}s")

        except Exception as e:
            if transaction is not None:
                transaction.rollback()
                self.log("↩ Đã rollback toàn bộ thay đổi rename.", is_rename=True)
            self.log(f"✘ Lỗi: {e}", is_rename=True)
            messagebox.showerror("Lỗi", f"Có lỗi xảy ra:\n{e}")


def main():
    app = App()
    app.root.mainloop()


if __name__ == "__main__":
    main()
