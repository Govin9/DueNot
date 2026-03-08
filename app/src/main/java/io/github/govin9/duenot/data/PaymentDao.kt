package io.github.govin9.duenot.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PaymentDao {
    @androidx.room.Transaction
    @Query("SELECT * FROM payments WHERE cardId = :cardId ORDER BY date DESC")
    fun getPaymentsForCard(cardId: Int): Flow<List<PaymentWithCard>>

    @androidx.room.Transaction
    @Query("SELECT * FROM payments ORDER BY date DESC")
    fun getAllPayments(): Flow<List<PaymentWithCard>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayment(payment: Payment)
    
    @Query("SELECT * FROM payments")
    suspend fun getAllPaymentsSync(): List<Payment>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayments(payments: List<Payment>)
    
    @Query("DELETE FROM payments")
    suspend fun deleteAllPayments(): Int
}
