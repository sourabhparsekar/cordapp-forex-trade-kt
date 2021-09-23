package com.trade.contracts

import com.trade.states.TradeState
import com.trade.states.TradeStatus
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import java.util.*

class ContractTests {

    private val ledgerServices: MockServices = MockServices(listOf("com.template"))

    var partyA = TestIdentity(CordaX500Name("PartyA", "Seattle", "US"))
    var partyB = TestIdentity(CordaX500Name("PartyB", "Seattle", "US"))

    private val initiatedState = TradeState(
        "Dollar",
        1,
        "Euro",
        2,
        partyA.party,
        partyB.party,
        TradeStatus.INITIATED,
        UniqueIdentifier.fromString(UUID.randomUUID().toString())
    )


    @Test
    fun `test an initiated state`() {

        ledgerServices.ledger {
            transaction {
                output(TradeContract.ID, initiatedState)
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                verifies()
            }
        }
    }

    @Test
    fun `test an invalid initiated state`() {

        ledgerServices.ledger {
            transaction {
                output(TradeContract.ID, initiatedState.copy(tradeCounterParty = partyA.party))
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                fails()
            }
            transaction {
                output(TradeContract.ID, initiatedState.copy(buyCurrencyValue = 0))
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                fails()
            }
            transaction {
                output(TradeContract.ID, initiatedState.copy(sellCurrencyValue = 0))
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                fails()
            }
            transaction {
                output(TradeContract.ID, initiatedState.copy(sellCurrencyType = "a", buyCurrencyType = "a"))
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                fails()
            }
            transaction {
                output(TradeContract.ID, initiatedState.copy(tradeStatus = TradeStatus.SUCCESS))
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                fails()
            }
            transaction {
                output(TradeContract.ID, initiatedState.copy(sellCurrencyType = ""))
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                fails()
            }
            transaction {
                output(TradeContract.ID, initiatedState.copy(buyCurrencyType = ""))
                command(partyA.publicKey, TradeCommands.CreateAndAssignTradeCommand())
                fails()
            }
        }
    }


    @Test
    fun `test an accepted or declined state`() {

        ledgerServices.ledger {
            transaction {
                input(TradeContract.ID, initiatedState)
                output(TradeContract.ID, initiatedState.copy(tradeStatus = TradeStatus.SUCCESS))
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                verifies()
            }
            transaction {
                input(TradeContract.ID, initiatedState)
                output(TradeContract.ID, initiatedState.copy(tradeStatus = TradeStatus.FAILURE))
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                verifies()
            }
        }
    }

    @Test
    fun `test an invalid accepted or declined  state`() {

        ledgerServices.ledger {
            transaction {
                input(TradeContract.ID, initiatedState)
                output(
                    TradeContract.ID,
                    initiatedState.copy(tradeStatus = TradeStatus.SUCCESS, tradeCounterParty = partyA.party)
                )
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                fails()
            }
            transaction {
                input(TradeContract.ID, initiatedState)
                output(TradeContract.ID, initiatedState.copy(tradeStatus = TradeStatus.SUCCESS, buyCurrencyValue = 0))
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                fails()
            }
            transaction {
                input(TradeContract.ID, initiatedState)
                output(TradeContract.ID, initiatedState.copy(tradeStatus = TradeStatus.SUCCESS, sellCurrencyValue = 0))
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                fails()
            }
            transaction {
                input(TradeContract.ID, initiatedState)
                output(
                    TradeContract.ID,
                    initiatedState.copy(
                        tradeStatus = TradeStatus.SUCCESS,
                        sellCurrencyType = "a",
                        buyCurrencyType = "a"
                    )
                )
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                fails()
            }
            transaction {
                input(TradeContract.ID, initiatedState)
                output(TradeContract.ID, initiatedState)
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                fails()
            }
            transaction {
                input(TradeContract.ID, initiatedState)
                output(TradeContract.ID, initiatedState.copy(tradeStatus = TradeStatus.SUCCESS, sellCurrencyType = ""))
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                fails()
            }
            transaction {
                input(TradeContract.ID, initiatedState)
                output(TradeContract.ID, initiatedState.copy(tradeStatus = TradeStatus.SUCCESS, buyCurrencyType = ""))
                command(partyA.publicKey, TradeCommands.TradeDecisionCommand())
                fails()
            }
        }
    }
}