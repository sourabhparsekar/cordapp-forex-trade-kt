package com.trade.states

import com.trade.contracts.TradeContract
import net.corda.core.contracts.BelongsToContract
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.serialization.CordaSerializable

@BelongsToContract(TradeContract::class)
@CordaSerializable
data class TradeState (
    val sellCurrencyType: String,
    val sellCurrencyValue: Int,
    val buyCurrencyType: String,
    val buyCurrencyValue: Int,
    val tradeInitiatingParty: Party,
    val tradeCounterParty: Party,
    val tradeStatus: TradeStatus,
    override val linearId: UniqueIdentifier = UniqueIdentifier()
) : LinearState {
    override val participants: List<AbstractParty> = listOf(tradeInitiatingParty, tradeCounterParty)
}