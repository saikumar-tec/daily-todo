package com.example.dailytodo.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.dailytodo.TaskViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val fmt = DateTimeFormatter.ofPattern("EEEE, MMM d")

private fun label(d: LocalDate): String {
    val today = LocalDate.now()
    return when (d) {
        today -> "Today"
        today.plusDays(1) -> "Tomorrow"
        today.minusDays(1) -> "Yesterday"
        else -> d.format(fmt)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TodoScreen(vm: TaskViewModel) {
    val day by vm.day.collectAsStateWithLifecycle()
    val tasks by vm.tasks.collectAsStateWithLifecycle()
    var text by remember { mutableStateOf("") }
    val doneCount = tasks.count { it.done }

    fun submit() { vm.add(text); text = "" }

    Scaffold(
        modifier = Modifier.safeDrawingPadding(),
        topBar = {
            Column {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(label(day), style = MaterialTheme.typography.titleLarge)
                            if (label(day) != day.format(fmt))
                                Text(day.format(fmt), style = MaterialTheme.typography.bodySmall)
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = { vm.shift(-1) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Previous day")
                        }
                    },
                    actions = {
                        IconButton(onClick = { vm.shift(1) }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, "Next day")
                        }
                    }
                )
                if (tasks.isNotEmpty()) {
                    LinearProgressIndicator(
                        progress = { doneCount / tasks.size.toFloat() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        bottomBar = {
            Row(
                Modifier.fillMaxWidth().padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Add a task…") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { submit() })
                )
                Spacer(Modifier.width(8.dp))
                FilledIconButton(onClick = ::submit, enabled = text.isNotBlank()) {
                    Icon(Icons.Default.Add, "Add task")
                }
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (tasks.isEmpty()) "No tasks yet" else "$doneCount of ${tasks.size} done",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = { vm.carryOverFromYesterday() }) { Text("Carry over") }
                if (doneCount > 0) TextButton(onClick = { vm.clearCompleted() }) { Text("Clear done") }
            }

            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(bottom = 8.dp)) {
                items(tasks, key = { it.id }) { task ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(checked = task.done, onCheckedChange = { vm.toggle(task) })
                        Text(
                            task.title,
                            modifier = Modifier.weight(1f).alpha(if (task.done) 0.5f else 1f),
                            textDecoration = if (task.done) TextDecoration.LineThrough else null,
                            style = MaterialTheme.typography.bodyLarge
                        )
                        IconButton(onClick = { vm.delete(task) }) {
                            Icon(Icons.Default.Delete, "Delete task")
                        }
                    }
                }
            }
        }
    }
}
