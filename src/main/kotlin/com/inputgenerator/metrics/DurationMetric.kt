package com.inputgenerator.metrics

import kotlin.time.ExperimentalTime
import kotlin.time.Duration
import kotlin.time.TimeMark
import kotlin.time.TimeSource

@OptIn(ExperimentalTime::class)
class DurationMetric: IMetricTime{
    private val timeSource: TimeSource = TimeSource.Monotonic
    private var begin: TimeMark? = null
    private var end: TimeMark? = null
    private var diff: Duration? = null

    override fun begin(): TimeMark {
        begin = timeSource.markNow()
        return this.begin!!
    }

    override fun end(): TimeMark? {
        if(begin == null)
            begin
        diff = begin?.elapsedNow()
        diff?.let { diff -> end = begin!! + diff }
        return this.end!!
    }

    override fun getMarks(): Pair<TimeMark?, TimeMark?> {
        return Pair(begin, end)
    }

    override fun getDiff(): Duration? {
        if (diff == null)
            end()
        return diff
    }

    override fun toString(): String {
        return getDiff() ?. toString() ?: "null"
    }
}