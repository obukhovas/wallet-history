package com.ao.wallet.model

import java.math.BigDecimal
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "wallet")
data class Wallet(
    @Id
    @Column(name = "id")
    val id: String,
    @Column(name = "current_amount")
    var currentAmount: BigDecimal,
)