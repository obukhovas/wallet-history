package com.ao.wallet.service

import com.ao.wallet.InvalidHistoryRangeException
import com.ao.wallet.dto.TransactionRecord
import com.ao.wallet.enum.WalletType.BTC
import com.ao.wallet.model.Transaction
import com.ao.wallet.repository.TransactionRepository
import com.ao.wallet.repository.WalletRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS

@Service
class WalletService(
    private val walletRepository: WalletRepository,
    private val transactionRepository: TransactionRepository,
) {

    private companion object {
        const val MAX_HISTORY_RANGE_DAYS = 28
    }

    @Transactional
    fun addRecord(deposit: TransactionRecord) {
        val wallet = walletRepository.findForUpdateById(BTC.name)
            .also { it.currentAmount = it.currentAmount.add(deposit.amount) }

        val transaction = transactionRepository.findForUpdateByDatetime(deposit.datetime.truncatedToEndOfHour())
            ?.also { it.amount = it.amount.plus(deposit.amount) }
            ?: Transaction(deposit.datetime.truncatedToEndOfHour(), deposit.amount)

        walletRepository.save(wallet)
        transactionRepository.save(transaction)
    }

    fun getHistory(from: OffsetDateTime, to: OffsetDateTime): Collection<TransactionRecord> {
        validateHistoryRange(from, to)

        val truncatedFrom = from.truncatedToBeginningOfHour()
        val truncatedTo = to.truncatedToEndOfHour()

        val records = transactionRepository.findByDatetimeBetweenOrderByDatetimeAsc(truncatedFrom, truncatedTo)
        if (records.isEmpty()) {
            return emptyList()
        }

        val totalUpToFrom = transactionRepository.getTotalAmountUpTo(truncatedFrom)

        val history = records.runningFold(TransactionRecord(truncatedFrom, totalUpToFrom)) { acc, record ->
            TransactionRecord(record.datetime, record.amount.plus(acc.amount))
        }.toMutableList().also { it.removeFirst() }

        return history
    }

    private fun validateHistoryRange(from: OffsetDateTime, to: OffsetDateTime) {
        if (from.isAfter(to)) {
            throw InvalidHistoryRangeException("'start' should be before 'end'")
        }

        if (DAYS.between(from, to) > MAX_HISTORY_RANGE_DAYS) {
            throw InvalidHistoryRangeException("Too much history range, max $MAX_HISTORY_RANGE_DAYS days")
        }
    }

    private fun OffsetDateTime.truncatedToBeginningOfHour() = truncatedTo(HOURS)
    private fun OffsetDateTime.truncatedToEndOfHour() = truncatedTo(HOURS).plusHours(1)

}