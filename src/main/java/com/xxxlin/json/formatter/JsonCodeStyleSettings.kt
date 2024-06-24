// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.formatter

import com.intellij.psi.codeStyle.CodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings
import com.intellij.psi.codeStyle.CommonCodeStyleSettings.WrapConstant
import com.intellij.psi.codeStyle.CustomCodeStyleSettings
import com.xxxlin.json.JsonBundle
import com.xxxlin.json.JsonLanguage
import org.intellij.lang.annotations.MagicConstant
import org.jetbrains.annotations.PropertyKey

/**
 * @author Mikhail Golubev
 */
class JsonCodeStyleSettings(container: CodeStyleSettings?) :
    CustomCodeStyleSettings(JsonLanguage.INSTANCE.id, container!!) {
    @JvmField
    var SPACE_AFTER_COLON: Boolean = true
    @JvmField
    var SPACE_BEFORE_COLON: Boolean = false
    @JvmField
    var KEEP_TRAILING_COMMA: Boolean = false

    // TODO: check whether it's possible to migrate CustomCodeStyleSettings to newer com.intellij.util.xmlb.XmlSerializer
    /**
     * Contains value of [PropertyAlignment.getId]
     *
     * @see .DO_NOT_ALIGN_PROPERTY
     *
     * @see .ALIGN_PROPERTY_ON_VALUE
     *
     * @see .ALIGN_PROPERTY_ON_COLON
     */
    @JvmField
    var PROPERTY_ALIGNMENT: Int = PropertyAlignment.DO_NOT_ALIGN.id

    @JvmField
    @MagicConstant(
        flags = [CommonCodeStyleSettings.DO_NOT_WRAP.toLong(), CommonCodeStyleSettings.WRAP_ALWAYS.toLong(), CommonCodeStyleSettings.WRAP_AS_NEEDED.toLong(), CommonCodeStyleSettings.WRAP_ON_EVERY_ITEM.toLong()
        ]
    )
    @WrapConstant
    var OBJECT_WRAPPING: Int = CommonCodeStyleSettings.WRAP_ALWAYS

    // This was default policy for array elements wrapping in JavaScript's JSON.
    // CHOP_DOWN_IF_LONG seems more appropriate however for short arrays.
    @JvmField
    @MagicConstant(
        flags = [CommonCodeStyleSettings.DO_NOT_WRAP.toLong(), CommonCodeStyleSettings.WRAP_ALWAYS.toLong(), CommonCodeStyleSettings.WRAP_AS_NEEDED.toLong(), CommonCodeStyleSettings.WRAP_ON_EVERY_ITEM.toLong()
        ]
    )
    @WrapConstant
    var ARRAY_WRAPPING: Int = CommonCodeStyleSettings.WRAP_ALWAYS

    enum class PropertyAlignment(val id: Int, key: @PropertyKey(resourceBundle = JsonBundle.BUNDLE) String) {
        DO_NOT_ALIGN(0, "formatter.align.properties.none"),
        ALIGN_ON_VALUE(1, "formatter.align.properties.on.value"),
        ALIGN_ON_COLON(2, "formatter.align.properties.on.colon");

        private val myKey: @PropertyKey(resourceBundle = JsonBundle.BUNDLE) String? =
            key

        val description: String
            get() = JsonBundle.message(myKey!!)
    }

    companion object {
        val DO_NOT_ALIGN_PROPERTY: Int = PropertyAlignment.DO_NOT_ALIGN.id
        @JvmField
        val ALIGN_PROPERTY_ON_VALUE: Int = PropertyAlignment.ALIGN_ON_VALUE.id
        @JvmField
        val ALIGN_PROPERTY_ON_COLON: Int = PropertyAlignment.ALIGN_ON_COLON.id
    }
}
