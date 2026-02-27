package io.github.govin9.duenot.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CardDao {
    @Query("SELECT * FROM cards")
    fun getAllCards(): Flow<List<Card>>

    @Query("SELECT * FROM cards WHERE id = :id")
    suspend fun getCardById(id: Int): Card?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCard(card: Card): Long

    @Update
    suspend fun updateCard(card: Card)

    @Delete
    suspend fun deleteCard(card: Card)
    
    @Query("UPDATE cards SET remainingDue = remainingDue - :paymentAmount WHERE id = :cardId")
    suspend fun recordPaymentUpdate(cardId: Int, paymentAmount: Double)
}
