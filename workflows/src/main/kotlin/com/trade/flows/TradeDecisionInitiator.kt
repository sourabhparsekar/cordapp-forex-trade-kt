package com.trade.flows

import co.paralleluniverse.fibers.Suspendable
import com.trade.contracts.TradeCommands
import com.trade.states.TradeState
import com.trade.states.TradeStatus
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import java.util.*

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
class TradeDecisionInitiator(
    val linearId: String,
    val isApproved: Boolean
) : FlowLogic<SignedTransaction>() {
    /**
     * This is where you fill out your business logic.
     */
    @Suspendable
    override fun call(): SignedTransaction {

        // Step 1. Get a reference to the notary service on our network and our key pair.
        // Note: ongoing work to support multiple notary identities is still in progress.
        val notary = serviceHub.networkMapCache.notaryIdentities[0]
        println("Found Notary ${notary.name}")

        // Step 2: Using the linear ID get the Trade State on ledger
        val queryCriteria = QueryCriteria.LinearStateQueryCriteria(null, listOf(UUID.fromString(linearId)))
        val stateAndRefList: List<StateAndRef<TradeState>> =
            serviceHub.vaultService.queryBy(TradeState::class.java, queryCriteria).states
        if (stateAndRefList.isEmpty()) throw FlowException("State does not exist on Ledger.")
        val stateAndRef = stateAndRefList.first()

        // Step 3: Get the counter party the trade has to be assigned to after processing
        println("Looking up for party ")
        val state: TradeState = stateAndRef.state.data
        val tradeCounterParty: Party = state.tradeInitiatingParty
        println("Found Party ${tradeCounterParty.name}")

        // Step 4: Compose the New State
        val tradeState: TradeState = TradeState(
            state.sellCurrencyType,
            state.sellCurrencyValue,
            state.buyCurrencyType,
            state.buyCurrencyValue,
            ourIdentity,
            tradeCounterParty,
            when (isApproved) {
                true -> TradeStatus.SUCCESS
                false -> TradeStatus.FAILURE
            },
            UniqueIdentifier.fromString(linearId)

        )
        println("Linear ID of trade state is ${tradeState.linearId} Status ${tradeState.tradeStatus}")

        // Step 5. Create a new transaction using TransactionBuilder
        val transactionSigners = listOf(ourIdentity.owningKey, tradeCounterParty.owningKey)
        val transaction = TransactionBuilder(notary)
            .addCommand(TradeCommands.TradeDecisionCommand(), transactionSigners)
            .addInputState(stateAndRef)
            .addOutputState(tradeState)
        println("Transaction is built")

        // Step 6. Verify and sign the transaction with our KeyPair.
        transaction.verify(serviceHub)
        println("Transaction is verified")

        val partialSignedTransaction = serviceHub.signInitialTransaction(transaction)
        println("Transaction is signed by Initiator")

        // Step 7. Gather signatures from the counter party
        val tradeCounterPartySession: FlowSession = initiateFlow(tradeCounterParty)
        val signedTransaction = subFlow(
            CollectSignaturesFlow(
                partialSignedTransaction,
                setOf(tradeCounterPartySession)
            )
        )
        println("Transaction is signed by Counter Party")

        // Step 8. Notarize and finalise the transaction
        return subFlow(
            FinalityFlow(
                signedTransaction,
                tradeCounterPartySession
            )
        )
    }
}