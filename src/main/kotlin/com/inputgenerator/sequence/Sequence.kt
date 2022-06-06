package com.inputgenerator.sequence

import kotlinx.coroutines.*

import com.inputgenerator.entities.DataEntity
import com.inputgenerator.sinks.DataSink
import com.inputgenerator.sources.DataSource
import com.inputgenerator.transform.DataTransformer
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource


/**
 * Defines a sequence of steps to execute.
 * @property source the [DataSource] that provides values for each step
 * @property sink the [DataSink] that provides a write target for each step
 * @property transformer the [DataTransformer] that connects the source and sink
 */
class Sequence <entry, out exit> (
    private val source: DataSource<entry>,
    private val sink: DataSink<exit>,
    private val transformer: DataTransformer<entry, exit>
){
    var baseDelay: Duration = 0.milliseconds

    @OptIn(ExperimentalTime::class)
    /**
     * Helper for execution with delay. Defines a suspend function.
     * @param count the number of read to include in a step (burst size)
     * @param duration the delay between steps
     * @return [Job] (coroutine) that executes the steps
     */
    suspend fun run(count: Int = 1, duration: Duration? = null, wait: Boolean = true): Job = coroutineScope {
        return@coroutineScope launch {
            // Anchoring vars for wait timing
            var elapsed: Duration = 0.seconds
            val timeSource: TimeSource = TimeSource.Monotonic
            var mark: TimeMark = timeSource.markNow()

            while (isActive){
                // Wait and mark
                if(wait){
                    duration?.let { duration -> delay(duration - (elapsed + baseDelay)) }
                    mark = timeSource.markNow()
                }

                if(step(count) == null) return@launch // Advance sequence, or end
                yield() // Yield as needed

                // Calculate delay
                if (wait){
                    elapsed = mark.elapsedNow()
                    //println(elapsed)
                }
            }
        }
    }

    /**
     * The suspend function that executes a single step. Reading, transforming and writing a given entry
     * @param count the number of read to include in a step (burst size)
     * @return the value that was last written
     */
    @OptIn(ExperimentalTime::class)
    suspend fun step(count: Int = 1): exit?{
        var last: exit? = null
        for(i in 1..count){
            if (source.available()){
                val data: entry? = source.get()
                if(data != null){
                    val result: DataEntity<exit>? = transformer.apply(data)
                    if (result != null)
                        last = sink
                            .write(result)
                            .getValue()
                }
                else {
                    return null
                }
            }
        }
        yield()
        return last
    }
}