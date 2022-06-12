package com.inputgenerator.metrics

import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

interface IMetricsRepository {
    fun getMetrics(filter: Regex = Regex(".*")): Map<String, IMetric>
    fun registerMetricIfAbsent(name: String, startingValue: IMetric)
    fun registerMetric(name: String, startingValue: IMetric)
    fun getCounter(name: String): IMetricCounter?
    fun getRange(name: String): IMetricRange?
    fun getTime(name: String): IMetricTime?
    fun print(filter: Regex = Regex(".*"))
}

object MetricsRepository: IMetricsRepository  {
    private val map: ConcurrentHashMap<String, IMetric> = ConcurrentHashMap()

    override fun getMetrics(filter: Regex): Map<String, IMetric> {
        return map.filter { it->  it.key.matches(filter)}
    }

    override fun registerMetricIfAbsent(name: String, startingValue: IMetric) {
       map.putIfAbsent(name, startingValue)
    }

    override fun registerMetric(name: String, startingValue: IMetric) {
        map[name] = startingValue
    }

    override fun getCounter(name: String): IMetricCounter? {
        val counter = map[name]
        return if(counter is IMetricCounter){
            counter
        } else {
            null
        }
    }

    override fun getRange(name: String): IMetricRange? {
        val range = map[name]
        return if(range is IMetricRange){
            range
        } else {
            null
        }
    }

    override fun getTime(name: String): IMetricTime? {
        val time = map[name]
        return if(time is IMetricTime){
            time
        } else {
            null
        }
    }

    override fun print(filter: Regex) {
        println("Metrics Count: ${map.size}")
        map.forEach { (t, u) -> println("${t}: $u") }
    }

}

