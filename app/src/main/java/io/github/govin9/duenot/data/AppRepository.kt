package io.github.govin9.duenot.data

import kotlinx.coroutines.flow.Flow

class AppRepository(private val cardDao: CardDao, private val paymentDao: PaymentDao) {
    val allCards: Flow<List<Card>> = cardDao.getAllCards()
    
    suspend fun getCardById(id: Int): Card? = cardDao.getCardById(id)
    
    suspend fun insertCard(card: Card): Long = cardDao.insertCard(card)
    
    suspend fun updateCard(card: Card) = cardDao.updateCard(card)
    
    suspend fun deleteCard(card: Card) = cardDao.deleteCard(card)
    
    suspend fun recordPayment(payment: Payment) {
        paymentDao.insertPayment(payment)
        cardDao.recordPaymentUpdate(payment.cardId, payment.amount)
    }
    
    suspend fun insertPaymentOnly(payment: Payment) {
        paymentDao.insertPayment(payment)
    }
    
    fun getPaymentsForCard(cardId: Int): Flow<List<PaymentWithCard>> = paymentDao.getPaymentsForCard(cardId)
    
    fun getAllPayments(): Flow<List<PaymentWithCard>> = paymentDao.getAllPayments()

    // Backup & Restore
    suspend fun exportData(): BackupData {
        val cards = cardDao.getAllCardsSync()
        val payments = paymentDao.getAllPaymentsSync()
        return BackupData(cards, payments)
    }

    suspend fun importData(backupData: BackupData) {
        // Clear existing data
        cardDao.deleteAllCards()
        paymentDao.deleteAllPayments()
        
        // Insert new data
        cardDao.insertCards(backupData.cards)
        paymentDao.insertPayments(backupData.payments)
    }
}

data class BackupData(
    val cards: List<Card> = emptyList(),
    val payments: List<Payment> = emptyList()
)
