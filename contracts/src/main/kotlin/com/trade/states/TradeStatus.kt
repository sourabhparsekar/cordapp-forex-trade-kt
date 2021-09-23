package com.trade.states

import net.corda.core.serialization.CordaSerializable

@CordaSerializable
enum class TradeStatus {
    INITIATED, SUCCESS, FAILURE
}
