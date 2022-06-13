package com.inputgenerator.metrics

import java.util.concurrent.atomic.AtomicLong
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

class AtomicLongMetric(
    value: Long = 0,
    private var value_: AtomicLong = AtomicLong(value)
) : IMetricCounter {

    override fun getValue(): Long {
        return value_.toLong()
    }

    override fun addToValue(add: Long): Long {
        return value_.addAndGet(add)
    }

    override fun subFromValue(sub: Long): Long {
        return value_.addAndGet(-sub)
    }

    override fun incValue(): Long {
        return value_.incrementAndGet()
    }

    override fun decValue(): Long {
        return value_.decrementAndGet()
    }

    override fun toString(): String {
        return getValue().toString()
    }
}