package com.inputgenerator.sinks

import com.inputgenerator.entities.DataEntity
import com.inputgenerator.metrics.AtomicLongMetric
import com.inputgenerator.metrics.IMetricCounter
import com.inputgenerator.metrics.MetricsRepository

interface DataSink <V> {
    fun write(data: DataEntity<V>): DataEntity<V>
    fun getDescription(): String?
    fun init(configs: Map<String, String> = HashMap())
}

abstract class BaseDataSink<V> (
    sequenceName: String,
    sinkName: String,
    var metricsRepository: MetricsRepository,
    var sinkId: String = "${sequenceName}.${sinkName}"
): DataSink<V>{
    protected var writeMetric: IMetricCounter? = null
    protected var failsMetric: IMetricCounter? = null

    override fun init(configs: Map<String, String>) {
        metricsRepository.registerMetricIfAbsent("sequence.sink.${sinkId}.writes", AtomicLongMetric())
        metricsRepository.registerMetricIfAbsent("sequence.sink.${sinkId}.fails", AtomicLongMetric())

        this.writeMetric = metricsRepository.getCounter("sequence.sink.${sinkId}.writes")
        this.failsMetric = metricsRepository.getCounter("sequence.sink.${sinkId}.fails")
    }
}

class DummySink <V> (
    sequenceName: String,
    metricsRepository: MetricsRepository
): BaseDataSink<V>(sequenceName, "dummySink", metricsRepository) {
    override fun write(data: DataEntity<V>): DataEntity<V> {
        return data
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}