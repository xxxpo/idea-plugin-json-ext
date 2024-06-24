// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter

import com.intellij.application.options.CodeStyleAbstractConfigurable
import com.intellij.application.options.CodeStyleAbstractPanel
import com.intellij.application.options.TabbedLanguageCodeStylePanel
import com.intellij.lang.Language
import com.intellij.psi.codeStyle.CodeStyleConfigurable
import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CodeStyleSettingsProvider
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.JsonLanguage

/**
 * @author Mikhail Golubev
 */
class JsonCodeStyleSettingsProvider : CodeStyleSettingsProvider() {
    override fun createConfigurable(
        settings: CodeStyleSettings,
        originalSettings: CodeStyleSettings
    ): CodeStyleConfigurable {
        return object : CodeStyleAbstractConfigurable(
            settings,
            originalSettings,
            JsonBundle.message("settings.display.name.json")
        ) {
            override fun createPanel(settings: CodeStyleSettings): CodeStyleAbstractPanel {
                val language: Language = JsonLanguage.INSTANCE
                val currentSettings = currentSettings
                return object : TabbedLanguageCodeStylePanel(language, currentSettings, settings) {
                    override fun initTabs(settings: CodeStyleSettings) {
                        addIndentOptionsTab(settings)
                        addSpacesTab(settings)
                        addBlankLinesTab(settings)
                        addWrappingAndBracesTab(settings)
                    }
                }
            }

            override fun getHelpTopic(): String {
                return "reference.settingsdialog.codestyle.json"
            }
        }
    }

    override fun getConfigurableDisplayName(): String? {
        return JsonLanguage.INSTANCE.displayName
    }

    override fun createCustomSettings(settings: CodeStyleSettings): CustomCodeStyleSettings? {
        return JsonCodeStyleSettings(settings)
    }

    override fun getLanguage(): Language? {
        return JsonLanguage.INSTANCE
    }
}
