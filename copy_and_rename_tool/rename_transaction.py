from __future__ import annotations

from pathlib import Path
import os
import subprocess


SKIP_PARTS = {".git", ".gradle", ".idea", ".kotlin", "build", "out", "captures"}
TRACKED_SUFFIXES = {".kt", ".java", ".xml"}


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
    if not environment.get("JAVA_HOME"):
        bundled_jbr = Path(r"C:\Program Files\Android\Android Studio\jbr")
        if (bundled_jbr / "bin" / "java.exe").is_file():
            environment["JAVA_HOME"] = str(bundled_jbr)
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
