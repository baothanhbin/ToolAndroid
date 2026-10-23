# -*- mode: python ; coding: utf-8 -*-


a = Analysis(
    ['main.py'],
    pathex=[],
    binaries=[],
    datas=[('.\\B9Ads', 'B9Ads'), ('.\\B9Default', 'B9Default'), ('.\\B9UI', 'B9UI'), ('.\\B9Util', 'B9Util'), ('.\\for_dev_', 'for_dev_'), ('.\\ignore.txt', '.'), ('.\\kotlin-symbol-engine\\symbol-engine.jar', 'kotlin-symbol-engine')],
    hiddenimports=[],
    hookspath=[],
    hooksconfig={},
    runtime_hooks=[],
    excludes=[],
    noarchive=False,
    optimize=0,
)
pyz = PYZ(a.pure)

exe = EXE(
    pyz,
    a.scripts,
    a.binaries,
    a.datas,
    [],
    name='ToolAndroid',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    upx_exclude=[],
    runtime_tmpdir=None,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
)
