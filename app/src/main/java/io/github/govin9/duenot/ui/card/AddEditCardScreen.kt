package io.github.govin9.duenot.ui.card

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Numbers
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.govin9.duenot.data.Card
import io.github.govin9.duenot.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import io.github.govin9.duenot.util.CurrencyUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditCardScreen(
    viewModel: MainViewModel,
    navController: NavController,
    cardId: Int
) {
    var name by remember { mutableStateOf("") }
    var bankName by remember { mutableStateOf("") }
    var last4 by remember { mutableStateOf("") }
    var totalDue by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf(System.currentTimeMillis()) }
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val dateFormatString by viewModel.dateFormat.collectAsState()
    
    val context = LocalContext.current
    val calendar = Calendar.getInstance()
    
    LaunchedEffect(cardId) {
        if (cardId != -1) {
            val card = viewModel.getCardById(cardId) // Need to add this to VM
            card?.let {
                name = it.name
                bankName = it.bankName
                last4 = it.cardNumberLast4
                totalDue = CurrencyUtils.formatAmount(it.totalDue)
                dueDate = it.dueDate
            }
        }
    }

    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            dueDate = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )
    
    val dateFormat = SimpleDateFormat(dateFormatString, Locale.getDefault())

    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(Unit) {
        if (cardId == -1) {
            focusRequester.requestFocus()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (cardId == -1) "Add Card" else "Edit Card") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = bankName,
                onValueChange = { bankName = it },
                label = { Text("Bank Name (e.g. HDFC)") },
                leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Card Name (e.g. Platinum)") },
                leadingIcon = { Icon(Icons.Default.CreditCard, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Next)
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = last4,
                onValueChange = { if (it.length <= 4) last4 = it },
                label = { Text("Last 4 Digits") },
                leadingIcon = { Icon(Icons.Default.Numbers, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = totalDue,
                onValueChange = { totalDue = it },
                label = { Text("Total Due Amount") },
                leadingIcon = { Text(currencySymbol, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = androidx.compose.ui.text.input.ImeAction.Done
                ),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = dateFormat.format(Date(dueDate)),
                    onValueChange = { },
                    label = { Text("Due Date") },
                    readOnly = true,
                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                // Transparent box to capture clicks over the text field
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { datePickerDialog.show() }
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Button(
                onClick = {
                    val amount = totalDue.toDoubleOrNull() ?: 0.0
                    val card = Card(
                        id = if (cardId == -1) 0 else cardId,
                        name = name,
                        bankName = bankName,
                        cardNumberLast4 = last4,
                        dueDate = dueDate,
                        statementDate = System.currentTimeMillis(), // Defaulting for now
                        totalDue = amount,
                        minDue = 0.0,
                        remainingDue = amount // Reset to total due on save? logic assumes full update.
                    )
                    
                    if (cardId == -1) {
                        viewModel.addCard(card)
                    } else {
                        viewModel.updateCard(card)
                    }
                    navController.popBackStack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (cardId == -1) "Add Card" else "Save Changes",
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
    }
}
