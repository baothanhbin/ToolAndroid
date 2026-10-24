from __future__ import annotations

from pathlib import Path
import os
import re
import subprocess


SKIP_PARTS = {".git", ".gradle", ".idea", ".kotlin", "build", "out", "captures"}
TRACKED_SUFFIXES = {".kt", ".java", ".xml"}


def _java_major_version(java_home: str | None) -> int | None:
    """Return the configured JDK major version, or None if it cannot be read."""
    if not java_home:
        return None
    java = Path(java_home) / "bin" / ("java.exe" if os.name == "nt" else "java")
    if not java.is_file():
        return None
    try:
        result = subprocess.run(
            [str(java), "-version"],
            capture_output=True,
            text=True,
            encoding="utf-8",
            errors="replace",
            timeout=10,
        )
    except (OSError, subprocess.SubprocessError):
        return None
    version_output = f"{result.stdout}\n{result.stderr}"
    match = re.search(r'version "(?:1\.)?(\d+)', version_output)
    return int(match.group(1)) if match else None


def _select_gradle_java_home(environment: dict[str, str]) -> None:
    """Use a Java 17+ JDK for AGP, even when a stale JAVA_HOME is exported."""
    configured = environment.get("JAVA_HOME")
    configured_version = _java_major_version(configured)
    if configured_version is not None and configured_version >= 17:
        return

    candidates = [
        Path(r"C:\Program Files\Android\Android Studio\jbr"),
        Path(r"C:\Program Files\Android\Android Studio\jre"),
    ]
    for candidate in candidates:
        candidate_version = _java_major_version(str(candidate))
        if candidate_version is not None and candidate_version >= 17:
            environment["JAVA_HOME"] = str(candidate)
            return


def find_gradle_project_root(folder: Path) -> Path:
    current = folder.resolve()
    for candidate in (current, *current.parents):
        if (candidate / "gradlew.bat").is_file() or (candidate / "gradlew").is_file():
            return candidate
    raise RuntimeError("Gradle wrapper not found above the selected folder.")


class RenameTransaction:
    def __init__(self, folder: Path):
        self.folder = folder.resolve()
        self.originals = {
            path.resolve(): path.read_bytes()
            for path in self.folder.rglob("*")
            if path.is_file()
            and path.suffix.lower() in TRACKED_SUFFIXES
            and not any(part in SKIP_PARTS for part in path.relative_to(self.folder).parts)
        }
        self._done = False

    def rollback(self) -> None:
        if self._done:
            return
        current = {
            path.resolve()
            for path in self.folder.rglob("*")
            if path.is_file()
            and path.suffix.lower() in TRACKED_SUFFIXES
            and not any(part in SKIP_PARTS for part in path.relative_to(self.folder).parts)
        }
        for path in current - self.originals.keys():
            path.unlink()
        for path, content in self.originals.items():
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_bytes(content)
        self._done = True

    def commit(self) -> None:
        self._done = True


def run_verification_build(project_root: Path, timeout: int = 900) -> subprocess.CompletedProcess[str]:
    wrapper = project_root / ("gradlew.bat" if (project_root / "gradlew.bat").is_file() else "gradlew")
    command = [str(wrapper), "clean", "assembleDebug", "--stacktrace"]
    environment = os.environ.copy()
    _select_gradle_java_home(environment)
    process = subprocess.run(
        command,
        cwd=project_root,
        env=environment,
        capture_output=True,
        text=True,
        encoding="utf-8",
        errors="replace",
        timeout=timeout,
    )
    if process.returncode != 0:
        output = "\n".join(part for part in (process.stdout, process.stderr) if part).strip()
        lines = output.splitlines()
        compiler_errors = [
            line for line in lines
            if line.lstrip().startswith(("e: file:", "error:", "AAPT:", "ERROR:"))
        ]
        detail = "\n".join(compiler_errors[-80:] or lines[-80:])
        raise RuntimeError(f"Gradle verification failed (exit {process.returncode}):\n{detail}")
    return process
