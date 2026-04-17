package io.github.govin9.duenot.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.DateRange
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream
import io.github.govin9.duenot.ui.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: MainViewModel, navController: NavController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val themeMode by viewModel.themeMode.collectAsState()
    val currencySymbol by viewModel.currencySymbol.collectAsState()
    val reminderDays by viewModel.reminderDaysBefore.collectAsState()
    val reminderTime by viewModel.reminderTime.collectAsState()
    val dateFormat by viewModel.dateFormat.collectAsState()

    // --- Backup & Restore Launchers ---
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                val jsonString = viewModel.exportDataToJson()
                if (jsonString.isNotEmpty()) {
                    try {
                        context.contentResolver.openOutputStream(it)?.use { outputStream ->
                            outputStream.write(jsonString.toByteArray())
                            Toast.makeText(context, "Backup Saved Successfully", Toast.LENGTH_SHORT).show()
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(context, "Failed to save backup", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "No data to backup", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            coroutineScope.launch {
                try {
                    val jsonString = context.contentResolver.openInputStream(it)?.bufferedReader().use { reader ->
                        reader?.readText()
                    }
                    if (!jsonString.isNullOrEmpty()) {
                        viewModel.importDataFromJson(
                            jsonString = jsonString,
                            onSuccess = {
                                Toast.makeText(context, "Data Restored Successfully", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(context, "Restore failed: $error", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Failed to read file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(context, "Error reading file", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    // ----------------------------------

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
        ) {
            // Appearance & General Section
            SettingsSectionHeader("Appearance & General")
            
            ThemeSettingItem(
                currentTheme = themeMode,
                onThemeSelected = { viewModel.setThemeMode(it) }
            )
            
            Divider(modifier = Modifier.padding(horizontal = 16.dp))
            
            CurrencySettingItem(
                currentCurrency = currencySymbol,
                onCurrencySelected = { viewModel.setCurrencySymbol(it) }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            DateFormatSettingItem(
                currentFormat = dateFormat,
                onFormatSelected = { viewModel.setDateFormat(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Notifications & Reminders Section
            SettingsSectionHeader("Notifications & Reminders")

            ReminderDaysSettingItem(
                currentDays = reminderDays,
                onDaysSelected = { viewModel.setReminderDaysBefore(it) }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            ReminderTimeSettingItem(
                currentTime = reminderTime,
                onTimeSelected = { viewModel.setReminderTime(it) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Data & Backup Section
            SettingsSectionHeader("Data & Backup")

            SettingsActionItem(
                icon = Icons.Default.FileDownload,
                title = "Export Data",
                subtitle = "Save a local offline backup",
                onClick = { exportLauncher.launch("DueNot_Backup.json") }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsActionItem(
                icon = Icons.Default.FileUpload,
                title = "Import Data",
                subtitle = "Restore from an offline backup",
                onClick = { importLauncher.launch(arrayOf("application/json", "*/*")) }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // About Section
            SettingsSectionHeader("About")

            val versionName = remember(context) {
                try {
                    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "Unknown"
                } catch (e: Exception) {
                    "Unknown"
                }
            }

            SettingsActionItem(
                icon = Icons.Default.Info,
                title = "App Version",
                subtitle = versionName,
                onClick = { /* No action needed */ }
            )

            Divider(modifier = Modifier.padding(horizontal = 16.dp))

            SettingsActionItem(
                icon = Icons.Default.Code,
                title = "Source Code",
                subtitle = "View on GitHub",
                onClick = { 
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/govin9/duenot"))
                    context.startActivity(intent)
                }
            )
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }
}

@Composable
fun SettingsActionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = icon, 
                contentDescription = title,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = subtitle, 
                    style = MaterialTheme.typography.bodyMedium, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}

@Composable
fun ThemeSettingItem(currentTheme: String, onThemeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("system" to "System Default", "light" to "Light", "dark" to "Dark")
    val currentLabel = options.find { it.first == currentTheme }?.second ?: "System Default"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.ColorLens, 
                    contentDescription = "Theme",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "App Theme", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = currentLabel, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Theme")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onThemeSelected(key)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentTheme == key) {
                            Icon(Icons.Default.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CurrencySettingItem(currentCurrency: String, onCurrencySelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("₹" to "Indian Rupee (₹)", "$" to "US Dollar ($)", "€" to "Euro (€)", "£" to "British Pound (£)")
    val currentLabel = options.find { it.first == currentCurrency }?.second ?: "Indian Rupee (₹)"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AttachMoney, 
                    contentDescription = "Currency",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Currency Symbol", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = currentLabel, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Currency")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onCurrencySelected(key)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentCurrency == key) {
                            Icon(Icons.Default.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderDaysSettingItem(currentDays: String, onDaysSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(
        "0" to "On the due date",
        "1" to "1 day before",
        "2" to "2 days before",
        "3" to "3 days before",
        "5" to "5 days before",
        "7" to "1 week before"
    )
    val currentLabel = options.find { it.first == currentDays }?.second ?: "3 days before"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Notifications, 
                    contentDescription = "Reminder Days",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Due Date Reminder", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = currentLabel, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Reminder Days")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onDaysSelected(key)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentDays == key) {
                            Icon(Icons.Default.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun ReminderTimeSettingItem(currentTime: String, onTimeSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    
    // Generating some common time options
    val options = listOf(
        "08:00" to "08:00 AM",
        "09:00" to "09:00 AM",
        "10:00" to "10:00 AM",
        "12:00" to "12:00 PM",
        "15:00" to "03:00 PM",
        "18:00" to "06:00 PM",
        "20:00" to "08:00 PM"
    )
    val currentLabel = options.find { it.first == currentTime }?.second ?: "09:00 AM"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime, 
                    contentDescription = "Reminder Time",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Reminder Time", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = currentLabel, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Reminder Time")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onTimeSelected(key)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentTime == key) {
                            Icon(Icons.Default.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun DateFormatSettingItem(currentFormat: String, onFormatSelected: (String) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf("dd/MM/yyyy" to "DD/MM/YYYY (e.g. 31/12/2026)", "MM/dd/yyyy" to "MM/DD/YYYY (e.g. 12/31/2026)")
    val currentLabel = options.find { it.first == currentFormat }?.second ?: "DD/MM/YYYY (e.g. 31/12/2026)"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = true }
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.DateRange, 
                    contentDescription = "Date Format",
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = "Date Format", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = currentLabel, 
                        style = MaterialTheme.typography.bodyMedium, 
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(Icons.Default.ArrowDropDown, contentDescription = "Select Date Format")
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (key, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onFormatSelected(key)
                        expanded = false
                    },
                    trailingIcon = {
                        if (currentFormat == key) {
                            Icon(Icons.Default.Check, contentDescription = "Selected")
                        }
                    }
                )
            }
        }
    }
}

