package com.inputgenerator.sinks

import com.inputgenerator.entities.BaseEntity
import com.inputgenerator.entities.DataEntity
import com.inputgenerator.transform.DataTransformer
import org.eclipse.paho.mqttv5.client.IMqttClient
import org.eclipse.paho.mqttv5.client.MqttClient
import org.eclipse.paho.mqttv5.client.MqttConnectionOptions
import org.eclipse.paho.mqttv5.common.MqttException


class MqttSink(
    private val MQTT_PUBLISHER_ID: String,
    private val MQTT_SERVER_ADDRES: String = "tcp://127.0.0.1:1883",
    private val MQTT_SERVER_PASSWORD: String? = null,
    private val MQTT_SERVER_USERNAME: String? = null,
    private val MQTT_SERVER_TOPIC: String
) : DataSink<String>
{
    private var client: IMqttClient? = this.getclient()

    override fun write(data: DataEntity<String>): DataEntity<String> {
        val mqttMessage: org.eclipse.paho.mqttv5.common.MqttMessage =
            org.eclipse.paho.mqttv5.common.MqttMessage(
                data
                    .getValue()
                    .encodeToByteArray()
            )
        mqttMessage.qos = 0
        client?.publish(MQTT_SERVER_TOPIC, mqttMessage)

        //System.out.println("Mqtt sent: " + data.getValue())

        return data
    }

    private fun getclient(): IMqttClient{
        val iMqttClient: IMqttClient = MqttClient(MQTT_SERVER_ADDRES, MQTT_PUBLISHER_ID)

        val options = MqttConnectionOptions()
        options.isAutomaticReconnect = true
        options.connectionTimeout = 10
        MQTT_SERVER_PASSWORD?.let { options.password = it.encodeToByteArray()}
        MQTT_SERVER_USERNAME?.let { options.userName = it}

        try {
            iMqttClient.connect(options)
        } catch (e: MqttException){
            System.out.println("Mqtt could not connect do to: ")
            e.stackTrace
            throw Error("Failed to start sink")
        }

        return iMqttClient
    }

    override fun getDescription(): String? {
        return "\n      class: ${this.javaClass.name} \n      publisherId: ${this.MQTT_PUBLISHER_ID} \n      host: ${this.MQTT_SERVER_ADDRES} \n      topic: ${this.MQTT_SERVER_TOPIC}"
    }
}

class MqttMessage(value: String): BaseEntity<String>(value) {
    override fun toString(): String { return this.getValue()}
}

class StringToMqttMessageTransformer: DataTransformer<String, String> {
    override fun apply(a: String): MqttMessage {
        return a?.let { MqttMessage(it) }
    }

    override fun getDescription(): String? {
        return "\n      class: ${this.javaClass.name}"
    }
}