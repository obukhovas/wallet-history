package com.ao.wallet.repository

import com.ao.wallet.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.time.OffsetDateTime
import javax.persistence.LockModeType.PESSIMISTIC_WRITE

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

    @Lock(PESSIMISTIC_WRITE)
    fun findForUpdateByDatetime(dateTime: OffsetDateTime): Transaction?

    @Transactional(readOnly = true)
    fun findByDatetimeBetweenOrderByDatetimeAsc(from: OffsetDateTime, to: OffsetDateTime): Collection<Transaction>

    @Transactional(readOnly = true)
    @Query("SELECT COALESCE(SUM(t.amount), 0) FROM Transaction t WHERE t.datetime < :datetime")
    fun getTotalAmountUpTo(datetime: OffsetDateTime): BigDecimal

}