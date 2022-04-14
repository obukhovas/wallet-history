package com.ao.wallet

import com.ao.wallet.enum.WalletType.BTC
import com.ao.wallet.model.Transaction
import com.ao.wallet.model.Wallet
import com.ao.wallet.repository.TransactionRepository
import com.ao.wallet.repository.WalletRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime.of
import java.time.ZoneOffset.UTC

class IntegrationTest : BaseTest() {

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @BeforeEach
    fun setUp() {
        val wallet = Wallet(BTC.name, 10.toBigDecimal())
        val history = listOf(
            Transaction(of(2011, 10, 5, 15, 0, 0, 0, UTC), 5.toBigDecimal()),
            Transaction(of(2011, 10, 5, 16, 0, 0, 0, UTC), 3.toBigDecimal()),
            Transaction(of(2011, 10, 5, 17, 0, 0, 0, UTC), 2.toBigDecimal()),
        )
        walletRepository.save(wallet)
        transactionRepository.saveAll(history)
    }

    @Test
    fun test() {
        assertHistory(
            """{"startDatetime": "2011-10-05T13:48:01+00:00", "endDatetime": "2011-10-05T15:48:02+00:00" }""",
            """
              [
                { "datetime": "2011-10-05T15:00:00+00:00", "amount": 5 },
                { "datetime": "2011-10-05T16:00:00+00:00", "amount": 8 }
              ]
            """
        )

        addRecord("""{"datetime": "2011-10-05T16:50:01+01:00", "amount": 1 }""")
        addRecord("""{"datetime": "2011-10-05T17:55:01+02:00", "amount": 1 }""")

        assertHistory(
            """{ "startDatetime": "2011-10-05T13:48:01+00:00", "endDatetime": "2011-10-05T15:48:02+00:00" }""",
            """
              [
                { "datetime": "2011-10-05T15:00:00+00:00", "amount": 5 },
                { "datetime": "2011-10-05T16:00:00+00:00", "amount": 10 }
              ]
            """
        )

        addRecord("""{"datetime": "2011-10-05T19:50:01+03:00", "amount": 1 }""")
        addRecord("""{"datetime": "2011-10-05T20:30:01+04:00", "amount": 1 }""")

        assertHistory(
            """{ "startDatetime": "2011-10-05T13:48:01+00:00", "endDatetime": "2011-10-05T16:48:02+00:00" }""",
            """
              [
                { "datetime": "2011-10-05T15:00:00+00:00", "amount": 5 },
                { "datetime": "2011-10-05T16:00:00+00:00", "amount": 10 },
                { "datetime": "2011-10-05T17:00:00+00:00", "amount": 14 }
              ]
            """
        )

        addRecord("""{"datetime": "2011-10-05T21:20:01+05:00", "amount": 1 }""")
        addRecord("""{"datetime": "2011-10-05T22:33:01+00:00", "amount": 1 }""")

        assertHistory(
            """{ "startDatetime": "2011-10-05T13:48:01+00:00", "endDatetime": "2011-10-05T22:48:02+00:00" }""",
            """
              [
                { "datetime": "2011-10-05T15:00:00+00:00", "amount": 5 },
                { "datetime": "2011-10-05T16:00:00+00:00", "amount": 10 },
                { "datetime": "2011-10-05T17:00:00+00:00", "amount": 15 },
                { "datetime": "2011-10-05T23:00:00+00:00", "amount": 16 }
              ]
            """
        )
    }

    private fun addRecord(record: String) {
        mockMvc.perform(post("/records").contentType(APPLICATION_JSON)
            .content(record))
            .andDo(print())
            .andExpect(status().isOk)
    }

    private fun assertHistory(request: String, response: String) {
        mockMvc.perform(get("/records").contentType(APPLICATION_JSON).content(request))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(response, true))
    }

}