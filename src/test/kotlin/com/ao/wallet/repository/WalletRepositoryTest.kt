package com.ao.wallet.repository

import com.ao.wallet.BaseTest
import com.ao.wallet.enum.WalletType.BTC
import com.ao.wallet.model.Wallet
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CountDownLatch
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit.SECONDS
import java.util.concurrent.TimeoutException

class WalletRepositoryTest : BaseTest() {

    @Autowired
    private lateinit var repository: WalletRepository

    @Autowired
    private lateinit var transactionTemplate: TransactionTemplate

    @Test
    fun `findForUpdateById should lock entity`() {
        val id = BTC.name
        repository.save(Wallet(id, 10.toBigDecimal()))

        val firstFindForUpdateLatch = CountDownLatch(1)
        val secondFindForUpdateLatch = CountDownLatch(1)
        val pool = Executors.newFixedThreadPool(2)

        val firstFindForUpdate = pool.submit {
            transactionTemplate.executeWithoutResult {
                repository.findForUpdateById(id)
                firstFindForUpdateLatch.countDown()
                secondFindForUpdateLatch.await()
            }
        }

        val secondFindForUpdate = pool.submit {
            transactionTemplate.executeWithoutResult {
                firstFindForUpdateLatch.await()
                repository.findForUpdateById(id)
            }
        }

        assertThrows<TimeoutException> { secondFindForUpdate.get(2, SECONDS) }
        secondFindForUpdateLatch.countDown()
        firstFindForUpdate.get()
    }

}