package com.ao.wallet.repository

import com.ao.wallet.model.Wallet
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.stereotype.Repository
import javax.persistence.LockModeType.PESSIMISTIC_WRITE

@Repository
interface WalletRepository : JpaRepository<Wallet, String> {

    @Lock(PESSIMISTIC_WRITE)
    fun findForUpdateById(id: String): Wallet

}