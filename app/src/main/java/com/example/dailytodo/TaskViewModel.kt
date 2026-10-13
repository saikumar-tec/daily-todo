package com.example.dailytodo

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.dailytodo.data.AppDatabase
import com.example.dailytodo.data.Task
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class TaskViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = AppDatabase.get(app).taskDao()

    private val _day = MutableStateFlow(LocalDate.now())
    val day: StateFlow<LocalDate> = _day

    val tasks: StateFlow<List<Task>> = _day
        .flatMapLatest { dao.observeDay(it.toEpochDay()) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun goTo(date: LocalDate) { _day.value = date }
    fun shift(days: Long) { _day.value = _day.value.plusDays(days) }

    fun add(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        viewModelScope.launch { dao.insert(Task(title = t, dateEpochDay = _day.value.toEpochDay())) }
    }

    fun toggle(task: Task) = viewModelScope.launch { dao.update(task.copy(done = !task.done)) }
    fun delete(task: Task) = viewModelScope.launch { dao.delete(task) }
    fun clearCompleted() = viewModelScope.launch { dao.clearCompleted(_day.value.toEpochDay()) }

    /** Pull unfinished tasks from the previous day into the selected day. */
    fun carryOverFromYesterday() = viewModelScope.launch {
        dao.carryOver(_day.value.minusDays(1).toEpochDay(), _day.value.toEpochDay())
    }
}
