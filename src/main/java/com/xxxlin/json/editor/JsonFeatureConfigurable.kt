// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.editor

import com.intellij.openapi.options.BeanConfigurable
import com.intellij.openapi.options.SearchableConfigurable
import com.xxxlin.json.JsonBundle

class JsonFeatureConfigurable : BeanConfigurable<Unit>(Unit), SearchableConfigurable {

    init {
        JsonFeatureOptions.instance.let { settings ->
            checkBox(
                JsonBundle.message("settings.feature.match_string"),
                settings::MATCH_STRING,
            )

            checkBox(
                JsonBundle.message("settings.feature.match_string_slot_curly_brace"),
                settings::MATCH_STRING_SLOT_BRACE
            )

            checkBox(
                JsonBundle.message("settings.feature.Highlight_key_of__pairs_of_percent_signs"),
                settings::JSON_KEY_PERCENT_SLOT_HIGHLIGHT
            )
        }
    }

    override fun getDisplayName(): String {
        return "JSON Ext Feature"
    }

    override fun getId(): String {
        return "editor.preferences.jsonExtFeatureOptions"
    }

    override fun getHelpTopic() = "reference.settings.json_ext"
}
