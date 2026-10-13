package com.example.dailytodo.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE dateEpochDay = :day ORDER BY done ASC, id ASC")
    fun observeDay(day: Long): Flow<List<Task>>

    @Insert
    suspend fun insert(task: Task)

    @Update
    suspend fun update(task: Task)

    @Delete
    suspend fun delete(task: Task)

    @Query("DELETE FROM tasks WHERE dateEpochDay = :day AND done = 1")
    suspend fun clearCompleted(day: Long)

    // Move unfinished tasks from one day to another
    @Query("UPDATE tasks SET dateEpochDay = :to WHERE dateEpochDay = :from AND done = 0")
    suspend fun carryOver(from: Long, to: Long)
}
