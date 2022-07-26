package com.inputgenerator.metrics

import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration

interface IMetricsRepository {
    fun getMetrics(filter: Regex = Regex(".*")): Map<String, IMetric>
    fun registerMetricIfAbsent(name: String, startingValue: IMetric)
    fun registerMetric(name: String, startingValue: IMetric)
    fun getCounter(name: String): IMetricCounter?
    fun getRange(name: String): IMetricRange?
    fun getMinMax(name: String): AtomicLongMinMaxMetric?
    fun getTime(name: String): IMetricTime?
    fun print(filter: Regex = Regex(".*"))
}

object MetricsRepository : IMetricsRepository {
    private val map: ConcurrentHashMap<String, IMetric> = ConcurrentHashMap()

    override fun getMetrics(filter: Regex): Map<String, IMetric> {
        return map.filter { it -> it.key.matches(filter) }
    }

    override fun registerMetricIfAbsent(name: String, startingValue: IMetric) {
        map.putIfAbsent(name, startingValue)
    }

    override fun registerMetric(name: String, startingValue: IMetric) {
        map[name] = startingValue
    }

    override fun getCounter(name: String): IMetricCounter? {
        val counter = map[name]
        return if (counter is IMetricCounter) {
            counter
        } else {
            null
        }
    }

    override fun getRange(name: String): IMetricRange? {
        val range = map[name]
        return if (range is IMetricRange) {
            range
        } else {
            null
        }
    }

    override fun getMinMax(name: String): AtomicLongMinMaxMetric? {
        val range = map[name]
        return if (range is AtomicLongMinMaxMetric) {
            range
        } else {
            null
        }
    }

    override fun getTime(name: String): IMetricTime? {
        val time = map[name]
        return if (time is IMetricTime) {
            time
        } else {
            null
        }
    }

    override fun print(filter: Regex) {
        println("Metrics Count: ${map.size}")
        map
            .filter { filter.matches(it.key) }
            .forEach { (metricKey, metricValue) ->
                println("${metricKey}: $metricValue")
                val name = extractSequenceNameFromKey(metricKey)
                val duration: Duration? = this.getTime("sequence.${name}.time.duration")?.getDiff()
                duration?.also {
                    metricValue.getRate(duration)
                        ?.let {
                            println("${metricKey}.rate: $it")
                        }
                }
            }
    }

    private fun extractSequenceNameFromKey(key: String, separator: String = "."): String {
        try {
            return key.split(".")[1]
        } catch (e: java.lang.Exception) {
            println("ERROR: Metric key probably malformed")
            println(e.stackTrace)
        }
        return ""
    }
}

