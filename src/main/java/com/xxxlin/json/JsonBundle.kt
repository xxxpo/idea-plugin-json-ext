// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.xxxlin.json

import com.intellij.DynamicBundle
import com.intellij.util.ArrayUtil
import com.xxxlin.utils.BundleBase.message
import org.jetbrains.annotations.Nls
import org.jetbrains.annotations.NonNls
import org.jetbrains.annotations.PropertyKey
import java.util.*
import java.util.function.Supplier

object JsonBundle {
    const val BUNDLE = "messages.JsonExtBundle"
    private val INSTANCE: ResourceBundle = DynamicBundle.getBundle(
        BUNDLE, JsonBundle::class.java
    )

    @JvmStatic
    fun message(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): @Nls String {
        return message(INSTANCE, key, *params)
    }

    fun messagePointer(key: @PropertyKey(resourceBundle = BUNDLE) String, vararg params: Any): Supplier<String> {
        val actualParams: Array<out Any> =
            if (params.isEmpty()) {
                ArrayUtil.EMPTY_OBJECT_ARRAY
            } else {
                params
            }
        return Supplier { message(key, *actualParams) }
    }
}