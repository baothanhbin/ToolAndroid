from __future__ import annotations

import re
import shutil
from pathlib import Path
from typing import Callable, Optional


class AndroidSetupHelper:
    """Hỗ trợ setup Android project theo quy chuẩn"""
    
    def __init__(self, project_root: Path, log_func: Callable[[str], None]):
        self.project_root = project_root
        self.app_folder = project_root / "app"
        self.log = log_func

    def _plugin_already_exists(self, content: str, plugin_id: str) -> bool:
        """Check plugin tồn tại bất kể dùng ' hay " hay id() hay id ''"""
        # Normalize: tìm chuỗi plugin_id xuất hiện trong content, bỏ qua loại quote
        escaped = re.escape(plugin_id)
        return bool(re.search(
            rf"""id\s*[\(\s]['"]{escaped}['"][\)]?""",
            content
        ))    

    def update_build_gradle_project(self):
        """Cập nhật build.gradle (Project)"""
        build_gradle = self.project_root / "build.gradle"
        if not build_gradle.exists():
            self.log(f"✘ Không tìm thấy: {build_gradle}")
            return False

        try:
            content = build_gradle.read_text(encoding="utf-8")
            
            # Cập nhật classpath trong dependencies
            classpath_updates = {
                r"classpath 'com\.android\.tools\.build:gradle:[\d.]+\'": "classpath 'com.android.tools.build:gradle:8.13.0'",
                r"classpath 'org\.jetbrains\.kotlin:kotlin-gradle-plugin:[\d.]+\'": "classpath 'org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.0'",
                r"classpath 'com\.google\.gms:google-services:[\d.]+\'": "classpath 'com.google.gms:google-services:4.4.4'",
                r"classpath 'com\.google\.firebase:firebase-crashlytics-gradle:[\d.]+\'": "classpath 'com.google.firebase:firebase-crashlytics-gradle:3.0.6'"
            }
            
            for pattern, replacement in classpath_updates.items():
                if re.search(pattern, content):
                    content = re.sub(pattern, replacement, content)
                else:
                    content = re.sub(
                        r'(dependencies\s*\{)',
                        rf'\1\n    {replacement}',
                        content
                    )
            
            self.log(f"  ✔ Cập nhật classpath versions")
            
            build_gradle.write_text(content, encoding="utf-8")
            self.log(f"✔ Cập nhật: build.gradle (Project)")
            return True
        except Exception as e:
            self.log(f"✘ Lỗi cập nhật build.gradle (Project): {e}")
            return False

    def update_gradle_properties(self):
        """Cập nhật gradle.properties"""
        gradle_props = self.project_root / "gradle.properties"
        if not gradle_props.exists():
            self.log(f"✘ Không tìm thấy: {gradle_props}")
            return False

        content = gradle_props.read_text(encoding="utf-8")
        content = re.sub(
            r'org\.gradle\.jvmargs\s*=.*',
            'org.gradle.jvmargs=-Xmx6g',
            content
        )
        gradle_props.write_text(content, encoding="utf-8")
        self.log(f"✔ Cập nhật: gradle.properties")
        return True

    def update_gradle_wrapper_properties(self):
        """Cập nhật gradle/wrapper/gradle-wrapper.properties"""
        wrapper_props = self.project_root / "gradle" / "wrapper" / "gradle-wrapper.properties"
        if not wrapper_props.exists():
            self.log(f"✘ Không tìm thấy: {wrapper_props}")
            return False

        content = wrapper_props.read_text(encoding="utf-8")
        content = re.sub(
            r'distributionUrl\s*=.*',
            r'distributionUrl=https\://services.gradle.org/distributions/gradle-8.14.3-bin.zip',
            content
        )
        wrapper_props.write_text(content, encoding="utf-8")
        self.log(f"✔ Cập nhật: gradle-wrapper.properties")
        return True

    def create_folder_structure(self):
        """Copy folder for_dev_ và key từ copy_and_rename_tool vào project app"""
        try:
            script_dir = Path(__file__).parent
            
            # Copy for_dev_ vào app/src/
            src_for_dev = script_dir / "for_dev_"
            dest_for_dev = self.app_folder / "src" / "for_dev_"
            
            if dest_for_dev.exists():
                self.log(f"ℹ Folder đã tồn tại: for_dev_")
            elif src_for_dev.exists():
                shutil.copytree(src_for_dev, dest_for_dev)
                self.log(f"✔ Copy folder: for_dev_ → {dest_for_dev}")
            else:
                self.log(f"⚠ Không tìm thấy: {src_for_dev}")
            
            # Copy key vào app/
            src_key = script_dir / "key"
            dest_key = self.app_folder / "key"
            
            if dest_key.exists():
                self.log(f"ℹ Folder đã tồn tại: key")
            elif src_key.exists():
                shutil.copytree(src_key, dest_key)
                self.log(f"✔ Copy folder: key → {dest_key}")
            else:
                self.log(f"⚠ Không tìm thấy: {src_key}")
            
            return True
        except Exception as e:
            self.log(f"✘ Lỗi copy folder: {e}")
            return False

    def add_strings_xml_entries(self):
        """Thêm entries vào strings.xml"""
        strings_file = self.app_folder / "src" / "main" / "res" / "values" / "strings.xml"
        if not strings_file.exists():
            self.log(f"⚠ Không tìm thấy: {strings_file}")
            return False

        content = strings_file.read_text(encoding="utf-8")
        
        new_entries = '''    <!--    text activity intro-->
    <string name="title_intro_1">Browse Safely</string>
    <string name="title_intro_2">Speedy Internet</string>
    <string name="title_intro_3">Explore Unlimted Web</string>
    <string name="description_intro_1">Keep your data safe and stay anonymous</string>
    <string name="description_intro_2">Enjoy swift browsing with free servers</string>
    <string name="description_intro_3">Surf the Internet with secure connection</string>
    <string name="cannot_access_this_file">Cannot access this file</string>
    <string name="something_went_wrong">Something went wrong</string>
    <string name="string_language">Language</string>
    <string name="update_available">Update available</string>
    <string name="what_s_new">What\'s new</string>
    <string name="to_use_this_app_download_the_latest_version">To use this app, download the latest version</string>
    <string name="update">Update</string>
    <string name="retry">Retry</string>
    <string name="thank_you_for_taking_the_time_to_rate_us_i_m_really_appreciate_that">Thank you for taking the time to rate us. I\'m really appreciate that.</string>
    <string name="txt_language">Language</string>
    <string name="are_you_sure_you_want_to_exit">Are you sure you want to exit?</string>
    <string name="yes">Yes</string>
    <string name="no">No</string>
    <string name="no_internet_connection">No Internet Connection</string>
    <string name="please_check_your_internet_connection_and_try_again">Please check your internet connection and try again</string>
    <string name="got_it">Got it</string>
    <string name="get_started">Get Started</string>
    <string name="history">History</string>
    <string name="clear_history">Clear History</string>
    <string name="rating">Rating</string>
    <string name="chat">Chat</string>
    <string name="choose_language">Choose language</string>
    <string name="please_select_a_language_to_continue">Please select a language to continue</string>
'''
        
        # Kiểm tra từng entry riêng lẻ để tránh thêm duplicate
        entries_to_add = []
        for line in new_entries.strip().split('\n'):
            line = line.strip()
            if not line or line.startswith('<!--'):
                entries_to_add.append(line)
            else:
                # Trích xuất string name từ <string name="...">
                name_match = re.search(r'<string name="([^"]+)"', line)
                if name_match:
                    string_name = name_match.group(1)
                    # Kiểm tra nếu string name này chưa tồn tại
                    if f'name="{string_name}"' not in content:
                        entries_to_add.append(line)
                else:
                    entries_to_add.append(line)
        
        if entries_to_add:
            entries_str = '\n    '.join(entries_to_add)
            content = content.replace('</resources>', f'    {entries_str}\n</resources>')
            strings_file.write_text(content, encoding="utf-8")
            self.log(f"✔ Thêm entries vào: strings.xml")
        else:
            self.log(f"ℹ Entries đã tồn tại trong: strings.xml")
        
        return True

    def run_all_steps(self):
        """Chạy tất cả các bước setup"""
        steps = [
            ("Cập nhật build.gradle (Project)", self.update_build_gradle_project),
            ("Cập nhật gradle.properties", self.update_gradle_properties),
            ("Cập nhật gradle-wrapper.properties", self.update_gradle_wrapper_properties),
            ("Cập nhật build.gradle (App) toàn diện", self.update_app_build_gradle_comprehensive),
            ("Tạo folder structure", self.create_folder_structure),
            ("Thêm strings.xml entries", self.add_strings_xml_entries),
            ("Copy modules (B9*)", self.copy_modules),
            ("Cập nhật settings.gradle", self.update_settings_gradle),
            ("Cập nhật build.gradle (App) - module dependencies", self.update_build_gradle_app_dependencies),
            ("Cập nhật AndroidManifest.xml", self.update_android_manifest),
        ]
        
        self.log("")
        self.log("📋 Bắt đầu setup Android project...")
        self.log("")
        
        success_count = 0
        for step_name, step_func in steps:
            try:
                if step_func():
                    success_count += 1
            except Exception as e:
                self.log(f"✘ Lỗi: {step_name}: {e}")
        
        self.log("")
        self.log(f"✔ Hoàn tất: {success_count}/{len(steps)} bước")
        return success_count == len(steps)

    def update_app_build_gradle_comprehensive(self):
        """Cập nhật build.gradle (App) toàn diện theo yêu cầu"""
        build_gradle = self.app_folder / "build.gradle"
        if not build_gradle.exists():
            self.log(f"✘ Không tìm thấy: {build_gradle}")
            return False

        try:
            content = build_gradle.read_text(encoding="utf-8")

            # 4.1 - Kiểm tra và thêm/thay thế plugins
            plugin_ids = {
                'id("com.google.devtools.ksp") version "2.1.0-1.0.29"': "com.google.devtools.ksp",
                "id 'com.android.application'":         "com.android.application",
                "id 'kotlin-android'":                  "kotlin-android",
                "id 'kotlin-parcelize'":                "kotlin-parcelize",
                "id 'com.google.gms.google-services'":  "com.google.gms.google-services",
                "id 'com.google.firebase.crashlytics'": "com.google.firebase.crashlytics",
            }

            # Thay đổi: nếu plugin đã tồn tại nhưng khác version, thay thế dòng hiện có
            plugins_match = re.search(r'plugins\s*\{', content)
            if plugins_match:
                # tìm phạm vi block plugins bằng brace-matching
                brace_start = content.index('{', plugins_match.start())
                brace_count = 0
                brace_end = -1
                for i in range(brace_start, len(content)):
                    if content[i] == '{':
                        brace_count += 1
                    elif content[i] == '}':
                        brace_count -= 1
                        if brace_count == 0:
                            brace_end = i
                            break

                if brace_end != -1:
                    inner = content[brace_start+1:brace_end]
                    inner_new = inner
                    for plugin_line, plugin_id in plugin_ids.items():
                        esc = re.escape(plugin_id)
                        # match id('xxx') or id "xxx" with optional version suffix
                        pat = rf'id\s*[\(\s][\"\']{esc}[\"\'][\)]?(?:\s*version\s*[\"\'][^\"\']+[\"\'])?'
                        if re.search(pat, inner_new):
                            inner_new = re.sub(pat, plugin_line, inner_new)
                        else:
                            # thêm nếu chưa có
                            inner_new = inner_new + '\n    ' + plugin_line

                    # rebuild content with updated plugins block
                    content = content[:brace_start+1] + inner_new + content[brace_end:]
            else:
                # không có block plugins, thêm plugin_line vào đầu file
                for plugin_line, plugin_id in plugin_ids.items():
                    content = re.sub(r'(plugins\s*\{)', rf'\1\n    {plugin_line}', content)
            
            self.log(f"  ✔ Kiểm tra/thêm plugins")
            # 4.2 - Thêm key, iv definitions và imports sau plugins (sử dụng brace-matching)
            key_iv_block = '''
        def key = "21f24ce47eb338cc666d344d438ec437"
        def iv = "fed2bb7f32abe4cd361a28a9051475b9"

        import com.android.build.gradle.internal.dsl.SigningConfig
        import javax.crypto.Cipher
        import javax.crypto.spec.IvParameterSpec
        import javax.crypto.spec.SecretKeySpec
        import java.security.KeyStore
        import java.security.MessageDigest
        '''
            if 'def key =' not in content:
                plugins_match = re.search(r'plugins\s*\{', content)
                if plugins_match:
                    brace_start = content.index('{', plugins_match.start())
                    brace_count = 0
                    plugins_end = -1
                    for i in range(brace_start, len(content)):
                        if content[i] == '{':
                            brace_count += 1
                        elif content[i] == '}':
                            brace_count -= 1
                            if brace_count == 0:
                                plugins_end = i + 1
                                break
                    if plugins_end != -1:
                        content = content[:plugins_end] + key_iv_block + content[plugins_end:]
                        self.log("  ✔ Thêm key/iv")
            
            # 4.3 - Đổi compileSdk thành compileSdkVersion 36
            content = re.sub(
                r'compileSdk\s+\d+',
                'compileSdkVersion 36',
                content
            )
            
            # 4.4 - Thêm signingConfigs sau compileSdkVersion
            signing_config_block = '''
    signingConfigs {
        debug {
            keyAlias 'key_store_lib'
            keyPassword 'vtd@123.net'
            storePassword 'vtd@123.net'
            storeFile file('key/key_store_lib')
        }

        prd_release {
            keyAlias 'key_store_lib'
            keyPassword 'vtd@123.net'
            storePassword 'vtd@123.net'
            storeFile file('key/key_store_lib')
        }
    }
'''
            if 'signingConfigs' not in content:
                content = re.sub(
                    r'(compileSdkVersion\s+\d+)',
                    rf'\1{signing_config_block}',
                    content
                )
                self.log(f"  ✔ Thêm signingConfigs")
            
            # 4.5 - Đổi minSdk/targetSdk thành minSdkVersion/targetSdkVersion
            content = re.sub(r'\bminSdk\b', 'minSdkVersion', content)
            content = re.sub(r'\btargetSdk\b', 'targetSdkVersion', content)
            content = re.sub(r'minSdkVersion\s+\d+', 'minSdkVersion 29', content)
            content = re.sub(r'targetSdkVersion\s+\d+', 'targetSdkVersion 36', content)
            self.log(f"  ✔ Cập nhật SDK versions")
            
            # 4.6 - Thay buildTypes (sử dụng brace-matching)
            build_types_block = '''buildTypes {
        release {
            debuggable false
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            signingConfig signingConfigs.prd_release
            def sha256Encrypted = getSHA256FromSigningConfig(signingConfigs.prd_release, key, iv)
            buildConfigField 'String', 'KEYSTORE_SHA256', "\\\"${sha256Encrypted}\\""
        }
        debug {
            debuggable true
            minifyEnabled true
            shrinkResources true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
            def sha256Encrypted = getSHA256FromSigningConfig(signingConfigs.debug, key, iv)
            buildConfigField 'String', 'KEYSTORE_SHA256', "\\\"${sha256Encrypted}\\""
        }
    }'''
            
            # Tìm và thay thế buildTypes block
            content = self._replace_gradle_block(content, 'buildTypes', build_types_block)
            self.log(f"  ✔ Cập nhật buildTypes")
            
            # 4.7 - Thêm bundle, flavorDimensions, productFlavors, sourceSets, compileOptions
            flavors_and_compile = '''
    bundle {
        density {
            enableSplit true
        }
        abi {
            enableSplit true
        }
        language {
            enableSplit false
        }
    }

    flavorDimensions 'default'
    productFlavors {
        for_dev_ {
            dimension 'default'
        }
//        for_product_ {
//            dimension 'default'
//        }
    }

    sourceSets {
        getByName('for_dev_') {
            res.srcDirs = ['src/for_dev_/res']
        }
//        getByName('for_product_') {
//            res.srcDirs = ['src/for_product_/res']
//        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
'''
            if 'productFlavors' not in content:
                bt_match = re.search(r'\bbuildTypes\s*\{', content)
                if bt_match:
                    brace_start = content.index('{', bt_match.start())
                    brace_count = 0
                    bt_end = -1
                    for i in range(brace_start, len(content)):
                        if content[i] == '{':
                            brace_count += 1
                        elif content[i] == '}':
                            brace_count -= 1
                            if brace_count == 0:
                                bt_end = i + 1
                                break
                    if bt_end != -1:
                        content = content[:bt_end] + flavors_and_compile + content[bt_end:]
                        self.log("  ✔ Thêm bundle, productFlavors, sourceSets, compileOptions")
            
            # 4.8 - Cập nhật dependencies versions
            content = self._update_dependencies_versions(content)
            
            # 4.9 - Thêm helper functions
            content = self._add_helper_functions_to_gradle(content)
            
            build_gradle.write_text(content, encoding="utf-8")
            self.log(f"✔ Cập nhật: build.gradle (App)")
            return True
        except Exception as e:
            self.log(f"✘ Lỗi cập nhật build.gradle (App): {e}")
            return False

    def _replace_gradle_block(self, content: str, block_name: str, new_block: str) -> str:
        """Thay thế Gradle block với proper brace matching"""
        pattern = rf'{block_name}\s*\{{'
        start_idx = content.find(block_name)
        
        if start_idx == -1:
            return content
        
        # Tìm vị trí mở ngoặc đầu tiên
        brace_start = content.find('{', start_idx)
        if brace_start == -1:
            return content
        
        # Đếm ngoặc để tìm ngoặc đóng
        brace_count = 0
        brace_end = -1
        for i in range(brace_start, len(content)):
            if content[i] == '{':
                brace_count += 1
            elif content[i] == '}':
                brace_count -= 1
                if brace_count == 0:
                    brace_end = i
                    break
        
        if brace_end == -1:
            return content
        
        # Thay thế block
        return content[:start_idx] + new_block + content[brace_end + 1:]

    def _update_dependencies_versions(self, content: str) -> str:
        """4.8 - Cập nhật versions của các dependencies"""
        dependencies_map = {
            r"implementation\s+['\"]androidx\.core:core-ktx:[^'\"]*['\"]": "implementation 'androidx.core:core-ktx:1.17.0'",
            r"implementation\s+['\"]androidx\.appcompat:appcompat:[^'\"]*['\"]": "implementation 'androidx.appcompat:appcompat:1.7.1'",
            r"implementation\s+['\"]com\.google\.android\.material:material:[^'\"]*['\"]": "implementation 'com.google.android.material:material:1.13.0'",
            r"implementation\s+['\"]androidx\.constraintlayout:constraintlayout:[^'\"]*['\"]": "implementation 'androidx.constraintlayout:constraintlayout:2.2.1'",
            r"implementation\s+['\"]androidx\.activity:activity:[^'\"]*['\"]": "implementation 'androidx.activity:activity:1.12.2'",
            r"implementation\s+['\"]androidx\.lifecycle:lifecycle-service:[^'\"]*['\"]": "implementation 'androidx.lifecycle:lifecycle-service:2.10.0'",
            r"testImplementation\s+['\"]junit:junit:[^'\"]*['\"]": "testImplementation 'junit:junit:4.13.2'",
            r"implementation\s+['\"]com\.google\.android\.play:asset-delivery-ktx:[^'\"]*['\"]": "implementation 'com.google.android.play:asset-delivery-ktx:2.2.2'",
            r"implementation\s+['\"]com\.google\.android\.play:review-ktx:[^'\"]*['\"]": "implementation 'com.google.android.play:review-ktx:2.0.1'",
            r"implementation\s+['\"]com\.github\.bumptech\.glide:glide:[^'\"]*['\"]": "implementation 'com.github.bumptech.glide:glide:4.15.1'",
            r"annotationProcessor\s+['\"]com\.github\.bumptech\.glide:compiler:[^'\"]*['\"]": "annotationProcessor 'com.github.bumptech.glide:compiler:4.13.2'",
            r"implementation\s+['\"]com\.android\.billingclient:billing:[^'\"]*['\"]": "implementation \"com.android.billingclient:billing:9.1.0\"",
            r"implementation\s+['\"]androidx\.multidex:multidex:[^'\"]*['\"]": "implementation \"androidx.multidex:multidex:2.0.1\"",
            r"implementation\s+['\"]com\.facebook\.shimmer:shimmer:[^'\"]*['\"]": "implementation \"com.facebook.shimmer:shimmer:0.5.0\"",
            r"implementation\s+['\"]com\.tbuonomo:dotsindicator:[^'\"]*['\"]": "implementation \"com.tbuonomo:dotsindicator:5.1.0\"",
            r"implementation\s+['\"]com\.google\.android\.ump:user-messaging-platform:[^'\"]*['\"]": "implementation \"com.google.android.ump:user-messaging-platform:3.2.0\"",
            r"implementation\s+['\"]com\.google\.ads\.mediation:facebook:[^'\"]*['\"]": "implementation \"com.google.ads.mediation:facebook:6.20.0.0\"",
            r"implementation\s+['\"]com\.google\.ads\.mediation:pangle:[^'\"]*['\"]": "implementation \"com.google.ads.mediation:pangle:7.3.0.3.0\"",
            r"implementation\s+['\"]com\.google\.ads\.mediation:applovin:[^'\"]*['\"]": "implementation \"com.google.ads.mediation:applovin:13.3.1.1\"",
            r"implementation\s+['\"]com\.google\.android\.play:review:[^'\"]*['\"]": "implementation \"com.google.android.play:review:2.0.2\"",
            r"implementation\s+['\"]androidx\.core:core-splashscreen:[^'\"]*['\"]": "implementation \"androidx.core:core-splashscreen:1.0.1\"",
        }
        
        for pattern, replacement in dependencies_map.items():
            if re.search(pattern, content):
                # Đã tồn tại (không comment), cập nhật version
                content = re.sub(pattern, replacement, content)
            else:
                # Chưa tồn tại, kiểm tra xem có comment không
                comment_pattern = r"//\s*" + pattern
                if not re.search(comment_pattern, content):
                    # Không có comment, thêm vào dependencies block
                    dep_match = re.search(r'dependencies\s*\{', content)
                    if dep_match:
                        brace_start = content.index('{', dep_match.start())
                        brace_count = 0
                        brace_end = -1
                        for i in range(brace_start, len(content)):
                            if content[i] == '{':
                                brace_count += 1
                            elif content[i] == '}':
                                brace_count -= 1
                                if brace_count == 0:
                                    brace_end = i
                                    break
                        if brace_end != -1:
                            content = content[:brace_end] + f"    {replacement}\n" + content[brace_end:]
        
        self.log(f"  ✔ Cập nhật dependencies versions")
        return content

    def _add_helper_functions_to_gradle(self, content: str) -> str:
        """4.9 - Thêm helper functions Groovy vào cuối file"""
        
        # Kiểm tra xem helper functions đã tồn tại chưa
        if 'getSHA256FromSigningConfig' in content and 'calculateSHA256' in content:
            self.log(f"  ℹ Helper functions đã tồn tại")
            return content
        
        helper_functions = '''

// === Các hàm helper chuyển nguyên bản sang Groovy ===
def getSHA256FromSigningConfig(SigningConfig signingConfig, String key, String iv) {
    if (signingConfig?.storeFile?.exists()) {
        def sha256 = calculateSHA256(signingConfig.storeFile, signingConfig.storePassword, signingConfig.keyAlias)
        return encrypt(sha256, key, iv)
    } else {
        throw new IllegalStateException("Keystore file not found")
    }
}

def calculateSHA256(File keystoreFile, String password, String alias) {
    if (!password || !alias) throw new IllegalArgumentException("Password and alias cannot be null")
    def keystore = KeyStore.getInstance(KeyStore.getDefaultType())
    keystoreFile.withInputStream { fis ->
        keystore.load(fis, password.toCharArray())
    }
    def certificate = keystore.getCertificate(alias)
    if (!certificate) throw new IllegalStateException("Certificate not found")
    def digest = MessageDigest.getInstance("SHA-256")
    def hash = digest.digest(certificate.encoded)
    return hash.collect { String.format("%02X", it) }.join(':')
}

def encrypt(String input, String key, String iv) {
    def ivParameterSpec = new IvParameterSpec(hexStringToByteArray(iv))
    def secretKeySpec = new SecretKeySpec(hexStringToByteArray(key), "AES")
    def cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
    cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec)
    def encrypted = cipher.doFinal(input.getBytes())
    return Base64.encoder.encodeToString(encrypted)
}

def hexStringToByteArray(String hex) {
    if (hex.length() % 2 != 0) throw new IllegalArgumentException("Hex string must have even length")
    def bytes = new byte[hex.length() / 2]
    for (int i = 0; i < hex.length(); i += 2) {
        bytes[(int) (i / 2)] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16)
    }
    return bytes
}
'''
        
        # Thêm helper functions vào cuối file
        if not content.endswith('\n'):
            content += '\n'
        content += helper_functions
        
        self.log(f"  ✔ Thêm helper functions (4 hàm)")
        return content

    def copy_modules(self):
        """10/ Copy modules B9Default, B9Ads, B9UI, B9Util vào project"""
        try:
            script_dir = Path(__file__).parent
            modules = ['B9Default', 'B9Ads', 'B9UI', 'B9Util']
            
            copied_count = 0
            for module in modules:
                src_module = script_dir / module
                dest_module = self.project_root / module
                
                if src_module.exists() and src_module.is_dir():
                    if dest_module.exists():
                        shutil.rmtree(dest_module)
                    shutil.copytree(src_module, dest_module)
                    self.log(f"  ✔ Copy module: {module}")
                    copied_count += 1
                else:
                    self.log(f"  ⚠ Không tìm thấy: {src_module}")
            
            if copied_count > 0:
                self.log(f"✔ Copy modules: {copied_count} module(s)")
                return True
            else:
                self.log(f"✘ Không copy được module nào")
                return False
        except Exception as e:
            self.log(f"✘ Lỗi copy modules: {e}")
            return False

    def update_settings_gradle(self):
        """11/ Cập nhật settings.gradle thêm include ':B9*'"""
        try:
            settings_gradle = self.project_root / "settings.gradle"
            if not settings_gradle.exists():
                self.log(f"✘ Không tìm thấy: {settings_gradle}")
                return False
            
            content = settings_gradle.read_text(encoding="utf-8")
            modules = ['B9UI', 'B9Default', 'B9Util', 'B9Ads']
            
            added_count = 0
            for module in modules:
                include_line = f"include ':{module}'"
                if include_line not in content:
                    content += f"\n{include_line}"
                    added_count += 1
            
            settings_gradle.write_text(content, encoding="utf-8")
            if added_count > 0:
                self.log(f"✔ Thêm {added_count} module(s) vào settings.gradle")
            else:
                self.log(f"ℹ Modules đã tồn tại trong settings.gradle")
            return True
        except Exception as e:
            self.log(f"✘ Lỗi cập nhật settings.gradle: {e}")
            return False

    def update_build_gradle_app_dependencies(self):
        """12/ Cập nhật build.gradle (App) - thêm module dependencies"""
        try:
            build_gradle = self.app_folder / "build.gradle"
            if not build_gradle.exists():
                self.log(f"✘ Không tìm thấy: {build_gradle}")
                return False
            
            content = build_gradle.read_text(encoding="utf-8")
            
            module_dependencies = [
                'implementation(project(":B9Ads"))',
                'implementation(project(":B9Util"))',
                'implementation(project(":B9Default"))',
                'implementation(project(":B9UI"))'
            ]
            
            # Tìm dependencies block
            dep_start = content.find('dependencies {')
            if dep_start == -1:
                self.log(f"⚠ Không tìm thấy dependencies block")
                return False
            
            # Tìm đóng ngoặc của dependencies block
            brace_count = 0
            dep_end = -1
            for i in range(dep_start, len(content)):
                if content[i] == '{':
                    brace_count += 1
                elif content[i] == '}':
                    brace_count -= 1
                    if brace_count == 0:
                        dep_end = i
                        break
            
            if dep_end == -1:
                self.log(f"⚠ Không tìm thấy kết thúc dependencies block")
                return False
            
            added_count = 0
            for dep in module_dependencies:
                if dep not in content:
                    # Thêm trước dấu đóng ngoặc cuối
                    insert_pos = dep_end
                    content = content[:insert_pos] + f"    {dep}\n" + content[insert_pos:]
                    added_count += 1
            
            build_gradle.write_text(content, encoding="utf-8")
            if added_count > 0:
                self.log(f"✔ Thêm {added_count} module dependency(ies)")
            else:
                self.log(f"ℹ Module dependencies đã tồn tại")
            return True
        except Exception as e:
            self.log(f"✘ Lỗi cập nhật module dependencies: {e}")
            return False
        
    # ─────────────────────────────────────────────────────────────────────────────
    # Nội dung activities + meta-data cần chèn vào <application>
    # ─────────────────────────────────────────────────────────────────────────────
    _B9DEFAULT_ACTIVITIES = '''\
            <activity
                android:name="com.b9default.ui.update.AppUpdateActivity"
                android:exported="true"
                android:screenOrientation="portrait" />
            <activity
                android:name="com.b9default.ui.welcomeback.WelcomeBackActivity"
                android:exported="true"
                android:screenOrientation="portrait" />
            <activity
                android:name="com.b9default.ui.test_native.TestNativeLayoutActivity"
                android:exported="true"
                android:screenOrientation="portrait" />
            <activity
                android:name="com.b9default.ui.no_internet.NoInternetActivity"
                android:exported="true"
                android:screenOrientation="portrait" />
            <activity
                android:name="com.b9default.ui.language.LanguageSettingActivity"
                android:exported="true"
                android:screenOrientation="portrait" />
            <activity
                android:name="com.b9default.ui.language.LanguageStartActivity"
                android:exported="true"
                android:screenOrientation="portrait" />
            <activity
                android:name="com.b9default.ui.intro.IntroActivity"
                android:exported="true"
                android:screenOrientation="portrait" />
            <activity
                android:name="com.b9default.ui.splash.SplashActivity"
                android:exported="true"
                android:screenOrientation="portrait"
                android:theme="@style/Theme.App.Starting">
                <intent-filter>
                    <action android:name="android.intent.action.MAIN" />
                    <category android:name="android.intent.category.LAUNCHER" />
                </intent-filter>
            </activity>
            <meta-data
                android:name="com.google.android.gms.ads.flag.OPTIMIZE_INITIALIZATION"
                android:value="true" />
            <meta-data
                android:name="com.google.android.gms.ads.flag.OPTIMIZE_AD_LOADING"
                android:value="true" />
            <!-- admob app id -->
            <meta-data
                android:name="com.google.android.gms.ads.APPLICATION_ID"
                android:value="@string/app_id_admob" />
            <!-- facebook ad -->
            <meta-data
                android:name="com.facebook.sdk.ApplicationId"
                android:value="@string/facebook_app_id" />
            <meta-data
                android:name="com.facebook.sdk.ClientToken"
                android:value="@string/facebook_client_token" />
            <meta-data
                android:name="com.facebook.sdk.AutoInitEnabled"
                android:value="true" />
            <meta-data
                android:name="com.facebook.sdk.AutoLogAppEventsEnabled"
                android:value="true" />
            <meta-data
                android:name="com.facebook.sdk.AdvertiserIDCollectionEnabled"
                android:value="true" />'''
    
    # intent-filter template cho PermissionActivity / MainActivity
    def _deep_link_filter(self, path_suffix: str) -> str:
        return f'''\
                <intent-filter>
                    <action android:name="android.intent.action.VIEW" />
                    <category android:name="android.intent.category.DEFAULT" />
                    <category android:name="android.intent.category.BROWSABLE" />
                    <data
                        android:host="app"
                        android:path="/{path_suffix}"
                        android:scheme="example" />
                </intent-filter>'''
    
    # Meta-data names cần dedup (xóa bản cũ trước khi thêm block mới)
    _DEDUP_META_NAMES = [
        "com.google.android.gms.ads.flag.OPTIMIZE_INITIALIZATION",
        "com.google.android.gms.ads.flag.OPTIMIZE_AD_LOADING",
        "com.google.android.gms.ads.APPLICATION_ID",
        "com.google.android.gms.version",
        "com.facebook.sdk.ApplicationId",
        "com.facebook.sdk.ClientToken",
        "com.facebook.sdk.AutoInitEnabled",
        "com.facebook.sdk.AutoLogAppEventsEnabled",
        "com.facebook.sdk.AdvertiserIDCollectionEnabled",
    ]
    
    
    # ─────────────────────────────────────────────────────────────────────────────
    # Helpers XML thuần text (không parse DOM để giữ format gốc)
    # ─────────────────────────────────────────────────────────────────────────────
    
    def _remove_selected_meta_data(self, content: str) -> str:
        """Chỉ xóa các meta-data có tên trong _DEDUP_META_NAMES."""
        app_match = re.search(r'<application\b', content)
        if not app_match:
            return content

        # Tìm '>' đóng thẻ mở <application ...>
        tag_close = content.find('>', app_match.end())
        if tag_close == -1:
            return content

        # Tìm </application>
        end_idx = content.find('</application>', tag_close)
        if end_idx == -1:
            return content

        end_tag_end = end_idx + len('</application>')
        app_block = content[app_match.start():end_tag_end]

        names = '|'.join(re.escape(name) for name in self._DEDUP_META_NAMES)
        pattern = re.compile(
            r'[ \t]*<meta-data\b'
            r'(?=[^>]*\bandroid:name\s*=\s*["\'](?:' + names + r')["\'])'
            r'[^>]*(?:/>|>.*?</meta-data\s*>)\s*',
            flags=re.DOTALL,
        )
        cleaned_block = pattern.sub('', app_block)

        new_content = content[:app_match.start()] + cleaned_block + content[end_tag_end:]
        new_content = re.sub(r'\n{3,}', '\n\n', new_content)
        return new_content
    
    
    def _remove_intent_filter_in_activity(self, content: str, activity_short_name: str) -> str:
        """
        Xóa <intent-filter> bên trong thẻ <activity> nào đó có android:name
        kết thúc bằng activity_short_name (vd: ".SplashActivity" hoặc "SplashActivity").
        Chỉ xóa intent-filter đầu tiên tìm thấy trong activity đó.
        """
        # Tìm vị trí mở thẻ <activity ... android:name="...SplashActivity"...>
        act_pattern = re.compile(
            r'<activity\b[^>]*android:name\s*=\s*"[^"]*' +
            re.escape(activity_short_name) +
            r'"',
            re.DOTALL
        )
        m = act_pattern.search(content)
        if not m:
            return content
    
        # Tìm đóng thẻ </activity> tương ứng từ vị trí đó
        act_start = m.start()
        # Tìm '>' đóng thẻ mở <activity ...>
        tag_close = content.find('>', m.end())
        if tag_close == -1:
            return content
    
        # Nếu self-closing <activity ... /> thì không có intent-filter
        if content[tag_close - 1] == '/':
            return content
    
        # Tìm </activity> từ sau thẻ mở
        act_end_match = re.search(r'</activity\s*>', content[tag_close:])
        if not act_end_match:
            return content
    
        act_end = tag_close + act_end_match.end()
        activity_block = content[act_start:act_end]
    
        # Xóa <intent-filter>...</intent-filter> bên trong activity block
        cleaned_block = re.sub(
            r'[ \t]*<intent-filter\b.*?</intent-filter\s*>\s*',
            '',
            activity_block,
            count=1,
            flags=re.DOTALL
        )
    
        return content[:act_start] + cleaned_block + content[act_end:]
    
    
    def _repair_malformed_activity_intent_filters(self, content: str) -> tuple[str, int]:
        """Sửa các trường hợp <activity ... /> bị nối tiếp bởi <intent-filter>...</activity>."""
        pattern = re.compile(
            r'(<activity\b[^>]*?)\s*/>\s*(<intent-filter\b.*?</intent-filter\s*>)\s*</activity\s*>',
            re.DOTALL
        )

        def replace(match: re.Match) -> str:
            open_tag = match.group(1)
            intent_block = match.group(2)
            return f"{open_tag}>\n{intent_block}\n</activity>"

        new_content, count = pattern.subn(replace, content)
        return new_content, count

    def _add_intent_filter_to_activity(self, content: str, activity_short_name: str,
                                    filter_xml: str) -> tuple[str, bool]:
        """
        Thêm intent-filter vào cuối thẻ <activity> có android:name kết thúc bằng
        activity_short_name.  Trả về (new_content, added).
        """
        act_pattern = re.compile(
            r'<activity\b[^>]*android:name\s*=\s*"[^"]*' +
            re.escape(activity_short_name) +
            r'"',
            re.DOTALL
        )
        m = act_pattern.search(content)
        if not m:
            return content, False
    
        tag_close = content.find('>', m.end())
        if tag_close == -1:
            return content, False

        # Ensure the activity opening tag has android:exported="true"
        open_start = m.start()
        open_tag = content[open_start:tag_close+1]
        orig_self_closing = open_tag.rstrip().endswith('/>')
        open_tag_new = open_tag
        if orig_self_closing:
            open_tag_new = re.sub(r'/>(\s*)$', r'>\1', open_tag)
            if 'android:exported' not in open_tag_new:
                open_tag_new = open_tag_new[:-1] + ' android:exported="true">'
        elif 'android:exported' in open_tag:
            open_tag_new = re.sub(r'android:exported\s*=\s*["\']\s*false\s*["\']', 'android:exported="true"', open_tag)
        else:
            open_tag_new = open_tag[:-1] + ' android:exported="true">'

        if open_tag_new != open_tag:
            content = content[:open_start] + open_tag_new + content[tag_close+1:]
            # adjust tag_close to point to the new '>'
            tag_close = open_start + len(open_tag_new) - 1

        # If the original tag was self-closing, we've converted it to non-self-closing
        # and must immediately insert the filter + closing tag.
        if orig_self_closing:
            insert_pos = open_start + len(open_tag_new)
            content = content[:insert_pos] + '\n' + filter_xml + '\n        </activity>' + content[insert_pos:]
            return content, True
    
        # Với activity đã mở bằng '>', giữ nguyên thẻ mở và chỉ chèn filter
        # trước thẻ đóng tương ứng.
        act_end_match = re.search(r'([ \t]*</activity\s*>)', content[tag_close:])
        if not act_end_match:
            return content, False
    
        insert_pos = tag_close + act_end_match.start()
        return content[:insert_pos] + '\n' + filter_xml + '\n' + content[insert_pos:], True
    
    
    def _find_activity_name(self, content: str, candidates: list[str]) -> str | None:
        """Tìm android:name của activity khớp với bất kỳ tên nào trong candidates."""
        for name in candidates:
            if re.search(
                r'<activity\b[^>]*android:name\s*=\s*"[^"]*' + re.escape(name) + r'"',
                content
            ):
                return name
        return None
    
    
    def _insert_before_application_close(self, content: str, xml_block: str) -> str:
        """Chèn xml_block ngay trước thẻ đóng </application>."""
        pos = content.rfind('</application>')
        if pos == -1:
            return content
        return content[:pos] + xml_block + '\n' + content[pos:]
    
    # ─────────────────────────────────────────────────────────────────────────────
    # Method chính – dán vào AndroidSetupHelper
    # ─────────────────────────────────────────────────────────────────────────────
    
    def update_android_manifest(
        self,
        permission_activity_name: str | None = None,
        main_activity_name: str | None = None,
    ):
        """
        14/ Cập nhật AndroidManifest.xml:
        14.1 – Thêm B9Default activities + meta-data vào <application>
        14.2 – Xóa intent-filter MAIN/LAUNCHER khỏi SplashActivity gốc
        14.3 – Thêm deep-link intent-filter cho PermissionActivity
        14.4 – Thêm deep-link intent-filter cho MainActivity
    
        Args:
            permission_activity_name: tên ngắn class (vd "PermissionActivity").
                None → tự detect trong manifest, fallback "PermissionActivity".
            main_activity_name: tên ngắn class (vd "MainActivity" / "HomeActivity").
                None → tự detect, fallback "MainActivity".
        """
        manifest_path = self.app_folder / "src" / "main" / "AndroidManifest.xml"
        if not manifest_path.exists():
            self.log(f"✘ Không tìm thấy: {manifest_path}")
            return False
    
        try:
            content = manifest_path.read_text(encoding="utf-8")

            content, repaired = self._repair_malformed_activity_intent_filters(content)
            if repaired:
                self.log(f"  ✔ Sửa {repaired} activity malformed với intent-filter")

            # Chỉ xóa các meta-data được liệt kê; các meta-data khác phải giữ nguyên.
            content_before_cleanup = content
            content = self._remove_selected_meta_data(content)
            removed_meta = content_before_cleanup != content
            if removed_meta:
                self.log("  ✔ Xóa meta-data đã chọn (sẽ thêm lại)")
    
            # Kiểm tra xem B9Default SplashActivity đã có chưa
            b9_splash_present = 'com.b9default.ui.splash.SplashActivity' in content
    
            if not b9_splash_present:
                content = self._insert_before_application_close(content, self._B9DEFAULT_ACTIVITIES)
                self.log("  ✔ Thêm B9Default activities + meta-data")
            else:
                # Vẫn cần thêm lại meta-data vừa dedup
                # Chỉ thêm phần meta-data (không thêm activities lại)
                meta_only = '\n'.join(
                    line for line in self._B9DEFAULT_ACTIVITIES.splitlines()
                    if '<meta-data' in line or 'android:name=' in line
                    or 'android:value=' in line or '<!--' in line
                )
                # Đơn giản hơn: thêm nguyên block rồi dedup activities sau
                # → Chỉ thêm meta-data block
                meta_block = """\
            <meta-data
                android:name="com.google.android.gms.ads.flag.OPTIMIZE_INITIALIZATION"
                android:value="true" />
            <meta-data
                android:name="com.google.android.gms.ads.flag.OPTIMIZE_AD_LOADING"
                android:value="true" />
            <!-- admob app id -->
            <meta-data
                android:name="com.google.android.gms.ads.APPLICATION_ID"
                android:value="@string/app_id_admob" />
            <!-- facebook ad -->
            <meta-data
                android:name="com.facebook.sdk.ApplicationId"
                android:value="@string/facebook_app_id" />
            <meta-data
                android:name="com.facebook.sdk.ClientToken"
                android:value="@string/facebook_client_token" />
            <meta-data
                android:name="com.facebook.sdk.AutoInitEnabled"
                android:value="true" />
            <meta-data
                android:name="com.facebook.sdk.AutoLogAppEventsEnabled"
                android:value="true" />
            <meta-data
                android:name="com.facebook.sdk.AdvertiserIDCollectionEnabled"
                android:value="true" />"""
                content = self._insert_before_application_close(content, meta_block)
                self.log("  ✔ Thêm lại meta-data (activities đã tồn tại)")
    
            # ── 14.2  Xóa intent-filter MAIN/LAUNCHER của SplashActivity gốc ─
            # "SplashActivity gốc" = bất kỳ activity nào (không phải b9default)
            # có MAIN/LAUNCHER và tên ngắn là SplashActivity
            # Cách đơn giản: xóa intent-filter chứa action.MAIN trong activity
            # có android:name KHÔNG phải com.b9default
            def remove_main_launcher_from_non_b9(text: str) -> str:
                def _strip_main_filter(m: re.Match) -> str:
                    block = m.group(0)
                    if 'com.b9default' in block:
                        return block  # giữ nguyên b9default
                    # Xóa intent-filter chứa action.MAIN
                    cleaned = re.sub(
                        r'[ \t]*<intent-filter\b[^>]*>.*?android\.intent\.action\.MAIN.*?</intent-filter\s*>\s*',
                        '',
                        block,
                        flags=re.DOTALL
                    )
                    return cleaned
    
                return re.sub(
                    r'<activity\b.*?</activity\s*>',
                    _strip_main_filter,
                    text,
                    flags=re.DOTALL
                )
    
            content_before = content
            content = remove_main_launcher_from_non_b9(content)
            if content != content_before:
                self.log("  ✔ Xóa intent-filter MAIN/LAUNCHER khỏi SplashActivity gốc")
            else:
                self.log("  ℹ Không tìm thấy MAIN/LAUNCHER SplashActivity gốc để xóa")
    
            # ── 14.3  Deep-link cho PermissionActivity ────────────────────────
            perm_candidates = ['PermissionActivity']
            perm_name = permission_activity_name or self._find_activity_name(content, perm_candidates) or 'PermissionActivity'
            perm_filter = self._deep_link_filter('PermissionActivity')
            # Kiểm tra xem deep-link /PermissionActivity đã có chưa
            if '/PermissionActivity' not in content:
                content, added = self._add_intent_filter_to_activity(content, perm_name, perm_filter)
                if added:
                    self.log(f"  ✔ Thêm deep-link intent-filter cho {perm_name}")
                else:
                    self.log(f"  ⚠ Không tìm thấy activity '{perm_name}' để thêm intent-filter")
            else:
                self.log(f"  ℹ Deep-link /PermissionActivity đã tồn tại")
    
            # ── 14.4  Deep-link cho MainActivity ─────────────────────────────
            main_candidates = ['MainActivity', 'HomeActivity']
            main_name = main_activity_name or self._find_activity_name(content, main_candidates) or 'MainActivity'
            main_filter = self._deep_link_filter('HomeActivity')
            if '/HomeActivity' not in content:
                content, added = self._add_intent_filter_to_activity(content, main_name, main_filter)
                if added:
                    self.log(f"  ✔ Thêm deep-link intent-filter cho {main_name}")
                else:
                    self.log(f"  ⚠ Không tìm thấy activity '{main_name}' để thêm intent-filter")
            else:
                self.log(f"  ℹ Deep-link /HomeActivity đã tồn tại")
    
            manifest_path.write_text(content, encoding="utf-8")
            self.log(f"✔ Cập nhật: AndroidManifest.xml")
            return True
    
        except Exception as e:
            self.log(f"✘ Lỗi cập nhật AndroidManifest.xml: {e}")
            return False