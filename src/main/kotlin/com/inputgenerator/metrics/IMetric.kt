package com.inputgenerator.metrics

import kotlin.time.ExperimentalTime
import java.io.Serializable
import kotlin.time.Duration
import kotlin.time.TimeMark

interface IMetric: Serializable {
    override fun toString(): String
}

interface IMetricCounter: IMetric {
    fun getValue(): Long
    fun addToValue(add: Long): Long
    fun subFromValue(sub: Long): Long
    fun incValue(): Long
    fun decValue(): Long
}

interface IMetricRange: IMetric {
    fun init(init: Long): Long
    fun last(init: Long): Long
    fun get(): Pair<Long, Long>
    fun diff(): Long
}

@OptIn(ExperimentalTime::class)
interface IMetricTime: IMetric {
    fun begin(): TimeMark
    fun end(): TimeMark?
    fun getMarks(): Pair<TimeMark?, TimeMark?>
    fun getDiff(): Duration?
}