package com.template.contracts

import com.template.states.TradeState
import com.template.states.TradeStatus
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.Party
import org.junit.Test
import kotlin.test.assertEquals

class StateTests {

    @Test
    fun `has fields of correct type`() {

        TradeState::class.java.declaredFields.iterator().forEach { println(it.name) }

        // Does the field exist?
        TradeState::class.java.getDeclaredField("sellCurrencyType")
        TradeState::class.java.getDeclaredField("sellCurrencyValue")
        TradeState::class.java.getDeclaredField("buyCurrencyType")
        TradeState::class.java.getDeclaredField("buyCurrencyValue")
        TradeState::class.java.getDeclaredField("tradeInitiatingParty")
        TradeState::class.java.getDeclaredField("tradeCounterParty")
        TradeState::class.java.getDeclaredField("tradeStatus")
        TradeState::class.java.getDeclaredField("linearId")

        // Is the field of the correct type?
        assertEquals(TradeState::class.java.getDeclaredField("sellCurrencyType").type, String::class.java)
        assertEquals(TradeState::class.java.getDeclaredField("sellCurrencyValue").type, Int::class.java)
        assertEquals(TradeState::class.java.getDeclaredField("buyCurrencyType").type, String::class.java)
        assertEquals(TradeState::class.java.getDeclaredField("buyCurrencyValue").type, Int::class.java)
        assertEquals(TradeState::class.java.getDeclaredField("tradeInitiatingParty").type, Party::class.java)
        assertEquals(TradeState::class.java.getDeclaredField("tradeCounterParty").type, Party::class.java)
        assertEquals(TradeState::class.java.getDeclaredField("tradeStatus").type, TradeStatus::class.java)
        assertEquals(TradeState::class.java.getDeclaredField("linearId").type, UniqueIdentifier()::class.java)

    }

}
