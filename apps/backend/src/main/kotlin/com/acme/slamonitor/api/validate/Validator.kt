package com.acme.slamonitor.api.validate

import com.acme.slamonitor.api.dto.ValidationError

interface Validator<T> {

    fun validate(request: T): List<ValidationError>
}
