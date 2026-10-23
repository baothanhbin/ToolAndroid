import xml.etree.ElementTree as ET
from pathlib import Path
from android_setup import AndroidSetupHelper

proj = Path(__file__).parent / "tmp_test_proj"
app_dir = proj / "app" / "src" / "main"
app_dir.mkdir(parents=True, exist_ok=True)

manifest = '''<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="com.example">
<application>
    <meta-data android:name="custom.metadata" android:value="keep" />
    <meta-data android:name="com.google.android.gms.ads.APPLICATION_ID" android:value="old" />
    <meta-data android:name="com.google.android.gms.version" android:value="@integer/google_play_services_version" />
    <activity
        android:name=".ui.permission.PermissionActivity"
        android:exported="true"
        android:screenOrientation="portrait">
    </activity>
    <activity
        android:name=".ui.main.MainActivity"
        android:exported="true"
        android:screenOrientation="portrait" />
</application>
</manifest>'''

(manifest_path := app_dir / "AndroidManifest.xml").write_text(manifest, encoding='utf-8')

helper = AndroidSetupHelper(proj, print)
helper.update_android_manifest()
result = manifest_path.read_text(encoding='utf-8')
assert 'android:name="custom.metadata"' in result
assert 'android:value="old"' not in result
assert 'android:name="com.google.android.gms.version"' not in result
ET.parse(manifest_path)
assert '<activity\n        android:name=".ui.permission.PermissionActivity"' in result
assert '<activity\n        android:name=".ui.main.MainActivity"' in result
print('metadata whitelist check passed')
