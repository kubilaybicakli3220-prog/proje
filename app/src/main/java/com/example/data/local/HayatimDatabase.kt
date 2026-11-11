package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "local_tasks")
data class LocalTask(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val dueDate: String,
    val dueTime: String,
    val isCompleted: Boolean,
    val reminderEnabled: Boolean
)

@Entity(tableName = "local_transactions")
data class LocalTransaction(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val amount: Double,
    val type: String,
    val category: String,
    val date: String,
    val note: String
)

@Entity(tableName = "local_notes")
data class LocalNote(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val content: String,
    val category: String,
    val isFavorite: Boolean
)

@Dao
interface TaskDao {
    @Query("SELECT * FROM local_tasks WHERE userId = :userId ORDER BY dueDate ASC")
    fun getTasks(userId: String): Flow<List<LocalTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<LocalTask>)

    @Query("DELETE FROM local_tasks WHERE id = :id")
    suspend fun deleteTask(id: String)
}

@Dao
interface TransactionDao {
    @Query("SELECT * FROM local_transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactions(userId: String): Flow<List<LocalTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactions(transactions: List<LocalTransaction>)

    @Query("DELETE FROM local_transactions WHERE id = :id")
    suspend fun deleteTransaction(id: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM local_notes WHERE userId = :userId")
    fun getNotes(userId: String): Flow<List<LocalNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotes(notes: List<LocalNote>)

    @Query("DELETE FROM local_notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}

@Database(
    entities = [LocalTask::class, LocalTransaction::class, LocalNote::class],
    version = 1,
    exportSchema = false
)
abstract class HayatimDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun transactionDao(): TransactionDao
    abstract fun noteDao(): NoteDao
}
