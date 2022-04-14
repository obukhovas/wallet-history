package com.ao.wallet.dto

import com.ao.wallet.DATE_TIME_PATTERN
import com.fasterxml.jackson.annotation.JsonFormat
import java.time.OffsetDateTime

data class HistoryRequest(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    val startDatetime: OffsetDateTime,
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    val endDatetime: OffsetDateTime,
)