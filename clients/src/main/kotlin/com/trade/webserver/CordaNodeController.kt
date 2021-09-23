package com.trade.webserver

import com.trade.flows.CreateAndAssignTradeInitiator
import com.trade.flows.TradeDecisionInitiator
import com.trade.states.TradeState
import io.swagger.annotations.ApiOperation
import net.corda.core.flows.FlowException
import net.corda.core.node.services.Vault
import net.corda.core.node.services.vault.QueryCriteria
import org.apache.commons.lang3.tuple.Pair
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.util.*

/**
 * Define your API endpoints here.
 */
@RestController
//@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class CordaNodeController(rpc: NodeRPCConnection) {

    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }

    private val proxy = rpc.proxy

    @GetMapping(value = ["/me"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(tags = ["Node Operation"], value = "Get Node Identity")
    private fun myIdentity(): Pair<String, String> {
        return Pair.of("identity", proxy.nodeInfo().legalIdentities.first().name.toString())
    }

    @GetMapping(value = ["/nodes"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(tags = ["Node Operation"], value = "Get All the Node Identities in network")
    private fun network(): List<String> {
        val nodeInfo = proxy.networkMapSnapshot()
        return nodeInfo.map { it.legalIdentities.first().name.toString() }
    }


    @GetMapping(value = ["/trade/new"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(tags = ["Forex Trade"], value = "Create a New Forex Trade")
    private fun createNewTrade(
        @RequestParam(name = "Sell Currency Type", required = true) sellCurrencyType: String,
        @RequestParam(name = "Sell Currency Value", required = true) sellCurrencyValue: Int,
        @RequestParam(name = "Buy Currency Type", required = true) buyCurrencyType: String,
        @RequestParam(name = "Buy Currency Value", required = true) buyCurrencyValue: Int,
        @RequestParam(name = "Trade Party Org Name", required = true) tradeCounterPartyName: String
    ): ResponseEntity<*> {

        try {
            val flowHandle = proxy.startFlowDynamic(
                CreateAndAssignTradeInitiator::class.java,
                sellCurrencyType,
                sellCurrencyValue,
                buyCurrencyType,
                buyCurrencyValue,
                tradeCounterPartyName
            )

            return ResponseEntity.accepted().body(
                Pair.of(
                    "Linear ID",
                    (flowHandle.returnValue.get().tx.outputStates.first() as TradeState).linearId.toString()
                )
            )
        } catch (e: FlowException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Pair.of("Error", e.message))
        }

    }

    @GetMapping(value = ["/trade/{linearId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(tags = ["Forex Trade"], value = "Get an Existing Forex Trade by Linear Id")
    private fun getTrade(
        @PathVariable(value = "linearId", required = true) linearId: String
    ): ResponseEntity<*> {

        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, listOf(UUID.fromString(linearId)))

        val states = proxy.vaultQueryByCriteria(queryCriteria, TradeState::class.java).states

        return if (states.isEmpty())
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(Pair.of("Error", "State not found on ledger."))
        else
            ResponseEntity.ok(states.map { it.state.data.toString() })

    }


    @GetMapping(value = ["/trade/state-status/{status}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(tags = ["Forex Trade"], value = "Get all Forex Trades by State Status")
    private fun getTrades(
        @PathVariable(value = "status", required = true) stateStatus: Vault.StateStatus
    ): ResponseEntity<*> {

        // todo "Handle failure cases here"

        val queryCriteria = QueryCriteria.VaultQueryCriteria(
            stateStatus,
            setOf(TradeState::class.java)
        )

        return ResponseEntity.ok(
            proxy.vaultQueryByCriteria(
                queryCriteria,
                TradeState::class.java
            ).states.map { it.state.data.toString() })

    }


    @GetMapping(value = ["/trade/{linearId}/{is-approved}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    @ApiOperation(
        tags = ["Forex Trade"],
        value = "Process Existing Forex Trade by Linear Id and Approved/Reject Status"
    )
    private fun getTrade(
        @PathVariable(value = "linearId", required = true) linearId: String,
        @PathVariable(value = "is-approved", required = true) status: Boolean
    ): ResponseEntity<*> {

        try {
            val flowHandle = proxy.startFlowDynamic(
                TradeDecisionInitiator::class.java,
                linearId,
                status
            )

            return ResponseEntity.accepted().body(
                Pair.of(
                    "Linear ID",
                    (flowHandle.returnValue.get().tx.outputStates.first() as TradeState).linearId.toString()
                )
            )
        } catch (e: FlowException) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Pair.of("Error", e.message))
        }

    }
}