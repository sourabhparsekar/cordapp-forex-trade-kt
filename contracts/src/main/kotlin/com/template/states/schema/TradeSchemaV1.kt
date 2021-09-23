//package com.template.states.schema
//
//import net.corda.core.identity.Party
//import net.corda.core.schemas.MappedSchema
//import net.corda.core.schemas.PersistentState
//import java.util.*
//import javax.persistence.Column
//import javax.persistence.Entity
//import javax.persistence.Table
//
//object TradeSchemaV1 :
//    MappedSchema(TradeSchema::class.java, 1, listOf(TradeModel::class.java)) {
//
//    @Entity
//    @Table(name = "trade_v1")
//    class TradeModel(
//
//        @field:Column(name = "id") val linearId: UUID,
//        @field:Column(name = "sell_currency_type") val sellCurrencyType: String,
//        @field:Column(name = "sell_currency_value") val sellCurrencyValue: Int,
//        @field:Column(name = "buy_currency_type") val buyCurrencyType: String,
//        @field:Column(name = "buy_currency_value") val buyCurrencyValue: Int,
//        @field:Column(name = "trade_initiator") val tradeInitiatingParty: Party,
//        @field:Column(name = "trade_counterparty") val tradeCounterParty: Party,
//        @field:Column(name = "trade_status") val status: String
//
//        ) : PersistentState()
//
//}
