package io.github.govin9.duenot.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.govin9.duenot.data.AppRepository
import io.github.govin9.duenot.data.Card
import io.github.govin9.duenot.data.Payment
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel(private val repository: AppRepository) : ViewModel() {
    val allCards: StateFlow<List<Card>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
        
    suspend fun getCardById(id: Int): Card? {
        return repository.getCardById(id)
    }

    fun addCard(card: Card) = viewModelScope.launch {
        val newCardId = repository.insertCard(card)
        // Record "Bill Generated" event for the initial bill ONLY if there is a due amount
        if (card.totalDue > 0.0) {
            val billPayment = Payment(
                cardId = newCardId.toInt(),
                amount = card.totalDue,
                date = System.currentTimeMillis(),
                type = io.github.govin9.duenot.data.PaymentType.BILL_GENERATED,
                note = "Initial Bill"
            )
            repository.insertPaymentOnly(billPayment)
        }
    }
    
    fun updateCard(card: Card) = viewModelScope.launch {
        repository.updateCard(card)
    }
    
    fun deleteCard(card: Card) = viewModelScope.launch {
        repository.deleteCard(card)
    }
    
    fun recordPayment(payment: Payment) = viewModelScope.launch {
        repository.recordPayment(payment)
    }

    fun recordNewBill(card: Card, newAmount: Double, newDate: Long) = viewModelScope.launch {
        // Update Card
        val updatedCard = card.copy(
            totalDue = newAmount,
            remainingDue = newAmount,
            dueDate = newDate,
            statementDate = System.currentTimeMillis()
        )
        repository.updateCard(updatedCard)

        // Record Bill Generated Event
        val billPayment = Payment(
            cardId = card.id,
            amount = newAmount,
            date = System.currentTimeMillis(),
            type = io.github.govin9.duenot.data.PaymentType.BILL_GENERATED,
            note = "Bill Generated"
        )
        repository.insertPaymentOnly(billPayment)
    }
    
    fun getCardPayments(cardId: Int): StateFlow<List<io.github.govin9.duenot.data.PaymentWithCard>> {
        // Note: For simplicity in this demo, accessing flow directly. 
        // In real app, might want to manage this better or use a separate VM per screen.
        return repository.getPaymentsForCard(cardId)
            .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
    }

    val globalHistory: StateFlow<List<io.github.govin9.duenot.data.PaymentWithCard>> = repository.getAllPayments()
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())
}

class MainViewModelFactory(private val repository: AppRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
