package com.inputgenerator.sinks

import com.inputgenerator.entities.DataEntity
import com.inputgenerator.metrics.AtomicLongMetric
import com.inputgenerator.metrics.IMetricCounter
import com.inputgenerator.metrics.MetricsRepository

interface DataSink<V> {
    fun write(data: DataEntity<V>): DataEntity<V>
    fun getDescription(): String?
    fun init(configs: Map<String, String> = HashMap())
    fun close(wait: Long?)
}

abstract class BaseDataSink<V>(
    sequenceName: String,
    sinkName: String,
    var metricsRepository: MetricsRepository,
    var sinkId: String = "${sequenceName}.${sinkName}"
) : DataSink<V> {
    protected var writeMetric: IMetricCounter? = null
    protected var failsMetric: IMetricCounter? = null
    protected var triesMetric: IMetricCounter? = null

    override fun init(configs: Map<String, String>) {
        metricsRepository.registerMetricIfAbsent("sequence.${sinkId}.tries", AtomicLongMetric())
        metricsRepository.registerMetricIfAbsent("sequence.${sinkId}.writes", AtomicLongMetric())
        metricsRepository.registerMetricIfAbsent("sequence.${sinkId}.fails", AtomicLongMetric())

        this.triesMetric = metricsRepository.getCounter("sequence.${sinkId}.tries")
        this.writeMetric = metricsRepository.getCounter("sequence.${sinkId}.writes")
        this.failsMetric = metricsRepository.getCounter("sequence.${sinkId}.fails")
    }
}

class DummySink<V>(
    sequenceName: String,
    metricsRepository: MetricsRepository
) : BaseDataSink<V>(sequenceName, "dummySink", metricsRepository) {
    override fun write(data: DataEntity<V>): DataEntity<V> {
        return data
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }

    override fun close(wait: Long?) {

    }
}

abstract class TopicBasedSink<V>(
    sequenceName: String,
    sinkName: String,
    metricsRepository: MetricsRepository,
    open var map: String? = null
) : BaseDataSink<V>(sequenceName, sinkName, metricsRepository) {
    override fun getDescription(): String? {
        return this.javaClass.name
    }

    fun mapTopic(topic: String, map: String?): String{
        var splits = map?.split(",")

        // Switch based on type of mapping in start of split
        return when(splits?.get(0)){
            "int" -> {
                // random int between split[1] and split[2]
                val id = (Math.random() * (splits[2].toInt() - splits[1].toInt())).toInt() + splits[1].toInt()
                topic.replace("{}", id.toString())
            }
            else -> {
                topic
            }
        }
    }

}