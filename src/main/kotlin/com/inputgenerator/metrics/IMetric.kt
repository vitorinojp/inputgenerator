package com.inputgenerator.metrics

import java.io.Serializable
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark

interface IMetric : Serializable {
    override fun toString(): String
    fun getRate(duration: Duration?): String? = null
}

interface IMetricCounter : IMetric {
    fun getValue(): Long
    fun addToValue(add: Long): Long
    fun subFromValue(sub: Long): Long
    fun incValue(): Long
    fun decValue(): Long
    override fun getRate(duration: Duration?): String? {
        var seconds: Double? = duration?.let { duration.toDouble(DurationUnit.SECONDS) }

        return if (seconds == null){
            null
        }
        else {
            (getValue()/seconds).toString()
        }
    }
}

interface IMetricRange : IMetric {
    fun init(init: Long): Long
    fun last(init: Long): Long
    fun get(): Pair<Long, Long>
    fun diff(): Long
}

@OptIn(ExperimentalTime::class)
interface IMetricTime : IMetric {
    fun begin(): TimeMark
    fun end(): TimeMark?
    fun getMarks(): Pair<TimeMark?, TimeMark?>
    fun getDiff(): Duration?
}