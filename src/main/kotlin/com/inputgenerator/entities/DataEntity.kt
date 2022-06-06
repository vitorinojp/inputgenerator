package com.inputgenerator.entities

interface DataEntity<V> {
    fun getValue(): V
}

open class BaseEntity<V> (private val value: V): DataEntity<V> {
    override fun getValue(): V {
        return this.value
    }
}