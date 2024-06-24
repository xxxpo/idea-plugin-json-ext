// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.xxxlin.json.pointer

import com.intellij.util.containers.ContainerUtil
import com.jetbrains.jsonSchema.JsonPointerUtil
import java.util.stream.Collectors

class JsonPointerPosition {
    constructor() {
        this.steps = ArrayList()
    }

    private constructor(steps: MutableList<Step>) {
        this.steps = steps
    }

    fun getSteps(): List<Step> {
        return steps
    }

    private val steps: MutableList<Step>

    fun addPrecedingStep(value: Int) {
        steps.add(0, Step.createArrayElementStep(value))
    }

    fun addFollowingStep(value: Int) {
        steps.add(Step.createArrayElementStep(value))
    }

    fun addPrecedingStep(value: String) {
        steps.add(0, Step.createPropertyStep(value))
    }

    fun addFollowingStep(value: String) {
        steps.add(Step.createPropertyStep(value))
    }

    fun replaceStep(pos: Int, value: Int) {
        steps[pos] = Step.createArrayElementStep(value)
    }

    fun replaceStep(pos: Int, value: String) {
        steps[pos] = Step.createPropertyStep(value)
    }

    val isEmpty: Boolean
        get() = steps.isEmpty()

    fun isArray(pos: Int): Boolean {
        return checkPosInRange(pos) && steps[pos].isFromArray
    }

    fun isObject(pos: Int): Boolean {
        return checkPosInRange(pos) && steps[pos].isFromObject
    }

    val stepNames: List<String?>
        get() = ContainerUtil.map(steps) { s: Step -> s.name }

    fun skip(count: Int): JsonPointerPosition? {
        return if (checkPosInRangeIncl(count)) JsonPointerPosition(steps.subList(count, steps.size)) else null
    }

    fun trimTail(count: Int): JsonPointerPosition? {
        return if (checkPosInRangeIncl(count)) JsonPointerPosition(steps.subList(0, steps.size - count)) else null
    }

    val lastName: String?
        get() {
            val last = ContainerUtil.getLastItem(steps)
            return last?.name
        }

    val firstName: String?
        get() {
            val last = ContainerUtil.getFirstItem(steps)
            return last?.name
        }

    val firstIndex: Int
        get() {
            val last = ContainerUtil.getFirstItem(steps)
            return last?.idx ?: -1
        }

    fun size(): Int {
        return steps.size
    }

    fun updateFrom(from: JsonPointerPosition) {
        steps.clear()
        steps.addAll(from.steps)
    }

    fun toJsonPointer(): String {
        return "/" + steps.stream().map { step: Step ->
            JsonPointerUtil.escapeForJsonPointer(
                step.name ?: step.idx.toString()
            )
        }.collect(Collectors.joining("/"))
    }

    override fun toString(): String {
        return steps.stream().map { obj: Step -> obj.toString() }
            .collect(Collectors.joining("->", "steps: <", ">"))
    }

    private fun checkPosInRange(pos: Int): Boolean {
        return steps.size > pos
    }

    private fun checkPosInRangeIncl(pos: Int): Boolean {
        return steps.size >= pos
    }

    class Step private constructor(val name: String?, val idx: Int) {
        val isFromObject: Boolean
            get() = name != null

        val isFromArray: Boolean
            get() = name == null

        override fun toString(): String {
            var format = "?%s"
            if (name != null) format = "{%s}"
            if (idx >= 0) format = "[%s]"
            return String.format(format, name ?: if (idx >= 0) idx.toString() else "null")
        }

        companion object {
            fun createPropertyStep(name: String): Step {
                return Step(name, -1)
            }

            fun createArrayElementStep(idx: Int): Step {
                assert(idx >= 0)
                return Step(null, idx)
            }
        }
    }

    companion object {
        fun createSingleProperty(property: String): JsonPointerPosition {
            return JsonPointerPosition(ContainerUtil.createMaybeSingletonList(Step.createPropertyStep(property)))
        }

        @JvmStatic
        fun parsePointer(pointer: String): JsonPointerPosition {
            val chain = JsonPointerUtil.split(JsonPointerUtil.normalizeSlashes(JsonPointerUtil.normalizeId(pointer)))
            val steps: MutableList<Step> = ArrayList(chain.size)
            for (s in chain) {
                try {
                    steps.add(Step.createArrayElementStep(s.toInt()))
                } catch (e: NumberFormatException) {
                    steps.add(Step.createPropertyStep(JsonPointerUtil.unescapeJsonPointerPart(s)))
                }
            }
            return JsonPointerPosition(steps)
        }
    }
}
