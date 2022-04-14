package com.ao.wallet.enum

enum class Error(val messageCode: String) {
    UNEXPECTED("unexpected.server.error"),
    JSON_PARSE("json.parse.error"),
    INVALID_FIELD("invalid.field.error"),
    MISSING_FIELD("missing.field.error"),
}