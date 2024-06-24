// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter

import com.intellij.formatting.*
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.xxxlin.json.JsonElementTypes
import com.xxxlin.json.JsonLanguage

class JsonFormattingBuilderModel : FormattingModelBuilder {
    override fun createModel(formattingContext: FormattingContext): FormattingModel {
        val settings = formattingContext.codeStyleSettings
        val customSettings = settings.getCustomSettings(
            JsonCodeStyleSettings::class.java
        )
        val spacingBuilder = createSpacingBuilder(settings)
        val block = JsonBlock(
            null,
            formattingContext.node,
            customSettings,
            null,
            Indent.getSmartIndent(Indent.Type.CONTINUATION),
            null,
            spacingBuilder
        )
        return FormattingModelProvider.createFormattingModelForPsiFile(
            formattingContext.containingFile,
            block,
            settings
        )
    }

    companion object {
        fun createSpacingBuilder(settings: CodeStyleSettings): SpacingBuilder {
            val jsonSettings = settings.getCustomSettings(
                JsonCodeStyleSettings::class.java
            )
            val commonSettings = settings.getCommonSettings(JsonLanguage.INSTANCE)

            val spacesBeforeComma = if (commonSettings.SPACE_BEFORE_COMMA) 1 else 0
            val spacesBeforeColon = if (jsonSettings.SPACE_BEFORE_COLON) 1 else 0
            val spacesAfterColon = if (jsonSettings.SPACE_AFTER_COLON) 1 else 0

            return SpacingBuilder(settings, JsonLanguage.INSTANCE)
                .before(JsonElementTypes.COLON).spacing(spacesBeforeColon, spacesBeforeColon, 0, false, 0)
                .after(JsonElementTypes.COLON).spacing(spacesAfterColon, spacesAfterColon, 0, false, 0)
                .withinPair(JsonElementTypes.L_BRACKET, JsonElementTypes.R_BRACKET)
                .spaceIf(commonSettings.SPACE_WITHIN_BRACKETS, true)
                .withinPair(JsonElementTypes.L_CURLY, JsonElementTypes.R_CURLY)
                .spaceIf(commonSettings.SPACE_WITHIN_BRACES, true)
                .before(JsonElementTypes.COMMA).spacing(spacesBeforeComma, spacesBeforeComma, 0, false, 0)
                .after(JsonElementTypes.COMMA).spaceIf(commonSettings.SPACE_AFTER_COMMA)
        }
    }
}
