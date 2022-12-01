package com.inputgenerator.sinks

import com.inputgenerator.entities.BaseEntity
import com.inputgenerator.entities.DataEntity
import com.inputgenerator.metrics.MetricsRepository
import com.inputgenerator.transform.BaseDataTransformer
import com.inputgenerator.transform.DataTransformer
import org.eclipse.paho.mqttv5.client.IMqttClient
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.common.MqttException
import org.eclipse.paho.mqttv5.common.MqttPersistenceException


class MqttSink(
    sequenceName: String,
    sinkName: String = "mqttSink",
    sinkCount: String = "0",
    private val MQTT_PUBLISHER_ID: String,
    private val MQTT_SERVER_ADDRES: String = "tcp://127.0.0.1:1883",
    private val MQTT_SERVER_PASSWORD: String? = null,
    private val MQTT_SERVER_USERNAME: String? = null,
    private val MQTT_SERVER_TOPIC: String,
    private val MQTT_MESSAGE_QOS: Int = 0,
    override var map: String? = null
) : TopicBasedSink<String>(sequenceName, "${sinkName}-${sinkCount}", MetricsRepository) {
    private var client: IMqttClient? = this.getclient()

    override fun write(data: DataEntity<String>): DataEntity<String> {
        this.triesMetric?.incValue()
        val mqttMessage: org.eclipse.paho.mqttv5.common.MqttMessage =
            org.eclipse.paho.mqttv5.common.MqttMessage(
                data
                    .getValue()
                    .encodeToByteArray()
            )
        mqttMessage.qos = MQTT_MESSAGE_QOS
        try {
            val topic = this.mapTopic(MQTT_SERVER_TOPIC, map)
            System.out.println("Publishing to topic: $topic")
            client?.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            this.failsMetric?.incValue()
            throw  e
        } catch (e: MqttPersistenceException) {
            this.failsMetric?.incValue()
            throw  e
        }


        this.writeMetric?.incValue()

        //System.out.println("Mqtt sent: " + data.getValue())

        return data
    }

    private fun getclient(): IMqttClient {
        val iMqttClient: IMqttClient = MqttClient(MQTT_SERVER_ADDRES, MQTT_PUBLISHER_ID)

        val options = MqttConnectionOptions()
        options.isAutomaticReconnect = true
        options.connectionTimeout = 10
        MQTT_SERVER_PASSWORD?.let { options.password = it.encodeToByteArray() }
        MQTT_SERVER_USERNAME?.let { options.userName = it }

        try {
            iMqttClient.connect(options)
        } catch (e: MqttException) {
            e.stackTrace
            throw Error("Failed to start sink")
        }

        return iMqttClient
    }

    override fun close(wait: Long?){
        try {
            if (wait == null){
                this.client?.disconnect()
            }
            else {
                this.client?.disconnect(wait)
            }
        }catch (e: java.lang.Exception){
            println(e.message)
        }finally {
            this.client?.close()
        }
    }



    override fun getDescription(): String? {
        return "\n      class: ${this.javaClass.name} \n      publisherId: ${this.MQTT_PUBLISHER_ID} \n      host: ${this.MQTT_SERVER_ADDRES} \n      topic: ${this.MQTT_SERVER_TOPIC}"
    }
}

class MqttMessage(value: String) : BaseEntity<String>(value) {
    override fun toString(): String {
        return this.getValue()
    }
}

class StringToMqttMessageTransformer(
    sequenceName: String,
    transformName: String = "stringToMqttMessageTransformer",
    metricsRepository: MetricsRepository = MetricsRepository
) : BaseDataTransformer<String, String>(sequenceName ,transformName, metricsRepository) {
    override fun apply(a: String): MqttMessage {
        return a.let { MqttMessage(it) }
    }

    override fun getDescription(): String? {
        return "\n      class: ${this.javaClass.name}"
    }
}