package io.github.govin9.duenot.ui.history

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.govin9.duenot.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import io.github.govin9.duenot.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    navController: NavController,
    cardId: Int = -1
) {
    val history by if (cardId == -1) {
        viewModel.globalHistory.collectAsState()
    } else {
        viewModel.getCardPayments(cardId).collectAsState()
    }
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    
    val title = if (cardId == -1) "Global History" else "Payment History"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        if (history.isEmpty()) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
                verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange, // Or History icon
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "No transactions yet",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Your payment history and bill generations will appear here.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(history) { item ->
                    ListItem(
                        headlineContent = { 
                            Column {
                                val isBill = item.payment.type == io.github.govin9.duenot.data.PaymentType.BILL_GENERATED
                                val amountPrefix = if (isBill) "Bill: " else "Paid "
                                Text(
                                    text = "$amountPrefix$currencySymbol${CurrencyUtils.formatAmount(item.payment.amount)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isBill) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                                )
                                if (cardId == -1) { 
                                    Text(
                                        text = "${item.card.bankName} ${if (item.card.cardNumberLast4.isNotEmpty()) "• ${item.card.cardNumberLast4}" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                    )
                                }
                            }
                        },
                        supportingContent = { 
                            Column {
                                Text(
                                    text = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date(item.payment.date)),
                                    style = MaterialTheme.typography.bodySmall
                                )
                                if (!item.payment.note.isNullOrEmpty()) {
                                    Text(
                                        text = "Note: ${item.payment.note}",
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        },
                        leadingContent = {
                            val isBill = item.payment.type == io.github.govin9.duenot.data.PaymentType.BILL_GENERATED
                            Icon(
                                imageVector = if (isBill) Icons.Default.Description else Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = if (isBill) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                    Divider()
                }
            }
        }
    }
}
