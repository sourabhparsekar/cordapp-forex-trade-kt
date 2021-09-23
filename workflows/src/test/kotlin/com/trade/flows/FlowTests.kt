package com.trade.flows

import com.trade.flows.CreateAndAssignTradeInitiator
import com.trade.flows.TradeDecisionInitiator
import net.corda.testing.node.*
import org.junit.Test
import net.corda.core.node.services.vault.QueryCriteria
import com.trade.states.TradeState
import com.trade.states.TradeStatus
import com.trade.AbstractFlowConfiguration
import net.corda.core.node.services.Vault.StateStatus
import org.junit.FixMethodOrder
import org.junit.runners.MethodSorters
import kotlin.test.assertEquals

class FlowTests : AbstractFlowConfiguration() {

    @Test
    fun `accept trade state between 2 parties`() {

        val counterPartyName = counterParty.name.organisation
        println("CounterParty $counterPartyName")

        val createAndAssignTradeInitiator = CreateAndAssignTradeInitiator(
            "Dollar",
            10,
            "Euro",
            7,
            counterPartyName
        )

        initiatorNode.startFlow(createAndAssignTradeInitiator).toCompletableFuture()

        mockNetwork.waitQuiescent()

        val queryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)

        var stateA =
            initiatorNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.INITIATED }.state.data
        var stateB =
            counterPartyNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.INITIATED }.state.data

        assertEquals(stateA, stateB, "Same state should be available in both nodes")

        val tradeDecisionInitiator = TradeDecisionInitiator(
            stateB.linearId.toString(),
            true
        )

        counterPartyNode.startFlow(tradeDecisionInitiator)

        mockNetwork.waitQuiescent()

        val stateASuccess =
            initiatorNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.SUCCESS }.state.data
        val stateBSuccess =
            counterPartyNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.SUCCESS }.state.data

        assertEquals(stateASuccess, stateBSuccess, "Same state should be available in both nodes")

        assertEquals(stateA.linearId, stateASuccess.linearId)
    }

    @Test
    fun `decline trade state between 2 parties`() {

        val counterPartyName = counterParty.name.organisation
        println("CounterParty $counterPartyName")

        val createAndAssignTradeInitiator = CreateAndAssignTradeInitiator(
            "Dollar",
            10,
            "Euro",
            7,
            counterPartyName
        )

        initiatorNode.startFlow(createAndAssignTradeInitiator).toCompletableFuture()

        mockNetwork.waitQuiescent()

        val queryCriteria = QueryCriteria.VaultQueryCriteria().withStatus(StateStatus.UNCONSUMED)

        var stateA =
            initiatorNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.INITIATED }.state.data
        var stateB =
            counterPartyNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.INITIATED }.state.data

        assertEquals(stateA, stateB, "Same state should be available in both nodes")

        val tradeDecisionInitiator = TradeDecisionInitiator(
            stateB.linearId.toString(),
            false
        )

        counterPartyNode.startFlow(tradeDecisionInitiator)

        mockNetwork.waitQuiescent()

        val stateAFailure =
            initiatorNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.FAILURE }.state.data
        val stateBFailure =
            counterPartyNode.services.vaultService.queryBy(
                TradeState::class.java,
                queryCriteria
            ).states.first { it.state.data.tradeStatus == TradeStatus.FAILURE }.state.data

        assertEquals(stateAFailure, stateBFailure, "Same state should be available in both nodes")

        assertEquals(stateA.linearId, stateAFailure.linearId)
    }
}
