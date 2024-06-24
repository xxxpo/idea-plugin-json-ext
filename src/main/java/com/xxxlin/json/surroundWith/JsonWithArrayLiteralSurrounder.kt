// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.surroundWith

import com.xxxlin.json.JsonBundle.message

class JsonWithArrayLiteralSurrounder : JsonSurrounderBase() {
    override fun getTemplateDescription(): String {
        return message("surround.with.array.literal.desc")
    }

    override fun createReplacementText(textInRange: String): String {
        return "[$textInRange]"
    }
}
