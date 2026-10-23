from __future__ import annotations

import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Dict, Iterable, Pattern, Set


# Never inspect or mutate generated/build metadata. These directories may
# contain stale generated Kotlin/Java/XML files and renaming them makes the
# next Gradle build inconsistent.
SKIP_DIRECTORIES = {
    ".git",
    ".gradle",
    ".idea",
    ".kotlin",
    "build",
    "out",
    "captures",
}

KOTLIN_SYNTAX_NAMES = {
    "init", "get", "set", "field", "property", "receiver", "param",
    "setparam", "delegate", "file", "field", "it", "this", "super",
}


@dataclass
class RenameCandidates:
    classes: Set[str]
    vars: Set[str]
    functions: Set[str]
    var_type_map: Dict[str, str] = field(default_factory=dict)
    xml_binding_map: Dict[str, str] = field(default_factory=dict)  # oldBindingFieldName -> newBindingFieldName


def scan_folder_for_rename_candidates(
    name_package: str,
    folder: Path,
    class_pattern: Pattern[str],
    object_var_pattern: Pattern[str],
    fun_pattern: Pattern[str],
    ignore_names: Set[str],
) -> RenameCandidates:
    all_classes: Set[str] = set()
    all_vars: Set[str] = set()
    all_functions: Set[str] = set()
    all_var_type_map: Dict[str, str] = {}

    for file in folder.iterdir():
        if file.is_dir():
            if file.name in SKIP_DIRECTORIES:
                continue
            nested = scan_folder_for_rename_candidates(
                name_package,
                file,
                class_pattern,
                object_var_pattern,
                fun_pattern,
                ignore_names,
            )
            all_classes.update(nested.classes)
            all_vars.update(nested.vars)
            all_functions.update(nested.functions)
            all_var_type_map.update(nested.var_type_map)
        elif file.suffix in (".kt", ".java"):
            content = file.read_text(encoding="utf-8")
            file_base = file.stem
            is_kotlin = file.suffix == ".kt"
            is_java = file.suffix == ".java"
            lines = content.splitlines()

            if is_kotlin:
                for match in class_pattern.finditer(content):
                    n = match.group(1)
                    if n not in ignore_names:
                        all_classes.add(n)

                kotlin_var_val_pattern = re.compile(
                    r"^\s*(?:(?:private|public|protected|internal|override|open|lateinit|const)\s+)*(?:var|val)\s+(\w+)\s*(?::\s*([\w<>\[\]?.]+))?",
                    re.MULTILINE,
                )
                for match in kotlin_var_val_pattern.finditer(content):
                    var_name = match.group(1)
                    declared_type = match.group(2) or ""
                    if var_name not in ignore_names:
                        all_vars.add(var_name)
                        if declared_type:
                            all_var_type_map[var_name] = declared_type.split(".")[-1]

                for match in fun_pattern.finditer(content):
                    n = match.group(1)
                    if n in all_classes:
                        continue
                    line_idx = next((i for i, line in enumerate(lines) if n in line), -1)
                    line = lines[line_idx] if line_idx != -1 else ""
                    is_override = (
                        line.lstrip().startswith("override ")
                        or re.search(r"\boverride\b", line) is not None
                    )
                    if not is_override and n != file_base and n not in ignore_names:
                        all_functions.add(n)

            if is_java:
                for match in class_pattern.finditer(content):
                    n = match.group(1)
                    if n not in ignore_names:
                        all_classes.add(n)

                java_instance_field_pattern = re.compile(
                    r"^\s*(?:private|public|protected)\s+(?:static\s+)?(?:final\s+)?([\w<>\[\]]+)\s+(\w+)\s*[;=]",
                    re.MULTILINE,
                )
                java_package_field_pattern = re.compile(
                    r"^    (?:static\s+)?(?:final\s+)?([\w<>\[\]]+)\s+(\w+)\s*[;=]",
                    re.MULTILINE,
                )
                java_static_field_pattern = re.compile(
                    r"^\s*(?:public|private|protected)?\s+static\s+(?:final\s+)?([\w<>\[\]]+)\s+(\w+)\s*=",
                    re.MULTILINE,
                )
                java_method_decl_pattern = re.compile(
                    r"^\s*(?:(?:public|private|protected)\s+)?(?:static\s+)?(?:final\s+)?(?:[\w<>\[\]]+)\s+(\w+)\s*\([^)]*\)\s*(?:throws\s+[\w,\s]+)?\s*\{",
                    re.MULTILINE,
                )

                for match in java_instance_field_pattern.finditer(content):
                    field_type = match.group(1)
                    field_name = match.group(2)
                    if field_type in {"return", "if", "for", "while", "new", "throw", "else"}:
                        continue
                    if field_name != file_base and field_name not in ignore_names:
                        if len(field_name) > 2:
                            all_vars.add(field_name)
                        all_var_type_map[field_name] = field_type

                for match in java_package_field_pattern.finditer(content):
                    field_type = match.group(1)
                    field_name = match.group(2)
                    java_keywords = {
                        "return", "if", "for", "while", "new", "throw", "else", "case",
                        "break", "continue", "try", "catch", "finally", "switch", "do",
                        "int", "float", "double", "long", "short", "byte", "char", "boolean", "void"
                    }
                    if field_type in java_keywords or field_name in java_keywords:
                        continue
                    if field_name != file_base and field_name not in ignore_names:
                        all_vars.add(field_name)
                        all_var_type_map[field_name] = field_type

                for match in java_static_field_pattern.finditer(content):
                    field_type = match.group(1)
                    field_name = match.group(2)
                    if field_type in {"return", "if", "for", "while", "new", "throw", "else"}:
                        continue
                    if field_name != file_base and field_name not in ignore_names:
                        all_vars.add(field_name)
                        all_var_type_map[field_name] = field_type

                for match in java_method_decl_pattern.finditer(content):
                    n = match.group(1)
                    if n in all_classes:
                        continue
                    if n == file_base or n in ignore_names:
                        continue
                    before_match = content[max(0, match.start() - 100): match.start()]
                    is_override = "@Override" in before_match
                    if not is_override:
                        all_functions.add(n)

    return RenameCandidates(
        classes=all_classes,
        vars=all_vars,
        functions=all_functions,
        var_type_map=all_var_type_map,
    )


def xml_id_to_binding_field(id_value: str) -> str:
    parts = id_value.split("_")
    return "".join(part if index == 0 else part[0].upper() + part[1:] for index, part in enumerate(parts))


def scan_xml_folder_for_binding_map(xml_folder: Path, name_suffix: str) -> Dict[str, str]:
    result: Dict[str, str] = {}
    xml_id_pattern = re.compile(r"@\+?id/(\w+)")

    def scan_dir(dir_path: Path):
        for file in dir_path.iterdir():
            if file.is_dir():
                scan_dir(file)
            elif file.suffix == ".xml":
                content = file.read_text(encoding="utf-8")
                for match in xml_id_pattern.finditer(content):
                    id_name = match.group(1)
                    if not id_name.endswith(name_suffix):
                        old_binding = xml_id_to_binding_field(id_name)
                        new_id_name = id_name + name_suffix
                        new_binding = xml_id_to_binding_field(new_id_name)
                        if old_binding != new_binding:
                            result[old_binding] = new_binding

    scan_dir(xml_folder)
    return result


def is_inside_string_literal(text: str, pos: int) -> bool:
    in_string = False
    interpolation_depth = 0
    i = 0
    while i < pos:
        c = text[i]
        if c == "\\":
            i += 2
            continue
        if in_string and interpolation_depth == 0 and c == "$":
            if i + 1 < len(text) and text[i + 1] == "{":
                interpolation_depth += 1
                i += 2
                continue
            i += 1
            continue
        if in_string and interpolation_depth > 0:
            if c == "{":
                interpolation_depth += 1
            elif c == "}":
                interpolation_depth -= 1
        elif c == '"':
            in_string = not in_string
        i += 1
    return in_string and interpolation_depth == 0


def normalize_type(type_name: str, name_suffix: str) -> str:
    return (
        type_name.replace(name_suffix, "")
        .removesuffix("?")
        .split("<")[0]
    )


def rename_folder_using_candidates(
    folder: Path,
    candidates: RenameCandidates,
    name_suffix: str,
    name_package: str,
):
    for file in folder.iterdir():
        if file.is_dir():
            if file.name in SKIP_DIRECTORIES:
                continue
            rename_folder_using_candidates(file, candidates, name_suffix, name_package)
        elif file.suffix in (".kt", ".java"):
            content = file.read_text(encoding="utf-8")
            lines = content.splitlines()
            header_lines = [line for line in lines if line.lstrip().startswith("import ") or line.lstrip().startswith("package ")]
            code_lines = [line for line in lines if not (line.lstrip().startswith("import ") or line.lstrip().startswith("package "))]
            code_content = "\n".join(code_lines)
            file_package_match = re.search(r"^\s*package\s+([\w.]+)", content, re.MULTILINE)
            file_package = file_package_match.group(1) if file_package_match else ""
            is_target_package = (
                file_package == name_package or file_package.startswith(name_package + ".")
            )
            # Variables/functions are scoped to the selected package. Classes
            # still remain global so imports and cross-package references can
            # be updated, but helper modules (for example security/) are not
            # rewritten accidentally.
            vars_to_rename = candidates.vars if is_target_package else set()
            functions_to_rename = candidates.functions if is_target_package else set()

            def replace_candidate(name: str, is_function: bool = False) -> str:
                nonlocal code_content
                pattern = re.compile(rf"\b{name}\b")

                def repl(match: re.Match[str]) -> str:
                    start = match.start()
                    if is_function and name in KOTLIN_SYNTAX_NAMES:
                        return match.group(0)
                    if is_inside_string_literal(code_content, start):
                        return match.group(0)

                    if is_function:
                        before_200 = code_content[max(0, start - 200):start]
                        current_line_start = before_200.rfind("\n") + 1
                        current_line = before_200[current_line_start:]
                        prev_line_end = current_line_start - 1
                        prev_line_start = (
                            before_200.rfind("\n", 0, prev_line_end) + 1 if prev_line_end > 0 else 0
                        )
                        prev_line = (
                            before_200[prev_line_start:prev_line_end].strip()
                            if prev_line_end > 0 else ""
                        )
                        is_override_decl = (
                            (prev_line == "@Override" or "@Override" in current_line)
                            and ("void " in current_line or re.search(rf"\w+\s+{name}", current_line) is not None)
                        )
                        if is_override_decl:
                            return match.group(0)

                    if start > 0 and code_content[start - 1] == ".":
                        if is_function:
                            return f"{match.group(0)}{name_suffix}"

                        before_dot = code_content[: start - 1]
                        prev_token = re.search(r"\b(\w+)$", before_dot)
                        prev_token_value = prev_token.group(1) if prev_token else ""
                        prev_token_base = (
                            prev_token_value[:-len(name_suffix)]
                            if prev_token_value.endswith(name_suffix)
                            else prev_token_value
                        )

                        if prev_token_value == "this":
                            return f"{match.group(0)}{name_suffix}"

                        if prev_token_base in candidates.classes:
                            return f"{match.group(0)}{name_suffix}"

                        var_type = candidates.var_type_map.get(prev_token_base)
                        if var_type is None:
                            var_decl_regex = re.compile(rf"\b(\w+)\s+{re.escape(prev_token_value)}\b")
                            var_match = var_decl_regex.search(code_content)
                            var_type = var_match.group(1) if var_match else None
                        var_type_base = (
                            var_type[:-len(name_suffix)] if var_type and var_type.endswith(name_suffix) else var_type
                        )
                        if var_type_base in candidates.classes:
                            return f"{match.group(0)}{name_suffix}"
                        return match.group(0)

                    return f"{match.group(0)}{name_suffix}"

                return pattern.sub(repl, code_content)

            for class_name in candidates.classes:
                code_content = replace_candidate(class_name)

            # Rename declarations/usages only when they are not part of a string literal.
            for var_name in vars_to_rename:
                def repl_var(match: re.Match[str], var_name: str = var_name) -> str:
                    start = match.start()
                    if var_name in KOTLIN_SYNTAX_NAMES:
                        return match.group(0)
                    if is_inside_string_literal(code_content, start):
                        return match.group(0)
                    # Kotlin property accessors are syntax, not identifiers
                    # that may be renamed (get() / set()).
                    if var_name in {"get", "set"}:
                        line_prefix = code_content[code_content.rfind("\n", 0, start) + 1:start].strip()
                        if line_prefix.startswith("get") or line_prefix.startswith("set"):
                            return match.group(0)

                    text_up_to_match = code_content[:start]
                    line_start = text_up_to_match.rfind("\n") + 1
                    line_end = code_content.find("\n", match.end())
                    if line_end == -1:
                        line_end = len(code_content)
                    current_line = code_content[line_start:line_end].strip()

                    if f"var {var_name}" in current_line or f"val {var_name}" in current_line:
                        return f"{match.group(0)}{name_suffix}"

                    local_decl_match = re.search(rf"\b([\w<>\[\].]+)\s+{re.escape(var_name)}\b", current_line)
                    if local_decl_match:
                        decl_type = local_decl_match.group(1)
                        control_keywords = {
                            "return", "if", "for", "while", "new", "throw", "else", "case",
                            "this", "super"
                        }
                        if decl_type in control_keywords:
                            return match.group(0)
                        primitives = {
                            "int", "float", "double", "long", "short", "byte", "char", "boolean"
                        }
                        known_type = candidates.var_type_map.get(var_name)
                        if known_type is not None:
                            decl_type_base = normalize_type(decl_type, name_suffix)
                            known_type_base = normalize_type(known_type, name_suffix)
                            both_primitive = (
                                decl_type_base in primitives and known_type_base in primitives
                            )
                            if not both_primitive and decl_type_base != known_type_base:
                                return match.group(0)
                    return f"{match.group(0)}{name_suffix}"

                code_content = re.compile(rf"\b{var_name}\b").sub(
                    lambda match: repl_var(match),
                    code_content,
                )

            for function_name in functions_to_rename:
                code_content = replace_candidate(function_name, is_function=True)

            local_var_pattern = re.compile(
                r"^\s{4,}(?!(?:private|public|protected|static|final|return|if|else|for|while|new|throw|case|break|continue)\b)([\w\[\]<>]+)\s+(\w+)\s*[=;(,]",
                re.MULTILINE,
            )
            local_vars: Set[str] = set()
            for match in local_var_pattern.finditer(code_content):
                type_name = match.group(1)
                var_name = match.group(2)
                control_keywords = {
                    "true", "false", "null", "this", "super",
                    "if", "else", "for", "while", "return", "new", "throw", "case", "break",
                    "continue", "try", "catch", "finally", "switch", "do", "instanceof"
                }
                primitive_or_class = {
                    "int", "float", "double", "long", "short", "byte", "char", "boolean",
                    "String", "Object"
                }
                is_valid_type = type_name in primitive_or_class or type_name[0].isupper()
                if (
                    is_valid_type
                    and var_name not in control_keywords
                    and not var_name.endswith(name_suffix)
                    and var_name not in candidates.classes
                    and var_name not in candidates.functions
                    and var_name not in {"i", "j", "k", "n", "e"}
                ):
                    local_vars.add(var_name)

            for var_name in (local_vars if is_target_package else set()):
                pattern = re.compile(rf"\b{var_name}\b")
                code_content = pattern.sub(
                    lambda match: (
                        match.group(0)
                        if is_inside_string_literal(code_content, match.start())
                        or (match.start() > 0 and code_content[match.start() - 1] == ".")
                        else f"{match.group(0)}{name_suffix}"
                    ),
                    code_content,
                )

            for old_field, new_field in candidates.xml_binding_map.items():
                code_content = re.compile(rf"\bR\.id\.{re.escape(old_field)}\b").sub(
                    f"R.id.{new_field}",
                    code_content,
                )
                code_content = re.compile(rf"\b{re.escape(old_field)}\b").sub(
                    lambda match: (
                        match.group(0)
                        if is_inside_string_literal(code_content, match.start())
                        else new_field
                    ),
                    code_content,
                )

            header_content = []
            for line in header_lines:
                if not line.lstrip().startswith("import "):
                    header_content.append(line)
                    continue

                import_path = line.removeprefix("import ").strip()
                if not import_path.startswith(name_package):
                    header_content.append(line)
                    continue
                if import_path.endswith(".*"):
                    header_content.append(line)
                    continue

                last_dot = import_path.rfind(".")
                if last_dot == -1:
                    header_content.append(line)
                    continue

                class_name = import_path[last_dot + 1:]
                if class_name not in candidates.classes or class_name.endswith(name_suffix):
                    header_content.append(line)
                    continue

                header_content.append(f"import {import_path}{name_suffix}")

            content = "\n".join(header_content + code_content.splitlines())
            file.write_text(content, encoding="utf-8", newline="\n")

            file_base2 = file.stem
            if file_base2 in candidates.classes:
                new_file = file.with_name(f"{file_base2}{name_suffix}{file.suffix}")
                if new_file != file and not new_file.exists():
                    file.rename(new_file)
        # XML is handled independently below so that resource qualifier folders
        # (layout-land, values-night, xml, etc.) are processed consistently.
        elif file.suffix == ".xml":
            content = file.read_text(encoding="utf-8")
            xml_id_pattern = re.compile(r"@\+?id/(\w+)")
            content = xml_id_pattern.sub(
                lambda match: (
                    match.group(0)
                    if match.group(1).endswith(name_suffix)
                    else match.group(0).replace(f"/{match.group(1)}", f"/{match.group(1)}{name_suffix}")
                ),
                content,
            )

            custom_view_pattern = re.compile(r"<(/?)([\w.]+)")
            content = custom_view_pattern.sub(
                lambda match: (
                    match.group(0)
                    if not match.group(2).startswith(name_package)
                    or match.group(2).rfind(".") == -1
                    or match.group(2)[match.group(2).rfind(".") + 1:].endswith(name_suffix)
                    else f"<{match.group(1)}{match.group(2)}{name_suffix}"
                ),
                content,
            )
            file.write_text(content, encoding="utf-8", newline="\n")


def main() -> None:
    print(
        "Python translation of the Kotlin rename scanner is ready. "
        "Use it with the same regex patterns as the Kotlin version."
    )


if __name__ == "__main__":
    main()
