package com.acme.slamonitor.api.validate

interface ValidationPipeline<T> {

    fun validate(request: T)
}
