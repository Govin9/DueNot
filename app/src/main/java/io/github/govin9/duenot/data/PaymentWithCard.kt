package io.github.govin9.duenot.data

import androidx.room.Embedded
import androidx.room.Relation

data class PaymentWithCard(
    @Embedded val payment: Payment,
    @Relation(
        parentColumn = "cardId",
        entityColumn = "id"
    )
    val card: Card
)
