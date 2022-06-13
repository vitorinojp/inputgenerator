package com.inputgenerator.transform

import com.inputgenerator.entities.BaseEntity
import com.inputgenerator.entities.DataEntity

interface DataTransformer<in A, B> {
    fun apply(a: A): DataEntity<B>?
    fun getDescription(): String?
}

class PassAllTransformer<A>() : DataTransformer<A, A> {
    override fun apply(a: A): DataEntity<A>? {
        return BaseEntity(a)
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}

class DummyTransformer : DataTransformer<Any, Unit> {
    override fun apply(a: Any): BaseEntity<Unit>? {
        return null
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}

class StringTransformer : DataTransformer<String, String> {
    override fun apply(a: String): BaseEntity<String>? {
        return BaseEntity(a)
    }

    override fun getDescription(): String? {
        return this.javaClass.name
    }
}