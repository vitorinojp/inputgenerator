package com.inputgenerator

import com.inputgenerator.sinks.MqttSink
import com.inputgenerator.sinks.StringToMqttMessageTransformer
import com.inputgenerator.transform.DataTransformer
import com.inputgenerator.configurations.*
import com.inputgenerator.metrics.MetricsRepository
import com.inputgenerator.sequence.Sequence
import com.inputgenerator.sources.DataSource
import com.inputgenerator.sources.TTNMeasureSource
import kotlinx.coroutines.*
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.util.*
import kotlin.system.exitProcess
import kotlin.time.Duration.Companion.milliseconds

@SpringBootApplication
class Ferrovia40InputGeneratorApplication(val configuration: Configuration): ApplicationRunner {
    private var metricsRepository: MetricsRepository = MetricsRepository
    @OptIn(DelicateCoroutinesApi::class)
    override fun run(args: ApplicationArguments?) {
        // Componentes
        val dataSource: DataSource<String>
        val dataTransformer: DataTransformer<String, String>

        parseOptions(configuration, args)

        // Sinks
        val sinkMap: MutableMap<String, String> = HashMap()
        configuration.host.let { sinkMap.put(HOST_NAME, it) }
        configuration.port.let { sinkMap.put(HOST_PORT, it) }
        configuration.password?.let { sinkMap.put(HOST_PASSWORD, it) }
        configuration.topic.let { sinkMap.put(HOST_TOPIC, it) }

        dataSource = TTNMeasureSource("main")
        val dataSink: MqttSink = MqttSink(
            "main",
            MQTT_PUBLISHER_ID = "InputGenerator",
            MQTT_SERVER_ADDRES = "tcp://" + configuration.host + ":" + configuration.port,
            MQTT_SERVER_PASSWORD = configuration.password,
            MQTT_SERVER_USERNAME = configuration.username,
            MQTT_SERVER_TOPIC = configuration.topic,
            MQTT_MESSAGE_QOS = configuration.qos
        )
        dataTransformer = StringToMqttMessageTransformer()

        val sequence: Sequence<String, String> =
            Sequence(
                "main",
                dataSource,
                dataSink,
                dataTransformer,
                metricsRepository
            )
        sequence.baseDelay = configuration.baseDelay.milliseconds

        println("\nSequence built. ")
        println("Starting sequence with: ")
        println("   dataSource: " + dataSource.getDescription())
        println("   dataSink: " + dataSink.getDescription())
        println("   dataTransformer: " + dataTransformer.getDescription())
        println("   burst: " + configuration.burst )
        println("   rate: \n      targetRate: " + configuration.rate
                + " \n      wait: " + configuration.wait
                + if (configuration.wait) " \n      timeDelay: " + configuration.delayDuration else ""
        )

        val job: Job = GlobalScope.launch(Dispatchers.Default) {
            sequence.init()
            sequence.run(
                configuration.burst,
                configuration.delayDuration,
                configuration.wait
            )
        }
        while (true){
            when(readln().trim()){
                "q", "Q", "quit" -> {
                    println("Closing sequence...")
                    job.cancel()
                    println("Closed. Printing metrics")
                    metricsRepository.print()
                    println("Quitting...")
                    exitProcess(0)
                }
                else -> println("Input not recognized. Type 'q' to quit")
            }
            if (job.isCompleted) exitProcess(0)
        }
    }

}

fun main(args: Array<String>) {
    runApplication<Ferrovia40InputGeneratorApplication>(*args)
}

/**
 * Read out options and parse them to the configuration object.
 * @param configuration the [Configuration] object to be loaded with configs, may have default values
 * @param args the list of args to parse. Currently a [ApplicationArguments] object from Spring.
 */
fun parseOptions(configuration: Configuration, args: ApplicationArguments?){
    val opts = args?.optionNames
    opts?.forEach { name ->
        when (name) {
            "file" -> configuration.filePath = args.getOptionValues("file")[0]
            "input" -> configuration.dataSource = args.getOptionValues("input")[0]
            "output" -> configuration.dataSink = args.getOptionValues("output")[0]
            "transformer" -> configuration.dataTransformer = args.getOptionValues("transformer")[0]
            "host" -> configuration.host = args.getOptionValues("host")[0]
            "port" -> configuration.port = args.getOptionValues("port")[0]
            "password" -> configuration.password = args.getOptionValues("password")[0]
            "username" -> configuration.username = args.getOptionValues("username")[0]
            "topic" -> configuration.topic = args.getOptionValues("topic")[0]
            "burst" -> configuration.burst =  args.getOptionValues("burst")[0].toInt()
            "rate" -> configuration.rate = args.getOptionValues("rate")[0].toInt()
            "basedelay" -> configuration.baseDelay = args.getOptionValues("basedelay")[0].toInt()
            "wait" -> configuration.wait = args.getOptionValues("wait")[0].toBoolean()
            "restart" -> configuration.restart = args.getOptionValues("restart")[0].toBoolean()
            "qos" -> configuration.qos = args.getOptionValues("qos")[0].toInt()
        }
    }
}

