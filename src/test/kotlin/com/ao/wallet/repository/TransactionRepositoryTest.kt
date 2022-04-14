package com.ao.wallet.repository

import com.ao.wallet.BaseTest
import com.ao.wallet.model.Transaction
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.OffsetDateTime.of
import java.time.ZoneOffset.UTC
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException

class TransactionRepositoryTest : BaseTest() {

    @Autowired
    private lateinit var repository: TransactionRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `findForUpdateByDatetime should lock entity`() {
        val datetime = OffsetDateTime.now()
        repository.save(Transaction(datetime, 10.toBigDecimal()))

        val firstFindForUpdateLatch = CountDownLatch(1)
        val secondFindForUpdateLatch = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)

        val firstFindForUpdate = pool.submit {
            transactionTemplate.executeWithoutResult {
                repository.findForUpdateByDatetime(datetime)
                firstFindForUpdateLatch.countDown()
                secondFindForUpdateLatch.await()
            }
        }

        val secondFindForUpdate = pool.submit {
            transactionTemplate.executeWithoutResult {
                firstFindForUpdateLatch.await()
                repository.findForUpdateByDatetime(datetime)
            }
        }

        assertThrows<TimeoutException> { secondFindForUpdate.get(2, SECONDS) }
        secondFindForUpdateLatch.countDown()
        firstFindForUpdate.get()
    }

    @Test
    fun findByDatetimeBetweenOrderByDatetimeAsc() {
        val hits = listOf(
            Transaction(of(2018, 10, 21, 8, 0, 0, 0, UTC), 10.toBigDecimal()),
            Transaction(of(2018, 10, 21, 9, 0, 0, 0, UTC), 15.toBigDecimal()),
            Transaction(of(2018, 10, 21, 10, 0, 0, 0, UTC), 20.toBigDecimal()),
        )
        val misses = listOf(
            Transaction(of(2018, 10, 21, 7, 0, 0, 0, UTC), BigDecimal.ONE),
            Transaction(of(2018, 10, 21, 11, 0, 0, 0, UTC), BigDecimal.ONE)
        )
        repository.saveAll(hits + misses)

        val actual = repository.findByDatetimeBetweenOrderByDatetimeAsc(
            from = of(2018, 10, 21, 8, 0, 0, 0, UTC),
            to = of(2018, 10, 21, 10, 0, 0, 0, UTC)
        )

        assertThat(actual).hasSize(3)
        val softly = SoftAssertions()
        actual.forEachIndexed { index, record ->
            softly.assertThat(record).usingRecursiveComparison().ignoringFields("datetime").isEqualTo(hits[index])
            softly.assertThat(record.datetime.isEqual(hits[index].datetime)).isTrue
        }
        softly.assertAll()
    }

    @Test
    fun getTotalAmountUpTo() {
        val history = listOf(
            Transaction(of(2018, 10, 21, 8, 0, 0, 0, UTC), 10.toBigDecimal()),
            Transaction(of(2018, 10, 21, 9, 0, 0, 0, UTC), 15.toBigDecimal()),
            Transaction(of(2018, 10, 21, 10, 0, 0, 0, UTC), 20.toBigDecimal()),
        )
        repository.saveAll(history)

        assertThat(repository.getTotalAmountUpTo(of(2018, 10, 21, 8, 0, 0, 0, UTC))).isEqualTo(0.toBigDecimal())
        assertThat(repository.getTotalAmountUpTo(of(2018, 10, 21, 8, 1, 0, 0, UTC))).isEqualTo(10.toBigDecimal())
        assertThat(repository.getTotalAmountUpTo(of(2018, 10, 21, 9, 0, 0, 0, UTC))).isEqualTo(10.toBigDecimal())
        assertThat(repository.getTotalAmountUpTo(of(2018, 10, 21, 9, 1, 0, 0, UTC))).isEqualTo(25.toBigDecimal())
        assertThat(repository.getTotalAmountUpTo(of(2018, 10, 21, 10, 0, 0, 0, UTC))).isEqualTo(25.toBigDecimal())
        assertThat(repository.getTotalAmountUpTo(of(2018, 10, 21, 10, 1, 0, 0, UTC))).isEqualTo(45.toBigDecimal())
        assertThat(repository.getTotalAmountUpTo(of(2018, 10, 21, 11, 0, 0, 0, UTC))).isEqualTo(45.toBigDecimal())
    }

}