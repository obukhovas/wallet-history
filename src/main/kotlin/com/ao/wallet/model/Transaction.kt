package com.ao.wallet.model

import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType.IDENTITY
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "transaction")
data class Transaction(
    @Column(name = "datetime")
    val datetime: OffsetDateTime,
    @Column(name = "amount")
    var amount: BigDecimal,
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "id")
    val id: Long? = null,
)