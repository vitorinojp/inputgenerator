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
    var count: Long

    /**
     * Initialization for configurations, components and metrics
     * @param configs an optional list of extra configurations, that may be ignored
     */
    fun init(configs: Map<String, String> = HashMap())

    /**
     * Helper for execution with delay. Defines a suspend function.
     * @param burstSize the number of read to include in a step (burst size). Defaults to 1
     * @param delay the delay between steps. Defaults to 1.seconds
     * @param wait whether to apply the delay or not.  Defaults to true
     * @param max value for the sequence, if null interpreted as infinite. Defaults to null
     * @return [Job] (coroutine) that executes the steps
     */
    suspend fun run(burstSize: Int = 1, delay: Duration = 1.seconds, wait: Boolean = true, max: Long? = null): Job

    /**
     * The suspend function that executes a single step. Reading, transforming and writing a given entry
     * @param burstSize the number of read to include in a step (burst size). Defaults to 1
     * @return the value that was last written
     */
    suspend fun step(burstSize: Int = 1, max: Long? = null): exit?

    /**
     * Auxiliary function: checks if count is in range 1..max, for max: Long?
     * @param count the given count to check
     * @param max value for the sequence, if null interpreted as infinite
     */
    fun checkMax(count: Long, max: Long?): Boolean {
        return if (max == null) {
            true
        } else count in 1..max
    }
}


/**
 * Implements [ISequence] for a [DataSource], [DataSink] and [DataTransformer]
 * @property source the [DataSource] that provides values for each step
 * @property sink the [DataSink] that provides a write target for each step
 * @property transformer the [DataTransformer] that connects the source and sink
 */
class Sequence<entry, out exit>(
    private val name: String,
    private val source: DataSource<entry>,
    private val sink: DataSink<exit>,
    private val transformer: DataTransformer<entry, exit>,
    private val metricsRepository: IMetricsRepository
) : ISequence<entry, exit> {
    // Configuration values
    var baseDelay: Duration = 0.milliseconds
    override var count: Long = 1

    override fun init(configs: Map<String, String>) {
        metricsRepository.registerMetricIfAbsent("sequence.${name}.time.duration", DurationMetric())
        this.source.init()
        this.sink.init()
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun run(burstSize: Int, delay: Duration, wait: Boolean, max: Long?): Job = coroutineScope {
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

            while (isActive && checkMax(count, max)) {
                // Wait and mark
                if (wait && !first) {
                    elapsed = mark.elapsedNow()
                    //println("delay: " + delay + ", elapsed: " + elapsed)
                    //println("result: " + (delay - elapsed ))
                    delay(delay - elapsed)
                    mark = timeSource.markNow()
                }

                if ( step(burstSize, max) == null) return@launch // Advance sequence, or end

                if (first) {
                    first = false
                }
            }

            close(null)

            durationMetric?.end()

            println(durationMetric?.getDiff())

            return@launch
        }
    }

    override suspend fun step(burstSize: Int, max: Long?): exit? {
        var last: exit? = null
        for (i in 1..burstSize) {
            if (source.available() && checkMax(count++, max)) {
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
        return last
    }

    private fun close(wait: Long?){
        this.source.close(wait)
        this.transformer.close(wait)
        this.sink.close(wait)
    }

}