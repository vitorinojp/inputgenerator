package com.inputgenerator.sources

import com.inputgenerator.metrics.AtomicLongMetric
import com.inputgenerator.metrics.AtomicLongMinMaxMetric
import com.inputgenerator.metrics.IMetricCounter
import com.inputgenerator.metrics.MetricsRepository

interface DataSource<V> {
    fun get(): V?
    fun available(): Boolean
    fun getDescription(): String
    fun init(configs: Map<String, String> = HashMap())
    fun close(wait: Long?)
}

abstract class BaseDataSource<V>(
    sequenceName: String,
    sourceName: String,
    var metricsRepository: MetricsRepository,
    var sourceId: String = "${sequenceName}.${sourceName}"
) : DataSource<V> {
    protected var readMetric: IMetricCounter? = null
    protected var failsMetric: IMetricCounter? = null
    protected var bytesMetric: IMetricCounter? = null
    protected var bytesMinMaxMetric: AtomicLongMinMaxMetric? = null

    override fun init(configs: Map<String, String>) {
        metricsRepository.registerMetricIfAbsent("sequence.${sourceId}.reads", AtomicLongMetric())
        metricsRepository.registerMetricIfAbsent("sequence.${sourceId}.fails", AtomicLongMetric())
        metricsRepository.registerMetricIfAbsent("sequence.${sourceId}.bytes.total", AtomicLongMetric())
        metricsRepository.registerMetricIfAbsent("sequence.${sourceId}.bytes.minMax", AtomicLongMinMaxMetric())

        this.readMetric = metricsRepository.getCounter("sequence.${sourceId}.reads")
        this.failsMetric = metricsRepository.getCounter("sequence.${sourceId}.fails")
        this.bytesMetric = metricsRepository.getCounter("sequence.${sourceId}.bytes.total")
        this.bytesMinMaxMetric = metricsRepository.getMinMax("sequence.${sourceId}.bytes.minMax")
    }

    override fun close(wait: Long?) {

    }
}

