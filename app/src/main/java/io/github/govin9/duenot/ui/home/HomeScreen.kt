package io.github.govin9.duenot.ui.home

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.background
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.govin9.duenot.data.Card
import io.github.govin9.duenot.data.Payment
import io.github.govin9.duenot.ui.MainViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.FilterChip

enum class SortOption { DEFAULT, AZ, HIGHEST_DUE }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    navController: NavController
) {
    val cards by viewModel.allCards.collectAsState()
    val context = LocalContext.current
    
    var showPayDialog by remember { mutableStateOf<Card?>(null) }
    var showNewBillDialog by remember { mutableStateOf<Card?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<Card?>(null) }

    if (showPayDialog != null) {
        PayDialog(
            card = showPayDialog!!,
            onDismiss = { showPayDialog = null },
            onConfirm = { amount, note ->
                val payment = Payment(
                    cardId = showPayDialog!!.id,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    note = note
                )
                viewModel.recordPayment(payment)
                showPayDialog = null
                Toast.makeText(context, "Payment Recorded", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showNewBillDialog != null) {
        NewBillDialog(
            card = showNewBillDialog!!,
            onDismiss = { showNewBillDialog = null },
            onConfirm = { newAmount, newDate ->
                viewModel.recordNewBill(showNewBillDialog!!, newAmount, newDate)
                showNewBillDialog = null
                Toast.makeText(context, "New Bill Added", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showDeleteConfirmation != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Card") },
            text = { Text("Are you sure you want to delete ${showDeleteConfirmation!!.name}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteCard(showDeleteConfirmation!!)
                        showDeleteConfirmation = null
                        Toast.makeText(context, "Card Deleted", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    var searchQuery by remember { mutableStateOf("") }
    var isSearchActive by remember { mutableStateOf(false) }
    var sortOption by remember { mutableStateOf(SortOption.DEFAULT) }

    val filteredCards = if (searchQuery.isBlank()) {
        cards
    } else {
        cards.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.bankName.contains(searchQuery, ignoreCase = true) ||
            it.cardNumberLast4.contains(searchQuery)
        }
    }

    // Handle Back Press to close search
    androidx.activity.compose.BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
    }

    Scaffold(
        topBar = {
            if (isSearchActive) {
                // Search Bar
                val focusRequester = remember { FocusRequester() }
                LaunchedEffect(Unit) {
                    focusRequester.requestFocus()
                }

                TopAppBar(
                    title = {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search cards...") },
                            singleLine = true,
                            modifier = Modifier
                                .fillMaxWidth()
                                .focusRequester(focusRequester),
                            colors = androidx.compose.material3.TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(Icons.Default.Close, contentDescription = "Clear Search")
                                    }
                                }
                            }
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            isSearchActive = false
                            searchQuery = ""
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    )
                )
            } else {
                // Default Top Bar
                TopAppBar(
                    title = { Text("My Cards") },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background
                    ),
                    actions = {
                        IconButton(onClick = { isSearchActive = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Search Cards")
                        }
                    }
                )
            }
        },
        floatingActionButton = {
            if (!isSearchActive) {
                FloatingActionButton(
                    onClick = { navController.navigate("add_edit_card/-1") },
                    containerColor = MaterialTheme.colorScheme.secondary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Card")
                }
            }
        }
    ) { paddingValues ->
        if (cards.isEmpty() && !isSearchActive) {
            // Empty State
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.CreditCard,
                    contentDescription = null,
                    modifier = Modifier.size(100.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f) // Softer look
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Welcome! \uD83D\uDC4B", // Waving Hand Emoji
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Let's get your cards organized.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Spacer(modifier = Modifier.height(32.dp))
                Button(
                    onClick = { navController.navigate("add_edit_card/-1") },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("Add Your First Card")
                }
            }
        } else {
            val totalRemainingDue = filteredCards.sumOf { it.remainingDue }
            val cardsWithDue = filteredCards.count { it.remainingDue > 0 }

            val sortedCards = when (sortOption) {
                SortOption.DEFAULT -> filteredCards
                SortOption.AZ -> filteredCards.sortedBy { it.name.lowercase() }
                SortOption.HIGHEST_DUE -> filteredCards.sortedByDescending { it.remainingDue }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
            ) {
                item {
                    OverviewCard(
                        totalDue = totalRemainingDue,
                        cardsDueCount = cardsWithDue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 0.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = sortOption == SortOption.DEFAULT,
                            onClick = { sortOption = SortOption.DEFAULT },
                            label = { Text("Default") }
                        )
                        FilterChip(
                            selected = sortOption == SortOption.AZ,
                            onClick = { sortOption = SortOption.AZ },
                            label = { Text("A-Z") }
                        )
                        FilterChip(
                            selected = sortOption == SortOption.HIGHEST_DUE,
                            onClick = { sortOption = SortOption.HIGHEST_DUE },
                            label = { Text("Highest Due") }
                        )
                    }
                }
                items(sortedCards) { card ->
                    Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        CardItem(
                            card = card,
                            onPayClick = { showPayDialog = card },
                            onHistoryClick = { navController.navigate("card_history/${card.id}") },
                            onEditClick = { navController.navigate("add_edit_card/${card.id}") },
                            onDeleteClick = { showDeleteConfirmation = card },
                            onNewBillClick = { showNewBillDialog = card }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp)) // Bottom padding for FAB
                }
            }
        }
    }
}

@Composable
fun OverviewCard(
    totalDue: Double,
    cardsDueCount: Int,
    modifier: Modifier = Modifier
) {
    val numberFormat = java.text.NumberFormat.getNumberInstance(Locale("en", "IN"))

    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Total Remaining Due",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = "₹${numberFormat.format(totalDue)}",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "Cards Due",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                )
                Text(
                    text = "$cardsDueCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun CardItem(
    card: Card,
    onPayClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onNewBillClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat("dd MMM", Locale.getDefault())
    val numberFormat = java.text.NumberFormat.getNumberInstance(Locale("en", "IN"))

    Card(
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            modifier = Modifier.padding(12.dp) // Reduced padding
        ) {
            // Row 1: Header (Name & Bank) + Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = card.name.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${card.bankName.uppercase()} ${if (card.cardNumberLast4.isNotEmpty()) "• ${card.cardNumberLast4}" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                
                // Options Menu
                Box {
                    var expanded by remember { mutableStateOf(false) }
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(24.dp) // Compact icon button
                    ) {
                        Icon(
                            Icons.Default.MoreVert, 
                            contentDescription = "More Options",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        DropdownMenuItem(
                            text = { Text("Add New Bill") },
                            onClick = { expanded = false; onNewBillClick() },
                            leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("History") },
                            onClick = { expanded = false; onHistoryClick() },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            onClick = { expanded = false; onEditClick() },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                            onClick = { expanded = false; onDeleteClick() },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Content Row: Amounts (Left) and Action (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Amounts
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Total Bill
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Total: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "₹${numberFormat.format(card.totalDue)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Remaining Due
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Remaining: ",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "₹${numberFormat.format(card.remainingDue)}",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (card.remainingDue <= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                // Right Column: Due Date & Action
                val getStartOfDay = { timeInMillis: Long ->
                    val calendar = java.util.Calendar.getInstance()
                    calendar.timeInMillis = timeInMillis
                    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
                    calendar.set(java.util.Calendar.MINUTE, 0)
                    calendar.set(java.util.Calendar.SECOND, 0)
                    calendar.set(java.util.Calendar.MILLISECOND, 0)
                    calendar.timeInMillis
                }

                val normalizedCurrentDate = getStartOfDay(System.currentTimeMillis())
                val normalizedDueDate = getStartOfDay(card.dueDate)
                val diffInMillis = normalizedDueDate - normalizedCurrentDate
                val daysRemaining = java.util.concurrent.TimeUnit.MILLISECONDS.toDays(diffInMillis)
                val isOverdue = diffInMillis < 0
                val isNearDue = !isOverdue && daysRemaining <= 3
                
                // Color Logic: Red if Overdue OR Near Due (<= 3 days)
                val statusColor = if (card.remainingDue > 0) {
                    if (isOverdue || isNearDue) MaterialTheme.colorScheme.error 
                    else MaterialTheme.colorScheme.onSurface 
                } else {
                    MaterialTheme.colorScheme.primary
                }
                
                // Text Logic: Show "Overdue" if past due
                val dueText = if (isOverdue && card.remainingDue > 0) {
                    "Overdue: ${dateFormat.format(Date(card.dueDate))}"
                } else {
                    "Due: ${dateFormat.format(Date(card.dueDate))}"
                }

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = dueText,
                        style = MaterialTheme.typography.labelMedium,
                        color = statusColor,
                        fontWeight = FontWeight.Medium
                    )

                    if (card.remainingDue > 0) {
                        Button(
                            onClick = onPayClick,
                            modifier = Modifier.height(36.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 0.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Text(
                                text = "Pay Now",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.height(36.dp)
                        ) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = "Paid",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Paid",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PayDialog(
    card: Card,
    onDismiss: () -> Unit,
    onConfirm: (Double, String) -> Unit
) {
    // Initialize with selection covering the whole text
    var amountState by remember { 
        val text = card.remainingDue.toString()
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text = text,
                selection = androidx.compose.ui.text.TextRange(0, text.length)
            )
        ) 
    }
    var note by remember { mutableStateOf("") }
    
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Payment for ${card.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountState,
                    onValueChange = { amountState = it },
                    label = { Text("Amount") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)

                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (Optional)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = androidx.compose.ui.text.input.ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountDouble = amountState.text.toDoubleOrNull() ?: 0.0
                onConfirm(amountDouble, note)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun NewBillDialog(
    card: Card,
    onDismiss: () -> Unit,
    onConfirm: (Double, Long) -> Unit
) {
    var amountState by remember { 
        mutableStateOf(
            androidx.compose.ui.text.input.TextFieldValue(
                text = "",
            )
        ) 
    }
    var dueDate by remember { mutableStateOf(card.dueDate) }
    
    val context = LocalContext.current
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = dueDate
    
    val datePickerDialog = android.app.DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(year, month, dayOfMonth)
            dueDate = calendar.timeInMillis
        },
        calendar.get(java.util.Calendar.YEAR),
        calendar.get(java.util.Calendar.MONTH),
        calendar.get(java.util.Calendar.DAY_OF_MONTH)
    )
    
    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    
    val focusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    androidx.compose.runtime.LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Bill for ${card.name}") },
        text = {
            Column {
                OutlinedTextField(
                    value = amountState,
                    onValueChange = { amountState = it },
                    label = { Text("New Total Bill Amount") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal,
                        imeAction = androidx.compose.ui.text.input.ImeAction.Next
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                )
                Spacer(modifier = Modifier.height(16.dp))
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = dateFormat.format(Date(dueDate)),
                        onValueChange = { },
                        label = { Text("New Due Date") },
                        readOnly = true,
                        trailingIcon = {
                            IconButton(onClick = { datePickerDialog.show() }) {
                                Icon(androidx.compose.material.icons.Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { datePickerDialog.show() }
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val amountDouble = amountState.text.toDoubleOrNull() ?: 0.0
                onConfirm(amountDouble, dueDate)
            }) {
                Text("Add Bill")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
