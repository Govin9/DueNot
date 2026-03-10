package io.github.govin9.duenot.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Keep
@Entity(
    tableName = "payments",
    foreignKeys = [
        ForeignKey(
            entity = Card::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [androidx.room.Index(value = ["cardId"])]
)
data class Payment(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val cardId: Int,
    val amount: Double,
    val date: Long,
    val note: String? = null,
    val type: PaymentType = PaymentType.PAYMENT // Default to PAYMENT for backward compatibility
)

@Keep
enum class PaymentType {
    PAYMENT,
    BILL_GENERATED
}
