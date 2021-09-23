package com.template.contracts

import net.corda.core.contracts.CommandData

interface TradeCommands : CommandData {
    class CreateAndAssignTradeCommand : TradeCommands
    class TradeDecisionCommand : TradeCommands
}