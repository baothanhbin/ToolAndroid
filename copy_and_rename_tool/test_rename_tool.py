import re
import tempfile
import unittest
from pathlib import Path

from scanFolderForRenameCandidates import (
    RenameCandidates,
    rename_folder_using_candidates,
    scan_folder_for_rename_candidates,
)
from kotlin_ast_renamer import rename_kotlin_locals_and_private_members
from rename_transaction import RenameTransaction


class RenameToolTests(unittest.TestCase):
    def test_semantic_resolution_respects_shadowing_and_receivers(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Scopes.kt"
            source.write_text(
                "package com.example\n"
                "open class Parent { fun inherited() {} }\n"
                "class Model { val result = 1 }\n"
                "class Scopes : Parent() {\n"
                "  private fun work(value: Int) {\n"
                "    val result = value\n"
                "    run { val result = 2; println(result) }\n"
                "    Model().result\n"
                "    inherited()\n"
                "    work(result)\n"
                "  }\n"
                "}\n",
                encoding="utf-8",
            )

            rename_kotlin_locals_and_private_members(Path(temp_dir), "Safe", "com.example")
            renamed = source.read_text(encoding="utf-8")

            self.assertIn("private fun workSafe(valueSafe: Int)", renamed)
            self.assertIn("val resultSafe = valueSafe", renamed)
            self.assertIn("val resultSafe = 2; println(resultSafe)", renamed)
            self.assertIn("Model().result", renamed)
            self.assertIn("inherited()", renamed)
            self.assertIn("workSafe(resultSafe)", renamed)

    def test_semantic_keeps_private_extension_functions(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Extension.kt"
            source.write_text(
                "package com.example\n"
                "class Example {\n"
                "  private fun String.normalized() = trim()\n"
                "  fun show(value: String) = value.normalized()\n"
                "}\n",
                encoding="utf-8",
            )

            rename_kotlin_locals_and_private_members(Path(temp_dir), "Safe", "com.example")
            renamed = source.read_text(encoding="utf-8")

            self.assertIn("fun String.normalized()", renamed)
            self.assertIn("value.normalized()", renamed)

    def test_transaction_restores_content_and_renamed_file(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            folder = Path(temp_dir)
            original = folder / "Before.kt"
            original.write_text("class Before\n", encoding="utf-8")
            transaction = RenameTransaction(folder)
            renamed = folder / "After.kt"
            original.rename(renamed)
            renamed.write_text("class After\n", encoding="utf-8")

            transaction.rollback()

            self.assertEqual("class Before\n", original.read_text(encoding="utf-8"))
            self.assertFalse(renamed.exists())

    def test_ast_renames_locals_and_private_function_without_touching_sdk_member(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Example.kt"
            source.write_text(
                "package com.example\n"
                "class Example {\n"
                "  private fun calculate(value: Int) {\n"
                "    val result = value + 1\n"
                "    view.show()\n"
                "    calculate(result)\n"
                "  }\n"
                "  override fun onDraw(canvas: Canvas) { val width = canvas.width }\n"
                "}\n",
                encoding="utf-8",
            )

            stats = rename_kotlin_locals_and_private_members(
                Path(temp_dir), "Test", "com.example"
            )

            renamed = source.read_text(encoding="utf-8")
            self.assertIn("private fun calculateTest(valueTest: Int)", renamed)
            self.assertIn("val resultTest = valueTest + 1", renamed)
            self.assertIn("calculateTest(resultTest)", renamed)
            self.assertIn("view.show()", renamed)
            self.assertIn("override fun onDraw(canvas: Canvas)", renamed)
            self.assertIn("val widthTest = canvas.width", renamed)
            self.assertEqual(3, stats.variables)
            self.assertEqual(1, stats.functions)

    def test_ast_keeps_public_function_and_parameter_names(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Api.kt"
            source.write_text(
                "package com.example\nfun load(deviceId: String) = deviceId\n",
                encoding="utf-8",
            )

            rename_kotlin_locals_and_private_members(Path(temp_dir), "Test", "com.example")

            self.assertIn(
                "fun load(deviceId: String) = deviceId",
                source.read_text(encoding="utf-8"),
            )

    def test_semantic_offsets_handle_utf8_bom_and_underscore_parameters(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Bom.kt"
            source.write_text(
                "\ufeffpackage com.example\n"
                "private fun map(value: Int, _: String) { val result = value; println(result) }\n",
                encoding="utf-8",
            )

            rename_kotlin_locals_and_private_members(Path(temp_dir), "Safe", "com.example")
            renamed = source.read_text(encoding="utf-8")

            self.assertIn("fun mapSafe(valueSafe: Int, _: String)", renamed)
            self.assertIn("val resultSafe = valueSafe", renamed)

    def test_scanner_defaults_to_build_safe_identifiers(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Example.kt"
            source.write_text(
                "package com.example\nclass Example { val context = 1; fun show() {} }",
                encoding="utf-8",
            )

            candidates = scan_folder_for_rename_candidates(
                "com.example",
                Path(temp_dir),
                re.compile(r"class\s+(\w+)"),
                re.compile(r"class\s+(\w+)"),
                re.compile(r"fun\s+(\w+)\s*\("),
                set(),
            )

            self.assertEqual({"Example"}, candidates.classes)
            self.assertEqual(set(), candidates.vars)
            self.assertEqual(set(), candidates.functions)

    def test_class_rename_does_not_touch_same_named_android_resource(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "BaseDialog.kt"
            source.write_text(
                "package com.example\nclass BaseDialog : Dialog(R.style.BaseDialog)",
                encoding="utf-8",
            )
            candidates = RenameCandidates({"BaseDialog"}, set(), set())

            rename_folder_using_candidates(Path(temp_dir), candidates, "Test", "com.example")

            renamed = (Path(temp_dir) / "BaseDialogTest.kt").read_text(encoding="utf-8")
            self.assertIn("class BaseDialogTest", renamed)
            self.assertIn("R.style.BaseDialog", renamed)
            self.assertNotIn("R.style.BaseDialogTest", renamed)

    def test_binding_fields_do_not_rename_other_resources_or_properties(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Screen.kt"
            source.write_text(
                "package com.example\n"
                "fun render() { binding.spotlight.text = R.string.spotlight; model.spotlight }",
                encoding="utf-8",
            )
            candidates = RenameCandidates(
                set(),
                set(),
                set(),
                xml_binding_map={"spotlight": "spotlightTest"},
            )

            rename_folder_using_candidates(Path(temp_dir), candidates, "Test", "com.example")

            renamed = source.read_text(encoding="utf-8")
            self.assertIn("binding.spotlightTest", renamed)
            self.assertIn("R.string.spotlight", renamed)
            self.assertIn("model.spotlight", renamed)

    def test_xml_id_does_not_rename_manual_view_holder_property(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Adapter.kt"
            source.write_text(
                "package com.example\n"
                "class Holder(itemView: View) {\n"
                "  val title: TextView = itemView.findViewById(R.id.title)\n"
                "}\n"
                "fun bind(holder: Holder) { holder.title.text = \"ok\" }\n",
                encoding="utf-8",
            )
            candidates = RenameCandidates(set(), set(), set(), xml_binding_map={"title": "titleSafe"})

            rename_folder_using_candidates(Path(temp_dir), candidates, "Safe", "com.example")
            renamed = source.read_text(encoding="utf-8")

            self.assertIn("val title: TextView", renamed)
            self.assertIn("R.id.titleSafe", renamed)
            self.assertIn("holder.title.text", renamed)

    def test_xml_id_renames_unqualified_field_in_binding_apply_scope(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "Screen.kt"
            source.write_text(
                "package com.example\n"
                "fun render() { binding.viewTop.apply { title.text = \"ok\" } }\n",
                encoding="utf-8",
            )
            candidates = RenameCandidates(set(), set(), set(), xml_binding_map={"title": "titleSafe"})

            rename_folder_using_candidates(Path(temp_dir), candidates, "Safe", "com.example")

            self.assertIn(
                "binding.viewTop.apply { titleSafe.text",
                source.read_text(encoding="utf-8"),
            )

    def test_manifest_component_references_follow_class_rename(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            manifest = Path(temp_dir) / "AndroidManifest.xml"
            manifest.write_text(
                '<manifest package="com.example"><application android:name=".MyApplication">'
                '<activity android:name=".ui.MainActivity"/>'
                '<activity android:name="com.example.ui.MainActivity"/></application></manifest>',
                encoding="utf-8",
            )
            candidates = RenameCandidates({"MyApplication", "MainActivity"}, set(), set())

            rename_folder_using_candidates(Path(temp_dir), candidates, "Test", "com.example")

            renamed = manifest.read_text(encoding="utf-8")
            self.assertIn('android:name=".MyApplicationTest"', renamed)
            self.assertIn('android:name=".ui.MainActivityTest"', renamed)
            self.assertIn('android:name="com.example.ui.MainActivityTest"', renamed)

    def test_room_entity_keeps_default_table_name_after_class_rename(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "AccountBlockedNumbersModel.kt"
            source.write_text(
                "package com.example\n"
                "import androidx.room.Entity\n"
                "@Entity\n"
                "data class AccountBlockedNumbersModel(val id: String)\n",
                encoding="utf-8",
            )
            candidates = RenameCandidates({"AccountBlockedNumbersModel"}, set(), set())

            rename_folder_using_candidates(Path(temp_dir), candidates, "Test", "com.example")

            renamed = (Path(temp_dir) / "AccountBlockedNumbersModelTest.kt").read_text(encoding="utf-8")
            self.assertIn('@Entity(tableName = "AccountBlockedNumbersModel")', renamed)
            self.assertIn("data class AccountBlockedNumbersModelTest", renamed)

    def test_bom_and_companion_import_remain_valid_after_rename(self):
        with tempfile.TemporaryDirectory(dir=Path.cwd()) as temp_dir:
            source = Path(temp_dir) / "CoreApplication.kt"
            source.write_text(
                "\ufeffpackage com.example\n"
                "import com.example.CoreApplication.Companion.isEnabled\n"
                "class CoreApplication { companion object { val isEnabled = true } }\n",
                encoding="utf-8",
            )
            candidates = RenameCandidates({"CoreApplication"}, set(), set())
            rename_folder_using_candidates(Path(temp_dir), candidates, "Test", "com.example")

            renamed = (Path(temp_dir) / "CoreApplicationTest.kt").read_text(encoding="utf-8")
            self.assertTrue(renamed.startswith("package com.example\nimport "))
            self.assertIn("import com.example.CoreApplicationTest.Companion.isEnabled", renamed)


if __name__ == "__main__":
    unittest.main()
