// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.highlighting

import com.intellij.icons.AllIcons
import com.intellij.lang.Language
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.fileTypes.SyntaxHighlighter
import com.intellij.openapi.fileTypes.SyntaxHighlighterFactory
import com.intellij.openapi.options.colors.AttributesDescriptor
import com.intellij.openapi.options.colors.ColorDescriptor
import com.intellij.openapi.options.colors.RainbowColorSettingsPage
import com.intellij.psi.codeStyle.DisplayPriority
import com.intellij.psi.codeStyle.DisplayPrioritySortable
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.JsonLanguage
import javax.swing.Icon

/**
 * @author Mikhail Golubev
 */
class JsonColorsPage : RainbowColorSettingsPage, DisplayPrioritySortable {
    override fun getIcon(): Icon {
        return AllIcons.FileTypes.Json
    }

    override fun getHighlighter(): SyntaxHighlighter {
        return SyntaxHighlighterFactory.getSyntaxHighlighter(JsonLanguage.INSTANCE, null, null)
    }

    override fun getDemoText(): String {
        return """
      {
        // Line comments are not included in standard but nonetheless allowed.
        /* As well as block comments. */
        <propertyKey>"the only keywords are"</propertyKey>: [true, false, null],
        <propertyKey>"strings with"</propertyKey>: {
          <propertyKey>"no escapes"</propertyKey>: "pseudopolinomiality"
          <propertyKey>"valid escapes"</propertyKey>: "C-style\r\
           and unicode\u0021",
          <propertyKey>"illegal escapes"</propertyKey>: "\0377\x\${'"'}
        },
        <propertyKey>"some numbers"</propertyKey>: [
          42,
          -0.0e-0,
          6.626e-34
        ] 
      }
      """.trimIndent()
    }

    override fun getAdditionalHighlightingTagToDescriptorMap(): Map<String, TextAttributesKey> {
        return ourAdditionalHighlighting
    }

    override fun getAttributeDescriptors(): Array<AttributesDescriptor> {
        return ourAttributeDescriptors
    }

    override fun getColorDescriptors(): Array<ColorDescriptor> {
        return ColorDescriptor.EMPTY_ARRAY
    }

    override fun getDisplayName(): String {
        return JsonBundle.message("settings.display.name.json")
    }

    override fun getPriority(): DisplayPriority {
        return DisplayPriority.LANGUAGE_SETTINGS
    }

    override fun isRainbowType(type: TextAttributesKey): Boolean {
        return JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY == type || JsonSyntaxHighlighterFactory.JSON_BRACES == type || JsonSyntaxHighlighterFactory.JSON_BRACKETS == type || JsonSyntaxHighlighterFactory.JSON_STRING == type || JsonSyntaxHighlighterFactory.JSON_NUMBER == type || JsonSyntaxHighlighterFactory.JSON_KEYWORD == type
    }

    override fun getLanguage(): Language {
        return JsonLanguage.INSTANCE
    }

    companion object {
        private val ourAdditionalHighlighting: Map<String, TextAttributesKey> =
            java.util.Map.of("propertyKey", JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY)

        private val ourAttributeDescriptors = arrayOf(
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.property.key"),
                JsonSyntaxHighlighterFactory.JSON_PROPERTY_KEY
            ),

            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.braces"),
                JsonSyntaxHighlighterFactory.JSON_BRACES
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.brackets"),
                JsonSyntaxHighlighterFactory.JSON_BRACKETS
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.comma"),
                JsonSyntaxHighlighterFactory.JSON_COMMA
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.colon"),
                JsonSyntaxHighlighterFactory.JSON_COLON
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.number"),
                JsonSyntaxHighlighterFactory.JSON_NUMBER
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.string"),
                JsonSyntaxHighlighterFactory.JSON_STRING
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.keyword"),
                JsonSyntaxHighlighterFactory.JSON_KEYWORD
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.line.comment"),
                JsonSyntaxHighlighterFactory.JSON_LINE_COMMENT
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.block.comment"),
                JsonSyntaxHighlighterFactory.JSON_BLOCK_COMMENT
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.valid.escape.sequence"),
                JsonSyntaxHighlighterFactory.JSON_VALID_ESCAPE
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.invalid.escape.sequence"),
                JsonSyntaxHighlighterFactory.JSON_INVALID_ESCAPE
            ),
            AttributesDescriptor(
                JsonBundle.messagePointer("color.page.attribute.parameter"),
                JsonSyntaxHighlighterFactory.JSON_PARAMETER
            )
        )
    }
}
