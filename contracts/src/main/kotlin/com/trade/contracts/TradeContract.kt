package com.trade.contracts

import com.trade.states.TradeState
import com.trade.states.TradeStatus
import net.corda.core.contracts.*
import net.corda.core.transactions.LedgerTransaction

class TradeContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.trade.contracts.TradeContract"
    }

    /**
     * Takes an object that represents a state transition, and ensures the inputs/outputs/commands make sense.
     * Must throw an exception if there's a problem that should prevent state transition. Takes a single object
     * rather than an argument so that additional data can be added without breaking binary compatibility with
     * existing contract code.
     */
    override fun verify(tx: LedgerTransaction) {

        println("Transaction verification started")

        val commands = tx.commands
        when (commands.first().value) {
            is TradeCommands.CreateAndAssignTradeCommand ->
                requireThat {
                    "Input State does not exists for New Transaction.".using(tx.inputStates.isEmpty())
                    "Only 1 Output State exists for New Trasnaction.".using(tx.outputStates.size == 1)

                    "Output state can only be TradeState".using(tx.outputStates.first() is TradeState)
                    val tradeState: TradeState = tx.outputStates.first() as TradeState
                    "Trade Initiator and Counterparty cannot be same.".using(tradeState.tradeInitiatingParty != tradeState.tradeCounterParty)
                    "Trade cannot happen with same currency type.".using(
                        !tradeState.buyCurrencyType.contentEquals(
                            tradeState.sellCurrencyType
                        )
                    )
                    "Trade Sell Currency Type cannot be blank or empty.".using(tradeState.sellCurrencyType.isNotBlank())
                    "Trade Buy Currency Type cannot be blank or empty.".using(tradeState.buyCurrencyType.isNotBlank())
                    "Trade Sell Currency Value cannot be empty or zero.".using(tradeState.sellCurrencyValue > 0)
                    "Trade Buy Currency Value cannot be empty or zero.".using(tradeState.buyCurrencyValue > 0)
                    "Trade Status should be Initiated for New Trade Exchange.".using(TradeStatus.INITIATED == tradeState.tradeStatus)
                }

            is TradeCommands.TradeDecisionCommand ->
                requireThat {
                    "Only 1 Input State is expected.".using(tx.inputStates.size == 1)
                    "Only 1 Output State is to be created.".using(tx.outputStates.size == 1)
                    "Input state can only be TradeState".using(tx.inputStates.first() is TradeState)
                    "Output state can only be TradeState".using(tx.outputStates.first() is TradeState)

                    val inputTradeState: TradeState = tx.inputStates.first() as TradeState
                    "Trade Status should be Initiated Trade Exchange.".using(TradeStatus.INITIATED == inputTradeState.tradeStatus)

                    val outputTradeState: TradeState = tx.outputStates.first() as TradeState
                    "Trade Initiator and Counterparty cannot be same.".using(outputTradeState.tradeInitiatingParty != outputTradeState.tradeCounterParty)
                    "Trade cannot happen with same currency type.".using(
                        !outputTradeState.buyCurrencyType.contentEquals(
                            outputTradeState.sellCurrencyType
                        )
                    )
                    "Trade Sell Currency Type cannot be blank or empty.".using(outputTradeState.sellCurrencyType.isNotBlank())
                    "Trade Buy Currency Type cannot be blank or empty.".using(outputTradeState.buyCurrencyType.isNotBlank())
                    "Trade Sell Currency Value cannot be empty or zero.".using(outputTradeState.sellCurrencyValue > 0)
                    "Trade Buy Currency Value cannot be empty or zero.".using(outputTradeState.buyCurrencyValue > 0)
                    "Trade Status should be Success/Failure Trade Exchange.".using(TradeStatus.INITIATED != outputTradeState.tradeStatus)
                    "Linear ID should be same for input/output Trade State".using(inputTradeState.linearId.equals(outputTradeState.linearId))
                }

        }

        println("Transaction verification ended")

    }

}