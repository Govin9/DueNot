package io.github.govin9.duenot.data

import androidx.annotation.Keep
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Keep
@Entity(tableName = "cards")
data class Card(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val bankName: String,
    val cardNumberLast4: String, // Optional, for display
    val dueDate: Long, // Timestamp
    val statementDate: Long, // Timestamp
    val totalDue: Double,
    val minDue: Double,
    val remainingDue: Double,
    val colorHex: String? = null // For UI theming per card
)
