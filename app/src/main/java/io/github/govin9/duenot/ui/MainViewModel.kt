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
import com.google.gson.Gson
import io.github.govin9.duenot.data.BackupData

import io.github.govin9.duenot.data.UserPreferencesRepository

class MainViewModel(
    private val repository: AppRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    val allCards: StateFlow<List<Card>> = repository.allCards
        .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val themeMode: StateFlow<String> = userPreferencesRepository.themeModeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "system")

    val currencySymbol: StateFlow<String> = userPreferencesRepository.currencySymbolFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "₹")

    val reminderDaysBefore: StateFlow<String> = userPreferencesRepository.reminderDaysBeforeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "3")

    val reminderTime: StateFlow<String> = userPreferencesRepository.reminderTimeFlow
        .stateIn(viewModelScope, SharingStarted.Lazily, "09:00")

    fun setThemeMode(mode: String) = viewModelScope.launch {
        userPreferencesRepository.setThemeMode(mode)
    }

    fun setCurrencySymbol(symbol: String) = viewModelScope.launch {
        userPreferencesRepository.setCurrencySymbol(symbol)
    }

    fun setReminderDaysBefore(days: String) = viewModelScope.launch {
        userPreferencesRepository.setReminderDaysBefore(days)
    }

    fun setReminderTime(time: String) = viewModelScope.launch {
        userPreferencesRepository.setReminderTime(time)
    }

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

    // --- Backup and Restore ---
    
    suspend fun exportDataToJson(): String {
        return try {
            val backupData = repository.exportData()
            Gson().toJson(backupData)
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    fun importDataFromJson(jsonString: String, onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val backupData = Gson().fromJson(jsonString, BackupData::class.java)
                if (backupData != null) {
                    repository.importData(backupData)
                    onSuccess()
                } else {
                    onError("Invalid Backup File")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onError(e.message ?: "Failed to import data")
            }
        }
    }
}

class MainViewModelFactory(
    private val repository: AppRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(repository, userPreferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
