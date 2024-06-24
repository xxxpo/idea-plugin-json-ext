// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.psi.impl

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.LiteralTextEscaper
import com.intellij.psi.PsiLanguageInjectionHost
import java.util.stream.IntStream
import kotlin.math.min

abstract class JSStringLiteralEscaper<T : PsiLanguageInjectionHost>(host: T) : LiteralTextEscaper<T>(host) {
    private var mySourceOffsets: SourceOffsets? = null

    override fun decode(rangeInsideHost: TextRange, outChars: StringBuilder): Boolean {
        val subText = rangeInsideHost.substring(myHost!!.text)

        val sourceOffsets = SourceOffsets()
        val result = parseStringCharacters(subText, outChars, sourceOffsets, isRegExpLiteral, !isOneLine)
        mySourceOffsets = sourceOffsets
        return result
    }

    protected abstract val isRegExpLiteral: Boolean
        get

    override fun getOffsetInHost(offsetInDecoded: Int, rangeInsideHost: TextRange): Int {
        val result = mySourceOffsets!!.getOffsetInHost(offsetInDecoded)
        if (result == -1) return -1
        return (min(result.toDouble(), rangeInsideHost.length.toDouble()) + rangeInsideHost.startOffset).toInt()
    }

    override fun isOneLine(): Boolean {
        return true
    }

    class SourceOffsets {
        /**
         * Offset in injected string -> offset in host string
         * Last element contains imaginary offset for the character after the last one in injected string. It would be host string length.
         * E.g. for "aa\nbb" it is [0,1,2,4,5,6]
         */
        lateinit var sourceOffsets: IntArray

        /**
         * Optimization for the case when all offsets in injected and host strings are the same.
         */
        var lengthIfNoShifts: Int = -1

        fun toOffsetArray(): IntArray {
            val offsets = sourceOffsets
            return offsets ?: IntStream.range(0, lengthIfNoShifts + 1).toArray()
        }

        fun getOffsetInHost(offsetInDecoded: Int): Int {
            return if (lengthIfNoShifts >= 0) {
                if (offsetInDecoded <= lengthIfNoShifts) offsetInDecoded else -1
            } else {
                if (offsetInDecoded < sourceOffsets.size) sourceOffsets[offsetInDecoded] else -1
            }
        }
    }

    companion object {
        fun parseStringCharacters(
            chars: String,
            outChars: StringBuilder,
            outSourceOffsets: SourceOffsets?,
            regExp: Boolean,
            escapeBacktick: Boolean
        ): Boolean {
            if (chars.indexOf('\\') < 0) {
                outChars.append(chars)
                if (outSourceOffsets != null) {
                    outSourceOffsets.lengthIfNoShifts = chars.length
                }
                return true
            }

            val sourceOffsets = IntArray(chars.length + 1)
            var index = 0
            val outOffset = outChars.length
            var result = true
            var iteration = 0
            loop@ while (index < chars.length) {
                if (iteration++ % 1000 == 0) ProgressManager.checkCanceled()
                var c = chars[index++]

                sourceOffsets[outChars.length - outOffset] = index - 1
                sourceOffsets[outChars.length + 1 - outOffset] = index

                if (c != '\\') {
                    outChars.append(c)
                    continue
                }
                if (index == chars.length) {
                    result = false
                    break
                }
                c = chars[index++]
                if (escapeBacktick && c == '`') {
                    outChars.append(c)
                } else if (regExp) {
                    if (c != '/') {
                        outChars.append('\\')
                    }
                    outChars.append(c)
                } else {
                    when (c) {
                        'b' -> outChars.append('\b')
                        't' -> outChars.append('\t')
                        'n', '\n' -> outChars.append('\n')
                        'f' -> outChars.append('\u000c')
                        'r' -> outChars.append('\r')
                        '"' -> outChars.append('"')
                        '/' -> outChars.append('/')
                        '\'' -> outChars.append('\'')
                        '\\' -> outChars.append('\\')
                        '0', '1', '2', '3', '4', '5', '6', '7' -> {
                            val startC = c
                            var v = c.code - '0'.code
                            if (index < chars.length) {
                                c = chars[index++]
                                if ('0' <= c && c <= '7') {
                                    v = v shl 3
                                    v += c.code - '0'.code
                                    if (startC <= '3' && index < chars.length) {
                                        c = chars[index++]
                                        if ('0' <= c && c <= '7') {
                                            v = v shl 3
                                            v += c.code - '0'.code
                                        } else {
                                            index--
                                        }
                                    }
                                } else {
                                    index--
                                }
                            }
                            outChars.append(v.toChar())
                        }

                        'x' -> {
                            if (index + 2 <= chars.length) {
                                try {
                                    val v = Integer.parseInt(chars, index, index + 2, 16)
                                    outChars.append(v.toChar())
                                    index += 2
                                } catch (e: Exception) {
                                    result = false
                                    break@loop
                                }
                            } else {
                                result = false
                                break@loop
                            }
                        }

                        'u' -> {
                            if (index + 3 <= chars.length && chars[index] == '{') {
                                val end = chars.indexOf('}', index + 1)
                                if (end < 0) {
                                    result = false
                                    break@loop
                                }
                                try {
                                    val v = Integer.parseInt(chars, index + 1, end, 16)
                                    c = chars[index + 1]
                                    if (c == '+' || c == '-') {
                                        result = false
                                        break@loop
                                    }
                                    outChars.appendCodePoint(v)
                                    index = end + 1
                                } catch (e: Exception) {
                                    result = false
                                    break@loop
                                }
                            } else if (index + 4 <= chars.length) {
                                try {
                                    val v = Integer.parseInt(chars, index, index + 4, 16)
                                    c = chars[index]
                                    if (c == '+' || c == '-') {
                                        result = false
                                        break@loop
                                    }
                                    outChars.append(v.toChar())
                                    index += 4
                                } catch (e: Exception) {
                                    result = false
                                    break@loop
                                }
                            } else {
                                result = false
                                break@loop
                            }
                        }

                        else -> outChars.append(c)
                    }
                }

                sourceOffsets[outChars.length - outOffset] = index
            }

            sourceOffsets[outChars.length - outOffset] = chars.length

            if (outSourceOffsets != null) {
                outSourceOffsets.sourceOffsets = sourceOffsets.copyOf(outChars.length - outOffset + 1)
            }
            return result
        }
    }
}
