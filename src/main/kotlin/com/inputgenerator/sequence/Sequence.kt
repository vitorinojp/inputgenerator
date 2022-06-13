package com.inputgenerator.sequence

import com.inputgenerator.entities.DataEntity
import com.inputgenerator.metrics.DurationMetric
import com.inputgenerator.metrics.IMetricsRepository
import com.inputgenerator.sinks.DataSink
import com.inputgenerator.sources.DataSource
import com.inputgenerator.transform.DataTransformer
import kotlinx.coroutines.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource


/**
 * Defines a sequence of steps to execute.
 */
interface ISequence<entry, out exit> {
    /**
     * Initialization for configurations, components and metrics
     */
    fun init(configs: Map<String, String> = HashMap())

    /**
     * Helper for execution with delay. Defines a suspend function.
     * @param count the number of read to include in a step (burst size)
     * @param duration the delay between steps
     * @return [Job] (coroutine) that executes the steps
     */
    suspend fun run(count: Int = 1, duration: Duration = 1.seconds, wait: Boolean = true): Job

    /**
     * The suspend function that executes a single step. Reading, transforming and writing a given entry
     * @param count the number of read to include in a step (burst size)
     * @return the value that was last written
     */
    suspend fun step(count: Int = 1): exit?
}


/**
 * Implements [ISequence] for a [DataSource], [DataSink] and [DataTransformer]
 * @property source the [DataSource] that provides values for each step
 * @property sink the [DataSink] that provides a write target for each step
 * @property transformer the [DataTransformer] that connects the source and sink
 */
@OptIn(ExperimentalTime::class)
class Sequence<entry, out exit>(
    private val name: String,
    private val source: DataSource<entry>,
    private val sink: DataSink<exit>,
    private val transformer: DataTransformer<entry, exit>,
    private val metricsRepository: IMetricsRepository
) : ISequence<entry, exit> {
    // Configuration values
    var baseDelay: Duration = 0.milliseconds

    override fun init(configs: Map<String, String>) {
        metricsRepository.registerMetricIfAbsent("sequence.${name}.time.duration", DurationMetric())
        this.source.init()
        this.sink.init()
    }

    override suspend fun run(count: Int, duration: Duration, wait: Boolean): Job = coroutineScope {
        // ** Timing //
        var durationMetric = metricsRepository.getTime("sequence.${name}.time.duration")
        // * Anchoring vars for wait timing * //
        var elapsed: Duration = 0.seconds
        val timeSource: TimeSource = TimeSource.Monotonic
        var first: Boolean = true

        return@coroutineScope launch {
            // Mark timers
            var mark: TimeMark = timeSource.markNow()
            durationMetric?.begin()

            while (isActive) {
                // Wait and mark
                if (wait && !first) {
                    delay(duration - (elapsed + baseDelay))
                    mark = timeSource.markNow()
                }

                if (step(count) == null) return@launch // Advance sequence, or end
                yield() // Yield as needed

                // Calculate delay
                if (wait && !first) {
                    elapsed = mark.elapsedNow()
                    //println(elapsed)
                }

                if (first) {
                    first = false
                }
            }

            durationMetric?.end()
        }
    }

    override suspend fun step(count: Int): exit? {
        var last: exit? = null
        for (i in 1..count) {
            if (source.available()) {
                val data: entry? = source.get()
                if (data != null) {
                    val result: DataEntity<exit>? = transformer.apply(data)
                    if (result != null)
                        last = sink
                            .write(result)
                            .getValue()
                } else {
                    return null
                }
            }
        }
        yield()
        return last
    }

}