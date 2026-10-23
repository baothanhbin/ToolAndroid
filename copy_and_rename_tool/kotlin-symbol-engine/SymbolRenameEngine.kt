@file:Suppress("DEPRECATION")

package toolandroid.symbols

import com.intellij.openapi.util.Disposer
import com.intellij.psi.PsiElement
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.EnvironmentConfigFiles
import org.jetbrains.kotlin.cli.jvm.compiler.KotlinCoreEnvironment
import org.jetbrains.kotlin.cli.jvm.compiler.NoScopeRecordCliBindingTrace
import org.jetbrains.kotlin.cli.jvm.compiler.TopDownAnalyzerFacadeForJVM
import org.jetbrains.kotlin.cli.jvm.config.addJvmClasspathRoot
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.config.CommonConfigurationKeys
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.FunctionDescriptor
import org.jetbrains.kotlin.descriptors.VariableDescriptor
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import java.io.File
import java.util.Base64

private data class Edit(val path: String, val start: Int, val end: Int, val replacement: String, val kind: String, val declaration: Boolean)

private fun encoded(value: String) = Base64.getEncoder().encodeToString(value.toByteArray(Charsets.UTF_8))

private fun descriptor(context: BindingContext, declaration: KtDeclaration): DeclarationDescriptor? =
    context[BindingContext.DECLARATION_TO_DESCRIPTOR, declaration]

private fun isEligibleFunction(function: KtNamedFunction): Boolean =
    function.name != null && function.hasModifier(KtTokens.PRIVATE_KEYWORD) &&
        !function.hasModifier(KtTokens.OVERRIDE_KEYWORD) &&
        function.receiverTypeReference == null

private fun owningFunction(parameter: KtParameter): KtNamedFunction? =
    PsiTreeUtil.getParentOfType(parameter, KtNamedFunction::class.java, true)

private fun isEligibleVariable(declaration: KtDeclaration): Boolean = when (declaration) {
    is KtProperty -> declaration.isLocal
    is KtParameter -> owningFunction(declaration)?.let(::isEligibleFunction) == true
    else -> false
}

private fun nameElement(declaration: KtNamedDeclaration): PsiElement? = declaration.nameIdentifier

fun main(args: Array<String>) {
    require(args.size >= 4) { "usage: engine <source-root> <suffix> <classpath-file> <package>" }
    val sourceRoot = File(args[0]).canonicalFile
    val suffix = args[1]
    val classpathFile = File(args[2])
    val packageName = args[3]
    val disposable = Disposer.newDisposable("toolandroid-symbol-engine")
    try {
        val configuration = CompilerConfiguration().apply {
            put(CommonConfigurationKeys.MODULE_NAME, "toolandroid-analysis")
            put(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)
            addKotlinSourceRoot(sourceRoot.path)
            if (classpathFile.isFile) {
                classpathFile.readLines().filter(String::isNotBlank).map(::File)
                    .filter(File::exists).forEach(::addJvmClasspathRoot)
            }
        }
        val environment = KotlinCoreEnvironment.createForProduction(
            disposable, configuration, EnvironmentConfigFiles.JVM_CONFIG_FILES
        )
        val files = environment.getSourceFiles()
        val trace = NoScopeRecordCliBindingTrace(environment.project)
        TopDownAnalyzerFacadeForJVM.analyzeFilesWithJavaIntegration(
            environment.project, files, trace, configuration, environment::createPackagePartProvider
        )
        val context = trace.bindingContext

        val candidates = mutableMapOf<DeclarationDescriptor, Pair<KtNamedDeclaration, String>>()
        for (file in files) {
            PsiTreeUtil.collectElementsOfType(file, KtNamedDeclaration::class.java).forEach { declaration ->
                val filePackage = file.packageFqName.asString()
                if (filePackage != packageName && !filePackage.startsWith("$packageName.")) return@forEach
                val eligible = (declaration is KtNamedFunction && isEligibleFunction(declaration)) ||
                    (declaration is KtDeclaration && isEligibleVariable(declaration))
                val name = declaration.name
                if (eligible && name != null && name != "_" && !name.endsWith(suffix)) {
                    val target = descriptor(context, declaration)?.original ?: return@forEach
                    if (declaration is KtNamedFunction) {
                        // Missing Android/variant classpath can leave receiver or lambda
                        // call sites unresolved. Fail closed: one uncertain occurrence means
                        // the declaration is not safe to rename at all.
                        val uncertain = PsiTreeUtil.collectElementsOfType(
                            file, KtSimpleNameExpression::class.java
                        ).any { reference ->
                            reference.getReferencedName() == name &&
                                context[BindingContext.REFERENCE_TARGET, reference] == null
                        }
                        if (uncertain) return@forEach
                    }
                    candidates[target] = declaration to name
                }
            }
        }

        val edits = linkedMapOf<Triple<String, Int, Int>, Edit>()
        for ((target, value) in candidates) {
            val (declaration, oldName) = value
            val id = nameElement(declaration) ?: continue
            val path = declaration.containingKtFile.virtualFilePath
            val kind = if (declaration is KtNamedFunction) "function" else "variable"
            edits[Triple(path, id.textRange.startOffset, id.textRange.endOffset)] =
                Edit(path, id.textRange.startOffset, id.textRange.endOffset, oldName + suffix, kind, true)

            for (file in files) {
                PsiTreeUtil.collectElementsOfType(file, KtSimpleNameExpression::class.java).forEach { reference ->
                    val resolved = context[BindingContext.REFERENCE_TARGET, reference]?.original
                    if (resolved == target) {
                        val range = reference.getReferencedNameElement().textRange
                        val refPath = file.virtualFilePath
                        edits[Triple(refPath, range.startOffset, range.endOffset)] =
                            Edit(refPath, range.startOffset, range.endOffset, oldName + suffix, kind, false)
                    }
                }
            }
        }

        edits.values.forEach { edit ->
            println(listOf(encoded(edit.path), edit.start, edit.end, encoded(edit.replacement), edit.kind, edit.declaration).joinToString("\t"))
        }
    } finally {
        Disposer.dispose(disposable)
    }
}
