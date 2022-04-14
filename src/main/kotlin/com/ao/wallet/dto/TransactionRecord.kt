package com.ao.wallet.dto

import com.ao.wallet.DATE_TIME_PATTERN
import com.fasterxml.jackson.annotation.JsonFormat
import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.validation.constraints.Positive

data class TransactionRecord(
    @field:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DATE_TIME_PATTERN)
    val datetime: OffsetDateTime,
    @field:Positive
    val amount: BigDecimal,
)