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

class AtomicLongMinMaxMetric (
    private var min: Long = 0,
    private var max: Long = 0,
    var _min: AtomicLong = AtomicLong(min),
    var _max: AtomicLong = AtomicLong(max)
): IMetricRange {
    override fun init(init: Long): Long {
        synchronized(this){
            if (_max.get() == max){
                _max.set(init)
            }
             _min.set(init)
            return _min.get()
        }
    }

    override fun last(last: Long): Long {
        synchronized(this){
            if (_min.get() == min){
                _min.set(last)
            }
            _max.set(last)
            return _max.get()
        }
    }

    override fun get(): Pair<Long, Long> {
        synchronized(this) {
            return Pair(_min.get(), _max.get())
        }
    }

    override fun diff(): Long {
        synchronized(this) {
            return _max.get() - _min.get()
        }
    }

    override fun toString(): String {
        synchronized(this) {
            return "min: $_min <=> max: $_max"
        }
    }

    fun insert(new: Long): AtomicLongMinMaxMetric{
        synchronized(this){
            if (_max.get() < new)
                last(new)
            if (_min.get() > new)
                init(new)
        }
        return this
    }

}