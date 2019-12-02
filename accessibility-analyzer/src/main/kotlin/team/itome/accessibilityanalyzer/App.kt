/*
 * This Kotlin source file was generated by the Gradle 'init' task.
 */
package team.itome.accessibilityanalyzer

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckMetadata.METADATA_KEY_CUSTOMIZED_TOUCH_TARGET_SIZE
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckPreset
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityCheckResult.AccessibilityCheckResultType
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheck
import com.google.android.apps.common.testing.accessibility.framework.AccessibilityHierarchyCheckResult
import com.google.android.apps.common.testing.accessibility.framework.Metadata
import com.google.android.apps.common.testing.accessibility.framework.uielement.AccessibilityHierarchy
import com.google.android.apps.common.testing.accessibility.framework.uielement.ViewHierarchyElement
import com.google.android.apps.common.testing.accessibility.framework.uielement.proto.AccessibilityHierarchyProtos.AccessibilityHierarchyProto
import org.im4java.core.ConvertCmd
import org.im4java.core.IMOperation
import java.io.File
import java.io.FileNotFoundException
import java.util.*

fun main(args: Array<String>) = AccessibilityCheckCommand().main(args)

class AccessibilityCheckCommand : CliktCommand() {

  companion object {
    private const val FRAME_MARGIN = 16
    private const val FRAME_STROKE_WIDTH = 8
  }

  private val targetDir by option(
    "--target",
    help = "Target directory that contains accessibility.meta files"
  ).required()

  private val lang by option(
    "--lang",
    help = "Language code to display test result. ex) en, jp"
  )

  private val minTouchTargetSize by option(
    "--min-touch-target-size",
    help = "Minimum touch target size in dp."
  ).int()

  private val targetDirFullPath: String
    get() = targetDir.replace("~", System.getProperty("user.home"))

  override fun run() {
    val dir = File(targetDirFullPath)
    val files = dir.listFiles()
      ?.filter { Regex("accessibility[0-9]+.meta").matches(it.name) }
      ?: throw FileNotFoundException("No test target file found in $targetDirFullPath")
    val metadata = createMetadata()

    for (file in files) {
      val proto = file.inputStream().use { stream -> AccessibilityHierarchyProto.parseFrom(stream) }
      val hierarchy = AccessibilityHierarchy.newBuilder(proto).build()
      val results = runAccessibilityChecks(hierarchy, metadata)

      results
        .filter {
          it.type == AccessibilityCheckResultType.ERROR ||
              it.type == AccessibilityCheckResultType.WARNING
        }
        .forEachIndexed { index, checkResult ->
          @Suppress("UNCHECKED_CAST")
          val checkClass = AccessibilityCheckPreset.getHierarchyCheckForClass(
            checkResult.sourceCheckClass as Class<out AccessibilityHierarchyCheck>
          )
          val locale = Locale.getAvailableLocales().find { it.language == lang } ?: Locale.US
          println(checkClass.getTitleMessage(locale))
          println(checkClass.getMessageForResult(locale, checkResult))

          val outputProto = checkResult.toProto()
          val inputFileNumber = file.nameWithoutExtension.removePrefix("accessibility")
          val outputProtoFile = File(dir, "accessibility${inputFileNumber}_check_result$index.meta")
          val targetPngFile = File(dir, "$inputFileNumber.png").takeIf { it.exists() } ?: run {
            println("Target file $inputFileNumber.png not found.")
            return@forEachIndexed
          }
          val outputPngFile = File(dir, "accessibility${inputFileNumber}_check_result$index.png")

          outputProtoFile.createNewFile()
          outputProtoFile.outputStream().use { outputProto.writeTo(it) }
          checkResult.element?.let {
            generateTestResultImage(targetPngFile.absolutePath, outputPngFile.absolutePath, it)
          }
        }
    }
  }

  private fun runAccessibilityChecks(
    hierarchy: AccessibilityHierarchy,
    metadata: Metadata
  ): List<AccessibilityHierarchyCheckResult> {
    return AccessibilityCheckPreset
      .getAccessibilityHierarchyChecksForPreset(AccessibilityCheckPreset.LATEST)
      .flatMap { it.runCheckOnHierarchy(hierarchy, null, metadata) }
  }

  private fun createMetadata(): Metadata {
    return Metadata().apply {
      minTouchTargetSize?.let { putInt(METADATA_KEY_CUSTOMIZED_TOUCH_TARGET_SIZE, it) }
    }
  }

  private fun generateTestResultImage(
    targetFilePath: String,
    outputFilePath: String,
    view: ViewHierarchyElement
  ) {
    val command = ConvertCmd()
    val operation = IMOperation()
    val left = view.boundsInScreen.left - FRAME_MARGIN
    val top = view.boundsInScreen.top - FRAME_MARGIN
    val right = view.boundsInScreen.right + FRAME_MARGIN
    val bottom = view.boundsInScreen.bottom + FRAME_MARGIN
    operation.addImage(targetFilePath)
    operation.strokewidth(FRAME_STROKE_WIDTH)
    operation.stroke("#FF0000")
    operation.draw("line $left,$top $right,$top")
    operation.draw("line $left,$top $left,$bottom")
    operation.draw("line $left,$bottom $right,$bottom")
    operation.draw("line $right,$top $right,$bottom")
    operation.addImage(outputFilePath)
    command.run(operation)
  }
}