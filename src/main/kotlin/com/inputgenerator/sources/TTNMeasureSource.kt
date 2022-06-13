package com.inputgenerator.sources

import com.inputgenerator.metrics.MetricsRepository
import java.time.LocalDateTime

class TTNMeasureSource(
    sequenceName: String,
    sourceName: String = "ttnMeasureSource",
    sourceCount: String = "0",
) : BaseDataSource<String>(sequenceName, "${sourceName}-${sourceCount}", MetricsRepository) {
    override fun get(): String? {
        this.readMetric?.incValue()
        return "{\"end_device_ids\":{\"device_id\":\"eui-3c60050d3b60ffff\",\"application_ids\":{\"application_id\":\"isel-meic-tfm71-tttnmapper-test\"},\"dev_eui\":\"3C60050D3B60FFFF\",\"join_eui\":\"0000000000000000\"},\"correlation_ids\":[\"as:up:01G4WRWW6RK10QMCNRP7RQ46MW\",\"rpc:/ttn.lorawan.v3.AppAs/SimulateUplink:6ebeee66-c4eb-46cb-afd2-ab01e84123e5\"],\"received_at\":" +
                "\"" + LocalDateTime.now() + "\"" +
                ",\"uplink_message\":{\"f_port\":1,\"frm_payload\":\"Asg7C9PgAFA=\",\"decoded_payload\":{\"alt\":80,\"lat\":38.7568,\"lon\":-9.1165},\"rx_metadata\":[{\"gateway_ids\":{\"gateway_id\":\"test\"},\"rssi\":42,\"channel_rssi\":42,\"snr\":4.2}],\"settings\":{\"data_rate\":{\"lora\":{\"bandwidth\":125000,\"spreading_factor\":7}}}},\"simulated\":true}"
    }

    override fun available(): Boolean {
        return true
    }

    override fun getDescription(): String {
        return "\n      class: ${this.javaClass.name}"
    }
}