package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.TradeCommands
import com.template.states.TradeState
import com.template.states.TradeStatus
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

// ******************
// * Initiator flow *
// ******************
@InitiatingFlow
@StartableByRPC
class CreateAndAssignTradeInitiator(
    val sellCurrencyType: String,
    val sellCurrencyValue: Int,
    val buyCurrencyType: String,
    val buyCurrencyValue: Int,
    val tradeCounterPartyName: String
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

        // Step 2: Get the counter party the trade has to be assigned to
        println("Looking up for party $tradeCounterPartyName")
        val counterparties: Set<Party> = serviceHub.identityService.partiesFromName(tradeCounterPartyName, true)
        val tradeCounterParty: Party = counterparties.iterator().next()
        println("Found Party ${tradeCounterParty.name}")

        // Step 3: Compose the State
        val tradeState: TradeState = TradeState(
            sellCurrencyType,
            sellCurrencyValue,
            buyCurrencyType,
            buyCurrencyValue,
            ourIdentity,
            tradeCounterParty,
            TradeStatus.INITIATED
        )
        println("Linear ID of trade state is " + tradeState.linearId)

        // Step 4. Create a new transaction using TransactionBuilder
        val transactionSigners = listOf(ourIdentity.owningKey, tradeCounterParty.owningKey)
        val transaction = TransactionBuilder(notary)
            .addCommand(TradeCommands.CreateAndAssignTradeCommand(), transactionSigners)
            .addOutputState(tradeState)
        println("Transaction is built")

        // Step 5. Verify and sign the transaction with our KeyPair.
        transaction.verify(serviceHub)
        println("Transaction is verified")

        val partialSignedTransaction = serviceHub.signInitialTransaction(transaction)
        println("Transaction is signed by Initiator")

        // Step 6. Gather signatures from the counter party
        val tradeCounterPartySession: FlowSession = initiateFlow(tradeCounterParty)
        val signedTransaction = subFlow(
            CollectSignaturesFlow(
                partialSignedTransaction,
                setOf(tradeCounterPartySession)
            )
        )
        println("Transaction is signed by Counter Party")

        // Step 7. Notarize and finalise the transaction
        return subFlow(
            FinalityFlow(
                signedTransaction,
                tradeCounterPartySession
            )
        )
    }
}
