package com.ao.wallet.web

import com.ao.wallet.BaseTest
import com.ao.wallet.enum.WalletType.BTC
import com.ao.wallet.model.Transaction
import com.ao.wallet.model.Wallet
import com.ao.wallet.patch
import com.ao.wallet.repository.TransactionRepository
import com.ao.wallet.repository.WalletRepository
import com.ao.wallet.single
import org.assertj.core.api.Assertions.assertThat
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultHandlers.print
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import java.time.OffsetDateTime
import java.time.ZoneOffset.UTC

class RecordsControllerTest : BaseTest() {

    @Autowired
    private lateinit var walletRepository: WalletRepository

    @Autowired
    private lateinit var transactionRepository: TransactionRepository

    @BeforeEach
    fun setUp() {
        val wallet = Wallet(BTC.name, 10.toBigDecimal())
        val history = listOf(
            Transaction(OffsetDateTime.of(2011, 10, 5, 15, 0, 0, 0, UTC), 5.toBigDecimal()),
            Transaction(OffsetDateTime.of(2011, 10, 5, 16, 0, 0, 0, UTC), 3.toBigDecimal()),
            Transaction(OffsetDateTime.of(2011, 10, 5, 17, 0, 0, 0, UTC), 2.toBigDecimal()),
        )
        walletRepository.save(wallet)
        transactionRepository.saveAll(history)
    }

    @Test
    fun addRecord() {
        @Language("JSON")
        val request = """{"datetime": "2019-10-05T14:48:01+01:00", "amount": 1.1}"""

        mockMvc.perform(post("/records").contentType(APPLICATION_JSON).content(request))
            .andDo(print())
            .andExpect(status().isOk)

        assertThat(walletRepository.single().currentAmount).isEqualTo(11.1.toBigDecimal())
        assertThat(transactionRepository.findAll()).hasSize(4)
    }

    @TestFactory
    fun `addRecord should return 400 if input data is invalid`(): Collection<DynamicTest> {
        @Language("JSON")
        val validRequest = """{"datetime": "2019-10-05T14:48:01+01:00", "amount": 1.1}"""

        return listOf(
            "{Trust me I'm Json}" to "JSON parse error",
            validRequest.patch("/datetime", "", operation = "remove") to "Field datetime is missing",
            validRequest.patch("/datetime", null) to "Field datetime is missing",
            validRequest.patch("/datetime", "2019 10 05 14:48:01+01:00") to "JSON parse error",
            validRequest.patch("/amount", "", operation = "remove") to "Field amount is missing",
            validRequest.patch("/amount", null) to "Field amount is missing",
            validRequest.patch("/amount", -1.1) to "Field amount must be greater than 0",
        ).map { (request, expectedMessage) ->
            DynamicTest.dynamicTest("request: $request, expected: $expectedMessage") {
                val response = """{"errors":[{"message":"$expectedMessage"}]}"""

                mockMvc.perform(post("/records").contentType(APPLICATION_JSON).content(request))
                    .andDo(print())
                    .andExpect(status().isBadRequest)
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(content().json(response, true))
            }
        }
    }

    @Test
    fun getHistory() {
        @Language("JSON")
        val request = """
            {
              "startDatetime": "2011-10-05T13:48:01+00:00", 
              "endDatetime": "2011-10-05T15:48:02+00:00"
            }
        """.trimIndent()

        @Language("JSON")
        val expectedResponse = """
            [
              { "datetime": "2011-10-05T15:00:00+00:00", "amount": 5 },
              { "datetime": "2011-10-05T16:00:00+00:00", "amount": 8 }
            ]
        """.trimIndent()

        mockMvc.perform(get("/records").contentType(APPLICATION_JSON).content(request))
            .andDo(print())
            .andExpect(status().isOk)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json(expectedResponse, true))
    }

    @Test
    fun `getHistory should return 204 if there is no history for input range`() {
        @Language("JSON")
        val request = """
            { 
              "startDatetime": "2031-10-05T10:48:01+00:00", 
              "endDatetime": "2031-10-05T18:48:02+00:00"
            }
        """.trimIndent()

        mockMvc.perform(get("/records").contentType(APPLICATION_JSON).content(request))
            .andDo(print())
            .andExpect(status().isNoContent)
            .andExpect(content().contentType(APPLICATION_JSON))
            .andExpect(content().json("[]", true))
    }

    @TestFactory
    fun `getHistory should return 400 if input data is invalid`(): Collection<DynamicTest> {
        @Language("JSON")
        val validRequest = """
            {
              "startDatetime": "2011-10-05T10:48:01+00:00", 
              "endDatetime": "2011-10-05T18:48:02+00:00"
            }
        """.trimIndent()

        return listOf(
            "{Trust me I'm Json}" to "JSON parse error",
            validRequest.patch("/startDatetime", "", operation = "remove") to "Field startDatetime is missing",
            validRequest.patch("/startDatetime", null) to "Field startDatetime is missing",
            validRequest.patch("/startDatetime", "2019 10 05 14:48:01+01:00") to "JSON parse error",
            validRequest.patch("/startDatetime", "", operation = "remove") to "Field startDatetime is missing",
            validRequest.patch("/startDatetime", null) to "Field startDatetime is missing",
            validRequest.patch("/startDatetime", "2019 10 05 14:48:01+01:00") to "JSON parse error",
            validRequest.patch("/endDatetime", "2010-10-05T18:48:02+00:00") to "'start' should be before 'end'",
            validRequest.patch("/endDatetime", "2021-10-05T18:48:02+00:00") to "Too much history range, max 28 days",
        ).map { (request, expectedMessage) ->
            DynamicTest.dynamicTest("request: $request, expected: $expectedMessage") {
                val response = """{"errors":[{"message":"$expectedMessage"}]}"""

                mockMvc.perform(get("/records").contentType(APPLICATION_JSON).content(request))
                    .andDo(print())
                    .andExpect(status().isBadRequest)
                    .andExpect(content().contentType(APPLICATION_JSON))
                    .andExpect(content().json(response, true))
            }
        }
    }
}