package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.states.TradeState
import net.corda.core.flows.*
import net.corda.core.transactions.SignedTransaction

// ******************
// * Responder flow *
// ******************
@InitiatedBy(CreateAndAssignTradeInitiator::class)
class CreateAndAssignTradeResponder(
    val counterpartySession: FlowSession
) : FlowLogic<SignedTransaction>() {

    /**
     * This is where you fill out your business logic.
     */
    @Suspendable
    override fun call(): SignedTransaction {

        println("Responder Flow Called")
        val signTransactionFlow: SignTransactionFlow = object : SignTransactionFlow(counterpartySession) {
            @Throws(FlowException::class)
            override fun checkTransaction(stx: SignedTransaction) {
                val state = stx.tx.outputs.iterator().next().data as TradeState
                println("accept transaction with linear ${state.linearId}")
            }
        }

        val signedTransaction = subFlow(signTransactionFlow)

        //The receiving counterpart to FinalityFlow.
        //All parties who are receiving a finalised transaction from a sender flow must subcall this flow in their own flows.
        //It's typical to have already signed the transaction proposal in the same workflow using SignTransactionFlow. If so then the transaction ID can be passed in as an extra check to ensure the finalised transaction is the one that was signed before it's committed to the vault
        return subFlow(
            ReceiveFinalityFlow(
                counterpartySession,
                signedTransaction.id
            )
        )

    }
}
