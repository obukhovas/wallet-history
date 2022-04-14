package com.ao.wallet.dto

data class ErrorsResponse(val errors: Collection<ErrorMessage>) {
    constructor(message: ErrorMessage) : this(listOf(message))
    constructor(message: String) : this(listOf(ErrorMessage(message)))
}

data class ErrorMessage(val message: String)