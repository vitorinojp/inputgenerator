package com.inputgenerator.common

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.nanoseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

@org.springframework.context.annotation.Configuration
class Configuration() {
    var baseDelay: Int = 0
    var filePath: String? = null
    var dataSink: String? = null
    var dataSource: String? = null
    var dataTransformer: String? = null
    var host: String = "localhost"
    var port: String = "1883"
    var password: String? = null
    var username: String? = null
    var topic: String = "/"
    var burst: Int = 1
    var rate: Int? = null
    var wait: Boolean = true
    @OptIn(ExperimentalTime::class)
    var delayDuration: Duration? = null
        get() = rate?.let { rate -> 60000.milliseconds / rate }
    var restart: Boolean = false
}

// Sources
const val SOURCE_FILE_NAME = "generator.source.file"
const val SOURCE_RESTART = "generator.source.restart"

// Hosts
const val HOST_NAME = "host"
const val HOST_PORT = "port"
const val HOST_PASSWORD = "password"
const val HOST_USERNAME = "username"
const val HOST_TOPIC = "topic"