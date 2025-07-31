@file:OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalAnimationApi::class,
    ExperimentalFoundationApi::class
)
package com.example.alarmclock

import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.text.SimpleDateFormat
import java.util.*

private val AppTypography = Typography()

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AlarmClockTheme {
                val vm: AlarmViewModel = viewModel()
                val alarms by vm.alarms.collectAsState()
                val currentTime by vm.currentTime.collectAsState()
                val isRinging by vm.isAlarmRinging.collectAsState()
                val ringingAlarm by vm.ringingAlarm.collectAsState()
                val showBedtimeDialog by vm.showBedtimeDialog.collectAsState()
                val editingAlarm by vm.editingAlarm.collectAsState()

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                Scaffold(
                    topBar = {
                        AnimatedVisibility(
                            visible = !isRinging,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            CenterAlignedTopAppBar(
                                title = {
                                    Text(
                                        text = "Alarm Clock",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                },
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                                )
                            )
                        }
                    },
                    floatingActionButton = {
                        AnimatedFAB(
                            extended = !isRinging,
                            vm = vm, // ✅ Pass the ViewModel here
                            onSet = { hour, minute, label, days, tone ->
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                }
                                vm.addAlarm(Alarm(time = cal, label = label, repeatDays = days, toneUri = tone))
                                scope.launch {
                                    snackbarHostState.showSnackbar("Alarm set at $hour:$minute")
                                }
                            }
                        )
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    containerColor = MaterialTheme.colorScheme.background
                ) { padding ->
                    Box(modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                        .padding(padding)) {

                        Column(modifier = Modifier.fillMaxSize()) {
                            AnimatedCurrentTime(currentTime)
                            AlarmList(
                                alarms = alarms,
                                onToggleAlarm = { alarm, enable ->
                                    vm.updateAlarm(alarm.copy(isEnabled = enable))
                                },
                                onEditAlarm = { alarm ->
                                    vm.editAlarm(alarm)
                                },
                                onDeleteAlarm = { alarmId -> vm.deleteAlarm(alarmId) }
                            )
                        }

                        if (isRinging && ringingAlarm != null) {
                            AlarmRingingDialog(
                                alarm = ringingAlarm!!,
                                onDismiss = { vm.dismissAlarm() },
                                onSnooze = { vm.snoozeAlarm() }
                            )
                        }

                        if (showBedtimeDialog) {
                            BedtimeDialog(onDismiss = { vm.toggleBedtimeDialog(false) })
                        }

                        if (editingAlarm != null) {
                            AlarmEditDialog(
                                alarm = editingAlarm!!,
                                vm = vm,
                                onSave = { updatedAlarm ->
                                    vm.updateAlarm(updatedAlarm)
                                    vm.editAlarm(null)
                                },
                                onDismiss = { vm.editAlarm(null) }
                            )
                        }
                    }
                    }
                }
            }
        }
    }

// ---------- Theme ----------
private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF9A5DEB),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color.White,
    background = Color(0xFF121212),
    onBackground = Color.White,
    surface = Color(0xFF1E1E1E),
    onSurface = Color.White,
    error = Color(0xFF9A5DEB),
    onError = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFFBB86FC),
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF6200EE),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF03DAC6),
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF018786),
    onSecondaryContainer = Color.White,
    background = Color(0xFFFFFBFE),
    onBackground = Color.Black,
    surface = Color(0xFFFFFBFE),
    onSurface = Color.Black,
    error = Color(0xFFB00020),
    onError = Color.White
)

@Composable
fun AlarmClockTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}

// ---------- ViewModel & Data ----------
data class Alarm(
    val id: String = UUID.randomUUID().toString(),
    val time: Calendar,
    val isEnabled: Boolean = true,
    val toneUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM),
    val label: String = "Alarm",
    val repeatDays: List<Boolean> = List(7) { false },
    val vibration: Boolean = true,
    val volume: Float = 0.8f
)

class AlarmViewModel : ViewModel() {
    private val _alarms = MutableStateFlow<List<Alarm>>(emptyList())
    val alarms: StateFlow<List<Alarm>> = _alarms

    private val _currentTime = MutableStateFlow(Calendar.getInstance())
    val currentTime: StateFlow<Calendar> = _currentTime

    private val _isAlarmRinging = MutableStateFlow(false)
    val isAlarmRinging: StateFlow<Boolean> = _isAlarmRinging

    private val _ringingAlarm = MutableStateFlow<Alarm?>(null)
    val ringingAlarm: StateFlow<Alarm?> = _ringingAlarm

    private val _showBedtimeDialog = MutableStateFlow(false)
    val showBedtimeDialog: StateFlow<Boolean> = _showBedtimeDialog

    private val _editingAlarm = MutableStateFlow<Alarm?>(null)
    val editingAlarm: StateFlow<Alarm?> = _editingAlarm

    private val alarmTones = listOf(
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM) to "Default Alarm",
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION) to "Default Notification",
        RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE) to "Default Ringtone",
        Uri.parse("android.resource://com.example.alarmclock/raw/alarm1") to "Beep Beep",
        Uri.parse("android.resource://com.example.alarmclock/raw/alarm2") to "Digital Alarm"
    )

    init {
        viewModelScope.launch {
            while (true) {
                delay(1000)
                _currentTime.value = Calendar.getInstance()
                checkAlarms()
            }
        }
    }

    fun addAlarm(alarm: Alarm) {
        _alarms.value = _alarms.value + alarm
    }

    fun updateAlarm(updated: Alarm) {
        _alarms.value = _alarms.value.map { if (it.id == updated.id) updated else it }
    }

    fun editAlarm(alarm: Alarm?) {
        _editingAlarm.value = alarm
    }

    fun deleteAlarm(id: String) {
        _alarms.value = _alarms.value.filterNot { it.id == id }
    }

    fun dismissAlarm() {
        _isAlarmRinging.value = false
        _ringingAlarm.value = null
    }

    fun snoozeAlarm() {
        val snoozed = Calendar.getInstance().apply { add(Calendar.MINUTE, 5) }
        _ringingAlarm.value?.let {
            addAlarm(it.copy(time = snoozed))
        }
        dismissAlarm()
    }

    fun toggleBedtimeDialog(show: Boolean) {
        _showBedtimeDialog.value = show
    }

    private fun checkAlarms() {
        if (_isAlarmRinging.value) return

        val now = Calendar.getInstance()
        val currentDay = now.get(Calendar.DAY_OF_WEEK) - 1 // Convert to 0-6 range

        _alarms.value.forEach { alarm ->
            if (alarm.isEnabled &&
                alarm.time.get(Calendar.HOUR_OF_DAY) == now.get(Calendar.HOUR_OF_DAY) &&
                alarm.time.get(Calendar.MINUTE) == now.get(Calendar.MINUTE) &&
                now.get(Calendar.SECOND) == 0 && // Only trigger at start of minute
                (alarm.repeatDays.none { it } || alarm.repeatDays[currentDay]) // Check if today is selected
            ) {
                _isAlarmRinging.value = true
                _ringingAlarm.value = alarm
                // Play alarm sound
                playAlarmSound(alarm.toneUri)
            }
        }
    }

    private fun playAlarmSound(uri: Uri) {
        // Implementation for playing alarm sound
        // You'll need to use MediaPlayer or similar here
    }

    fun getAlarmTones() = alarmTones
}

// ---------- UI Composables ----------

@Composable
fun AnimatedCurrentTime(currentTime: Calendar) {
    val formatter = remember { SimpleDateFormat("HH:mm:ss", Locale.getDefault()) }
    val dateFormatter = remember { SimpleDateFormat("EEE, MMM d", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(24.dp)
            )
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FlipClock(formatter.format(currentTime.time))

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                dateFormatter.format(currentTime.time).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun FlipClock(time: String) {
    val parts = time.split(":")

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        FlipDigit(parts[0][0].toString())
        FlipDigit(parts[0][1].toString())
        Text(":",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        FlipDigit(parts[1][0].toString())
        FlipDigit(parts[1][1].toString())
        Text(":",
            style = MaterialTheme.typography.displayLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer)
        FlipDigit(parts[2][0].toString())
        FlipDigit(parts[2][1].toString())
    }
}

@Composable
fun FlipDigit(digit: String, modifier: Modifier = Modifier) {
    var currentDigit by remember { mutableStateOf(digit) }
    var nextDigit by remember { mutableStateOf(digit) }
    var isFlipping by remember { mutableStateOf(false) }

    LaunchedEffect(digit) {
        if (digit != currentDigit) {
            nextDigit = digit
            isFlipping = true
            delay(300)
            currentDigit = digit
            isFlipping = false
        }
    }

    Box(
        modifier = modifier
            .padding(2.dp)
            .width(48.dp)
            .height(64.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationX = if (isFlipping) 90f else 0f
                }
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                currentDigit,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }

        if (isFlipping) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        rotationX = -90f
                    }
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    nextDigit,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }
}

@Composable
fun AlarmList(
    alarms: List<Alarm>,
    onToggleAlarm: (Alarm, Boolean) -> Unit,
    onEditAlarm: (Alarm) -> Unit,
    onDeleteAlarm: (String) -> Unit
) {
    if (alarms.isEmpty()) {
        EmptyAlarmsState()
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(alarms.sortedBy { it.time.timeInMillis }, key = { it.id }) { alarm ->
                var showDelete by remember { mutableStateOf(false) }

                if (showDelete) {
                    DeleteConfirmationDialog(
                        onConfirm = {
                            onDeleteAlarm(alarm.id)
                            showDelete = false
                        },
                        onDismiss = { showDelete = false }
                    )
                }

                AnimatedVisibility(
                    visible = true,
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut()
                ) {
                    AlarmItem(
                        alarm = alarm,
                        onToggleAlarm = onToggleAlarm,
                        onClick = { onEditAlarm(alarm) },
                        onLongPress = { showDelete = true }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete Alarm") },
        text = { Text("Are you sure you want to delete this alarm?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("DELETE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL")
            }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@Composable
fun EmptyAlarmsState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "No Alarms Set",
            fontSize = MaterialTheme.typography.headlineLarge.fontSize,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
        )
    }
}

@Composable
fun AlarmItem(
    alarm: Alarm,
    onToggleAlarm: (Alarm, Boolean) -> Unit,
    onClick: () -> Unit,
    onLongPress: () -> Unit
) {
    val timeFormat = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val days = listOf("S", "M", "T", "W", "T", "F", "S")

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onLongPress() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeFormat.format(alarm.time.time),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    if (alarm.repeatDays.any { it }) {
                        Row {
                            alarm.repeatDays.forEachIndexed { index, enabled ->
                                Text(
                                    text = days[index],
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (enabled)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.padding(horizontal = 2.dp)
                                )
                            }
                        }
                    }
                }
                Text(
                    text = alarm.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }

            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = { onToggleAlarm(alarm, it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.primary,
                    checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                    uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                    uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlarmEditDialog(
    alarm: Alarm,
    vm: AlarmViewModel,
    onSave: (Alarm) -> Unit,
    onDismiss: () -> Unit
) {
    val state = rememberTimePickerState(
        initialHour = alarm.time.get(Calendar.HOUR_OF_DAY),
        initialMinute = alarm.time.get(Calendar.MINUTE),
        is24Hour = false
    )
    var label by remember { mutableStateOf(alarm.label) }
    var days by remember { mutableStateOf(alarm.repeatDays) }
    var selectedTone by remember { mutableStateOf(alarm.toneUri) }
    val alarmTones = vm.getAlarmTones()
    val scrollState = rememberScrollState()
    var allFieldsValid by remember { mutableStateOf(false) }

    LaunchedEffect(label, selectedTone) {
        allFieldsValid = label.isNotBlank() && selectedTone != null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Alarm", style = MaterialTheme.typography.headlineSmall) },
        text = {
            Column(modifier = Modifier.fillMaxWidth().verticalScroll(scrollState)) {
                TimePicker(state = state)
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = label,
                    onValueChange = { label = it },
                    label = { Text("Label") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = label.isBlank()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Repeat Days", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                val dayLabels = listOf("S", "M", "T", "W", "T", "F", "S")
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    dayLabels.forEachIndexed { idx, day ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (days[idx]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .border(
                                    width = 2.dp,
                                    color = if (days[idx]) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .clickable { days = days.toMutableList().also { it[idx] = !it[idx] } },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day,
                                color = if (days[idx]) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = if (days[idx]) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text("Alarm Tone", style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Column {
                    alarmTones.forEach { (uri, name) ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth().clickable { selectedTone = uri }.padding(vertical = 8.dp)
                        ) {
                            RadioButton(selected = selectedTone == uri, onClick = { selectedTone = uri })
                            Text(name, modifier = Modifier.padding(start = 8.dp))
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (allFieldsValid) {
                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, state.hour)
                            set(Calendar.MINUTE, state.minute)
                            set(Calendar.SECOND, 0)
                        }
                        onSave(alarm.copy(time = cal, label = label, repeatDays = days, toneUri = selectedTone))
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = allFieldsValid
            ) {
                Text("SAVE")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCEL") }
        },
        containerColor = MaterialTheme.colorScheme.surface
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AnimatedFAB(
    extended: Boolean,
    vm: AlarmViewModel,
    onSet: (Int, Int, String, List<Boolean>, Uri) -> Unit
) {
    val context = LocalContext.current
    val ringtoneManager = remember { RingtoneManager(context).apply { setType(RingtoneManager.TYPE_ALARM) } }
    val alarmTones = remember {
        List(ringtoneManager.cursor.count) {
            ringtoneManager.getRingtoneUri(it)
        }
    }

    var showDialog by remember { mutableStateOf(false) }
    val timeState = rememberTimePickerState(initialHour = 7, initialMinute = 0, is24Hour = false)
    var label by remember { mutableStateOf("Alarm") }
    var days by remember { mutableStateOf(List(7) { false }) }
    var selectedTone by remember { mutableStateOf(alarmTones.firstOrNull() ?: Uri.EMPTY) }

    val dayNames = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")

    AnimatedVisibility(
        visible = extended,
        enter = fadeIn() + scaleIn(),
        exit = fadeOut() + scaleOut()
    ) {
        FloatingActionButton(
            onClick = { showDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Alarm")
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Set Alarm") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.verticalScroll(rememberScrollState())
                ) {
                    TimePicker(state = timeState)

                    OutlinedTextField(
                        value = label,
                        onValueChange = { label = it },
                        label = { Text("Label") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text("Repeat on", style = MaterialTheme.typography.titleMedium)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        dayNames.forEachIndexed { index, day ->
                            val selected = days[index]
                            val bgColor =
                                if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                            val textColor =
                                if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(bgColor)
                                    .clickable {
                                        days = days.toMutableList().apply {
                                            set(index, !get(index))
                                        }
                                    }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(text = day, color = textColor)
                            }
                        }
                    }

                    Text("Alarm Tone", style = MaterialTheme.typography.titleMedium)
                    LazyColumn(
                        modifier = Modifier
                            .height(150.dp)
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                            .padding(4.dp)
                    ) {
                        items(alarmTones) { toneUri ->
                            val ringtone = RingtoneManager.getRingtone(context, toneUri)
                            val title = ringtone?.getTitle(context) ?: "Unknown"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedTone = toneUri }
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = selectedTone == toneUri,
                                    onClick = { selectedTone = toneUri }
                                )
                                Text(text = title, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    onSet(timeState.hour, timeState.minute, label, days, selectedTone)
                    showDialog = false
                }) {
                    Text("Set")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun AlarmRingingDialog(alarm: Alarm, onDismiss: () -> Unit, onSnooze: () -> Unit) {
    val format = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }
    val infiniteTransition = rememberInfiniteTransition()
    val pulse by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Dialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Card(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .scale(pulse),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Alarm,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    "ALARM",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    alarm.label,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                )
                Text(
                    format.format(alarm.time.time),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                Spacer(Modifier.height(24.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = onSnooze,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(Icons.Default.Snooze, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                    }
                    Button(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BedtimeDialog(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(32.dp),
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    "Bedtime Schedule",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(modifier = Modifier.height(16.dp))
                // Bedtime scheduling UI would go here
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("DONE")
                }
            }
        }
    }
}
