package com.ao.wallet

import org.junit.jupiter.api.AfterEach
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
abstract class BaseTest {

    @Autowired
    private lateinit var repositories: Collection<JpaRepository<*, *>>

    @Autowired
    protected lateinit var mockMvc: MockMvc

    @AfterEach
    fun cleanUp() {
        repositories.forEach { it.deleteAll() }
    }

}
