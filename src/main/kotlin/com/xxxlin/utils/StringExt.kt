package com.xxxlin.utils

/**
 *
 * @author xiaolin
 * time 2025-04-05 04:51
 */

fun String.contains(vararg chars: Char): Boolean {
    return this.firstOrNull {
        chars.contains(it)
    } != null
}