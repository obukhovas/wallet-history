package com.ao.wallet

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class WalletAppApplication

fun main(args: Array<String>) {
	runApplication<WalletAppApplication>(*args)
}
