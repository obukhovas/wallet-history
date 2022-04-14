package com.ao.wallet.web

import com.ao.wallet.dto.TransactionRecord
import com.ao.wallet.dto.HistoryRequest
import com.ao.wallet.service.WalletService
import org.springframework.http.HttpStatus.NO_CONTENT
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@RestController
@RequestMapping("/records")
class RecordsController(private val walletService: WalletService) {

    @PostMapping(produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    fun addRecord(@RequestBody @Valid record: TransactionRecord): ResponseEntity<Nothing> {
        walletService.addRecord(record)
        return ResponseEntity.ok().build()
    }

    @GetMapping(produces = [APPLICATION_JSON_VALUE], consumes = [APPLICATION_JSON_VALUE])
    fun getHistory(@RequestBody @Valid request: HistoryRequest): ResponseEntity<Collection<TransactionRecord>> {
        val history = walletService.getHistory(request.startDatetime, request.endDatetime)
        return if (history.isEmpty()) {
            ResponseEntity.status(NO_CONTENT).body(history)
        } else {
            ResponseEntity.ok(history)
        }
    }

}