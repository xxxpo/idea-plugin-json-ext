// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter

import com.intellij.application.options.IndentOptionsEditor
import com.intellij.application.options.SmartIndentOptionsEditor
import com.intellij.application.options.codeStyle.properties.CodeStyleFieldAccessor
import com.intellij.application.options.codeStyle.properties.MagicIntegerConstAccessor
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizableOptions
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.IndentOptions
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider
import com.intellij.util.ArrayUtil
import com.intellij.util.containers.ContainerUtil
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.JsonLanguage
import java.lang.reflect.Field
import java.util.*

/**
 * @author Mikhail Golubev
 */
class JsonLanguageCodeStyleSettingsProvider : LanguageCodeStyleSettingsProvider() {
    private object Holder {
        val ALIGN_OPTIONS: Array<String?> =
            Arrays.stream(JsonCodeStyleSettings.PropertyAlignment.entries.toTypedArray())
                .map { alignment: JsonCodeStyleSettings.PropertyAlignment -> alignment.description }
                .toArray<String?> { value: Int -> arrayOfNulls<String>(value) }

        val ALIGN_VALUES: IntArray = ArrayUtil.toIntArray(
            ContainerUtil.map(
                JsonCodeStyleSettings.PropertyAlignment.entries.toTypedArray()
            ) { alignment: JsonCodeStyleSettings.PropertyAlignment -> alignment.id }
        )

        val SAMPLE: String = """
                {
                    "json literals are": {
                        "strings": ["foo", "bar", "\u0062\u0061\u0072"],
                        "numbers": [42, 6.62606975e-34],
                        "boolean values": [true, false,],
                        "objects": {"null": null,"another": null,}
                    }
                }
                """.trimIndent()
    }

    override fun customizeSettings(consumer: CodeStyleSettingsCustomizable, settingsType: SettingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions(
                "SPACE_WITHIN_BRACKETS",
                "SPACE_WITHIN_BRACES",
                "SPACE_AFTER_COMMA",
                "SPACE_BEFORE_COMMA"
            )
            consumer.renameStandardOption(
                "SPACE_WITHIN_BRACES",
                JsonBundle.message("formatter.space_within_braces.label")
            )
            consumer.showCustomOption(
                JsonCodeStyleSettings::class.java,
                "SPACE_BEFORE_COLON",
                JsonBundle.message("formatter.space_before_colon.label"),
                CodeStyleSettingsCustomizableOptions.getInstance().SPACES_OTHER
            )
            consumer.showCustomOption(
                JsonCodeStyleSettings::class.java,
                "SPACE_AFTER_COLON",
                JsonBundle.message("formatter.space_after_colon.label"),
                CodeStyleSettingsCustomizableOptions.getInstance().SPACES_OTHER
            )
        } else if (settingsType == SettingsType.BLANK_LINES_SETTINGS) {
            consumer.showStandardOptions("KEEP_BLANK_LINES_IN_CODE")
        } else if (settingsType == SettingsType.WRAPPING_AND_BRACES_SETTINGS) {
            consumer.showStandardOptions(
                "RIGHT_MARGIN",
                "WRAP_ON_TYPING",
                "KEEP_LINE_BREAKS",
                "WRAP_LONG_LINES"
            )

            consumer.showCustomOption(
                JsonCodeStyleSettings::class.java,
                "KEEP_TRAILING_COMMA",
                JsonBundle.message("formatter.trailing_comma.label"),
                CodeStyleSettingsCustomizableOptions.getInstance().WRAPPING_KEEP
            )

            consumer.showCustomOption(
                JsonCodeStyleSettings::class.java,
                "ARRAY_WRAPPING",
                JsonBundle.message("formatter.wrapping_arrays.label"),
                null,
                CodeStyleSettingsCustomizableOptions.getInstance().WRAP_OPTIONS,
                CodeStyleSettingsCustomizable.WRAP_VALUES
            )

            consumer.showCustomOption(
                JsonCodeStyleSettings::class.java,
                "OBJECT_WRAPPING",
                JsonBundle.message("formatter.objects.label"),
                null,
                CodeStyleSettingsCustomizableOptions.getInstance().WRAP_OPTIONS,
                CodeStyleSettingsCustomizable.WRAP_VALUES
            )

            consumer.showCustomOption(
                JsonCodeStyleSettings::class.java,
                "PROPERTY_ALIGNMENT",
                JsonBundle.message("formatter.align.properties.caption"),
                JsonBundle.message("formatter.objects.label"),
                Holder.ALIGN_OPTIONS,
                Holder.ALIGN_VALUES
            )
        }
    }

    override fun getLanguage(): Language {
        return JsonLanguage.INSTANCE
    }

    override fun getIndentOptionsEditor(): IndentOptionsEditor? {
        return SmartIndentOptionsEditor()
    }

    override fun getCodeSample(settingsType: SettingsType): String? {
        return Holder.SAMPLE
    }

    override fun customizeDefaults(
        commonSettings: CommonCodeStyleSettings,
        indentOptions: IndentOptions
    ) {
        indentOptions.INDENT_SIZE = 2
        // strip all blank lines by default
        commonSettings.KEEP_BLANK_LINES_IN_CODE = 0
    }

    override fun getAccessor(codeStyleObject: Any, field: Field): CodeStyleFieldAccessor<*, *>? {
        if (codeStyleObject is JsonCodeStyleSettings && field.name == "PROPERTY_ALIGNMENT") {
            return MagicIntegerConstAccessor(
                codeStyleObject, field,
                intArrayOf(
                    JsonCodeStyleSettings.PropertyAlignment.DO_NOT_ALIGN.id,
                    JsonCodeStyleSettings.PropertyAlignment.ALIGN_ON_VALUE.id,
                    JsonCodeStyleSettings.PropertyAlignment.ALIGN_ON_COLON.id
                ),
                arrayOf(
                    "do_not_align",
                    "align_on_value",
                    "align_on_colon"
                )
            )
        }
        return null
    }
}
