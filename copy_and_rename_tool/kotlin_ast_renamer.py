from __future__ import annotations

import base64
from dataclasses import dataclass
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile


@dataclass
class AstRenameStats:
    variables: int = 0
    functions: int = 0
    files: int = 0


def _asset_path(*parts: str) -> Path:
    root = Path(getattr(sys, "_MEIPASS", Path(__file__).resolve().parent))
    return root.joinpath(*parts)


def _compiler_homes() -> list[Path]:
    result: list[Path] = []
    for env_name in ("KOTLIN_HOME", "ANDROID_STUDIO_HOME"):
        value = os.environ.get(env_name)
        if value:
            base = Path(value)
            result.extend((base, base / "plugins" / "Kotlin" / "kotlinc"))
    result.extend(
        (
            Path(r"C:\Program Files\Android\Android Studio\plugins\Kotlin\kotlinc"),
            Path(r"C:\Program Files\JetBrains\IntelliJ IDEA\plugins\Kotlin\kotlinc"),
            Path(r"C:\Program Files\JetBrains\IntelliJ IDEA 2024.3.5\plugins\Kotlin\kotlinc"),
        )
    )
    executable = shutil.which("kotlinc") or shutil.which("kotlinc.bat")
    if executable:
        result.append(Path(executable).resolve().parent.parent)
    return list(dict.fromkeys(path for path in result if path.is_dir()))


def _runtime() -> tuple[Path, list[Path]]:
    for home in _compiler_homes():
        compiler = home / "lib" / "kotlin-compiler.jar"
        stdlib = home / "lib" / "kotlin-stdlib.jar"
        if not compiler.is_file() or not stdlib.is_file():
            continue
        java_candidates = (
            home.parents[2] / "jbr" / "bin" / "java.exe",
            Path(shutil.which("java") or ""),
        )
        java = next((item for item in java_candidates if item.is_file()), None)
        if java is not None:
            jars = [compiler, stdlib]
            reflect = home / "lib" / "kotlin-reflect.jar"
            if reflect.is_file():
                jars.append(reflect)
            return java, jars
    raise RuntimeError("Kotlin compiler not found. Install Android Studio or set KOTLIN_HOME.")


def _engine_jar() -> Path:
    jar = _asset_path("kotlin-symbol-engine", "symbol-engine.jar")
    if not jar.is_file():
        raise RuntimeError(f"Kotlin symbol engine is missing: {jar}")
    return jar


def _project_classpath(project_root: Path | None) -> list[Path]:
    """Use already-resolved Gradle artifacts when available; never download here."""
    if project_root is None:
        return []
    roots = [project_root / ".gradle", project_root / "build"]
    jars: list[Path] = []
    for root in roots:
        if root.is_dir():
            jars.extend(root.rglob("*.jar"))
    return jars


def rename_kotlin_locals_and_private_members(
    folder: Path,
    suffix: str,
    package_name: str,
    project_root: Path | None = None,
) -> AstRenameStats:
    """Rename only references whose compiler descriptor equals the declaration."""
    java, compiler_jars = _runtime()
    engine = _engine_jar()
    with tempfile.NamedTemporaryFile("w", encoding="utf-8", delete=False) as stream:
        classpath_file = Path(stream.name)
        for item in _project_classpath(project_root):
            stream.write(str(item) + "\n")
    try:
        process = subprocess.run(
            [
                str(java), "-Xmx2g", "-cp",
                os.pathsep.join(str(item) for item in [engine, *compiler_jars]),
                "toolandroid.symbols.SymbolRenameEngineKt", str(folder.resolve()), suffix,
                str(classpath_file), package_name,
            ],
            capture_output=True, text=True, encoding="utf-8", errors="replace", timeout=300,
        )
    finally:
        classpath_file.unlink(missing_ok=True)
    if process.returncode != 0:
        detail = (process.stderr or process.stdout).strip()
        raise RuntimeError(f"Kotlin symbol analysis failed: {detail}")

    edits_by_file: dict[Path, list[tuple[int, int, bytes, str, bool]]] = {}
    for line in process.stdout.splitlines():
        fields = line.split("\t")
        if len(fields) != 6:
            continue
        path64, start, end, replacement64, kind, declaration = fields
        path = Path(base64.b64decode(path64).decode("utf-8"))
        replacement = base64.b64decode(replacement64)
        edits_by_file.setdefault(path, []).append(
            (int(start), int(end), replacement, kind, declaration == "true")
        )

    variable_edits = function_edits = 0
    for path, edits in edits_by_file.items():
        text = path.read_text(encoding="utf-8").replace("\r\n", "\n").replace("\r", "\n")
        source = bytearray(text.encode("utf-8"))
        # IntelliJ strips the UTF-8 BOM before assigning PSI offsets while
        # Python exposes it as one character. Compensate without dropping it.
        psi_offset = 1 if text.startswith("\ufeff") else 0
        byte_edits = []
        for start, end, replacement, kind, declaration in edits:
            # IntelliJ PSI offsets use UTF-16 code units, not UTF-8 byte offsets.
            prefix = text.encode("utf-16-le")
            char_start = len(prefix[: start * 2].decode("utf-16-le", errors="ignore"))
            char_end = len(prefix[: end * 2].decode("utf-16-le", errors="ignore"))
            char_start += psi_offset
            char_end += psi_offset
            byte_edits.append((len(text[:char_start].encode()), len(text[:char_end].encode()), replacement, kind, declaration))
        for start, end, replacement, kind, declaration in sorted(byte_edits, reverse=True):
            source[start:end] = replacement
            variable_edits += kind == "variable" and declaration
            function_edits += kind == "function" and declaration
        path.write_bytes(source)
    return AstRenameStats(variable_edits, function_edits, len(edits_by_file))
