package com.inputgenerator.transform

import com.inputgenerator.entities.BaseEntity
import com.inputgenerator.entities.DataEntity
import com.inputgenerator.metrics.AtomicLongMetric
import com.inputgenerator.metrics.IMetricCounter
import com.inputgenerator.metrics.MetricsRepository

interface DataTransformer<in A, B> {
    fun apply(a: A): DataEntity<B>?
    fun getDescription(): String?
    fun close(wait: Long?)
    fun init(configs: Map<String, String>)
}

abstract class BaseDataTransformer<in A, B>(
    sequenceName: String,
    transformName: String,
    var metricsRepository: MetricsRepository,
    var transformId: String = "${sequenceName}.${transformName}"
): DataTransformer<A, B>{
    protected var inMetric: IMetricCounter? = null
    protected var outMetric: IMetricCounter? = null

    override fun init(configs: Map<String, String>) {
        metricsRepository.registerMetricIfAbsent("sequence.${transformId}.in", AtomicLongMetric())
        metricsRepository.registerMetricIfAbsent("sequence.${transformId}.out", AtomicLongMetric())

        this.inMetric = metricsRepository.getCounter("sequence.${transformId}.in")
        this.outMetric = metricsRepository.getCounter("sequence.${transformId}.out")
    }
    override fun close(wait: Long?){

    }
}

class PassAllTransformer<A>(
    sequenceName: String,
    transformName: String = "passAllTransformer",
    metricsRepository: MetricsRepository = MetricsRepository
) : BaseDataTransformer<A, A>(sequenceName ,transformName, metricsRepository) {
    override fun apply(a: A): DataEntity<A>? {
        return BaseEntity(a)
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}

class DummyTransformer(
    sequenceName: String,
    transformName: String = "dummyTransformer",
    metricsRepository: MetricsRepository = MetricsRepository
): BaseDataTransformer<Any, Unit>(sequenceName ,transformName, metricsRepository) {
    override fun apply(a: Any): BaseEntity<Unit>? {
        return null
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}

class StringTransformer(
    sequenceName: String,
    transformName: String = "stringTransformer",
    metricsRepository: MetricsRepository = MetricsRepository
): BaseDataTransformer<String, String>(sequenceName ,transformName, metricsRepository) {
    override fun apply(a: String): BaseEntity<String>? {
        return BaseEntity(a)
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}