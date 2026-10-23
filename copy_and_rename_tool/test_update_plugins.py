from pathlib import Path
from android_setup import AndroidSetupHelper

proj = Path(__file__).parent / "tmp_test_plugins"
proj.mkdir(parents=True, exist_ok=True)
build = proj / "build.gradle"
build_content = """plugins {
    id 'kotlin-android'
    id("com.google.devtools.ksp") version "1.0.0"
}
"""
build.write_text(build_content, encoding='utf-8')

helper = AndroidSetupHelper(proj, print)
# call the method that updates app build.gradle comprehensive but it operates on app/build.gradle
# We'll instead directly import and reuse the plugin update logic by copying minimal code here

# Simulate reading a build.gradle (project) content and applying plugin update logic
content = build.read_text(encoding='utf-8')
plugin_ids = {
    'id("com.google.devtools.ksp") version "2.1.0-1.0.29"': "com.google.devtools.ksp",
    "id 'com.android.application'":         "com.android.application",
    "id 'kotlin-android'":                  "kotlin-android",
    "id 'kotlin-parcelize'":                "kotlin-parcelize",
    "id 'com.google.gms.google-services'":  "com.google.gms.google-services",
    "id 'com.google.firebase.crashlytics'": "com.google.firebase.crashlytics",
}

import re
plugins_match = re.search(r'plugins\s*\{', content)
if plugins_match:
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

    inner = content[brace_start+1:brace_end]
    inner_new = inner
    for plugin_line, plugin_id in plugin_ids.items():
        esc = re.escape(plugin_id)
        pat = rf'id\s*[\(\s][\"\']{esc}[\"\'][\)]?(?:\s*version\s*[\"\'][^\"\']+[\"\'])?'
        if re.search(pat, inner_new):
            inner_new = re.sub(pat, plugin_line, inner_new)
        else:
            inner_new = inner_new + '\\n    ' + plugin_line

    content = content[:brace_start+1] + inner_new + content[brace_end:]

print('--- Updated build.gradle content ---')
print(content)
