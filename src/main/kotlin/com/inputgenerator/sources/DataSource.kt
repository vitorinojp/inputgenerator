package com.inputgenerator.sources

interface DataSource <V> {
    fun get(): V?
    fun available(): Boolean
    fun getDescription(): String
}

