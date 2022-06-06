package com.inputgenerator.sinks

import com.inputgenerator.entities.DataEntity

interface DataSink <V> {
    fun write(data: DataEntity<V>): DataEntity<V>
    fun getDescription(): String?
}

class DummySink <V>: DataSink<V> {
    override fun write(data: DataEntity<V>): DataEntity<V> {
        return data
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}