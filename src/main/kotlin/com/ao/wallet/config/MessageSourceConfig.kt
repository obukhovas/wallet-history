package com.ao.wallet.config

import org.springframework.context.MessageSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.support.ReloadableResourceBundleMessageSource

@Configuration
class MessageSourceConfig {
    @Bean
    fun messageSource(): MessageSource {
        val messageSource = ReloadableResourceBundleMessageSource()
        messageSource.setBasename("classpath:messages/messages")
        messageSource.setUseCodeAsDefaultMessage(true)
        messageSource.setDefaultEncoding("UTF-8")
        return messageSource
    }
}