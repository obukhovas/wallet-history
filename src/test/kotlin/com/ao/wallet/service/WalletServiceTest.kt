package com.ao.wallet.service

import com.ao.wallet.BaseTest
import com.ao.wallet.dto.TransactionRecord
import com.ao.wallet.enum.WalletType.BTC
import com.ao.wallet.model.Transaction
import com.ao.wallet.model.Wallet
import com.ao.wallet.repository.TransactionRepository
import com.ao.wallet.repository.WalletRepository
import com.ao.wallet.single
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import java.math.BigDecimal
import java.time.OffsetDateTime.of
import java.time.ZoneOffset.UTC

class WalletServiceTest : BaseTest() {

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @Autowired
    private lateinit var service: WalletService

    @BeforeEach
    fun setUp() {
        val wallet = Wallet(BTC.name, 10.toBigDecimal())
        walletRepository.save(wallet)
    }

    @Test
    fun addRecord() {
        service.addRecord(TransactionRecord(of(2011, 10, 5, 14, 59, 0, 0, UTC), BigDecimal.TEN))
        service.addRecord(TransactionRecord(of(2011, 10, 5, 15, 10, 0, 0, UTC), BigDecimal.TEN))
        service.addRecord(TransactionRecord(of(2011, 10, 5, 15, 30, 0, 0, UTC), BigDecimal.TEN))
        service.addRecord(TransactionRecord(of(2011, 10, 5, 16, 20, 0, 0, UTC), BigDecimal.TEN))

        assertThat(walletRepository.single().currentAmount).isEqualTo((10 + 4 * 10).toBigDecimal())
        val transactions = transactionRepository.findAll()
        assertThat(transactions).hasSize(3)
        with(transactions[0]) {
            assertThat(datetime.isEqual(of(2011, 10, 5, 15, 0, 0, 0, UTC))).isTrue
            assertThat(amount).isEqualTo(10.toBigDecimal())
        }
        with(transactions[1]) {
            assertThat(datetime.isEqual(of(2011, 10, 5, 16, 0, 0, 0, UTC))).isTrue
            assertThat(amount).isEqualTo((2 * 10).toBigDecimal())
        }
        with(transactions[2]) {
            assertThat(datetime.isEqual(of(2011, 10, 5, 17, 0, 0, 0, UTC))).isTrue
            assertThat(amount).isEqualTo(10.toBigDecimal())
        }
    }

    @Test
    fun getHistory() {
        val history = listOf(
            Transaction(of(2011, 10, 5, 15, 0, 0, 0, UTC), 5.toBigDecimal()),
            Transaction(of(2011, 10, 5, 16, 0, 0, 0, UTC), 3.toBigDecimal()),
            Transaction(of(2011, 10, 5, 17, 0, 0, 0, UTC), 2.toBigDecimal()),
        )
        transactionRepository.saveAll(history)

        assertThat(service.getHistory(of(2011, 10, 5, 13, 30, 0, 0, UTC), of(2011, 10, 5, 13, 45, 0, 0, UTC))).isEmpty()
        with(service.getHistory(of(2011, 10, 5, 14, 30, 0, 0, UTC), of(2011, 10, 5, 14, 45, 0, 0, UTC))) {
            assertThat(this).hasSize(1)
            assertThat(this.elementAt(0).datetime.isEqual(of(2011, 10, 5, 15, 0, 0, 0, UTC))).isTrue
            assertThat(this.elementAt(0).amount).isEqualTo(5.toBigDecimal())
        }
        with(service.getHistory(of(2011, 10, 5, 13, 30, 0, 0, UTC), of(2011, 10, 5, 15, 30, 0, 0, UTC))) {
            assertThat(this).hasSize(2)
            assertThat(this.elementAt(0).datetime.isEqual(of(2011, 10, 5, 15, 0, 0, 0, UTC))).isTrue
            assertThat(this.elementAt(0).amount).isEqualTo(5.toBigDecimal())
            assertThat(this.elementAt(1).datetime.isEqual(of(2011, 10, 5, 16, 0, 0, 0, UTC))).isTrue
            assertThat(this.elementAt(1).amount).isEqualTo(8.toBigDecimal())
        }
        with(service.getHistory(of(2011, 10, 5, 13, 30, 0, 0, UTC), of(2011, 10, 5, 16, 30, 0, 0, UTC))) {
            assertThat(this).hasSize(3)
            assertThat(this.elementAt(0).datetime.isEqual(of(2011, 10, 5, 15, 0, 0, 0, UTC))).isTrue
            assertThat(this.elementAt(0).amount).isEqualTo(5.toBigDecimal())
            assertThat(this.elementAt(1).datetime.isEqual(of(2011, 10, 5, 16, 0, 0, 0, UTC))).isTrue
            assertThat(this.elementAt(1).amount).isEqualTo(8.toBigDecimal())
            assertThat(this.elementAt(2).datetime.isEqual(of(2011, 10, 5, 17, 0, 0, 0, UTC))).isTrue
            assertThat(this.elementAt(2).amount).isEqualTo(10.toBigDecimal())
        }
        with(service.getHistory(of(2011, 10, 5, 17, 30, 0, 0, UTC), of(2011, 10, 5, 17, 45, 0, 0, UTC))) {
            assertThat(this).hasSize(1)
            assertThat(this.elementAt(0).datetime.isEqual(of(2011, 10, 5, 17, 0, 0, 0, UTC))).isTrue
            assertThat(this.elementAt(0).amount).isEqualTo(10.toBigDecimal())
        }
        assertThat(service.getHistory(of(2011, 10, 5, 18, 30, 0, 0, UTC), of(2011, 10, 5, 18, 45, 0, 0, UTC))).isEmpty()
    }
}