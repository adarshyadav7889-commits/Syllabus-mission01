package com.adarsh.syllabusmission.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow

/** One row = one completed chapter + the calendar day (epoch day) it was completed. */
@Entity(tableName = "completions")
data class Completion(
    @PrimaryKey val chapterId: String,
    val completedOn: Long
)

@Dao
interface CompletionDao {
    @Query("SELECT * FROM completions")
    fun observeAll(): Flow<List<Completion>>

    @Query("SELECT * FROM completions WHERE chapterId = :id")
    suspend fun find(id: String): Completion?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(completion: Completion)

    @Query("DELETE FROM completions WHERE chapterId = :id")
    suspend fun delete(id: String)
}

@Database(entities = [Completion::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): CompletionDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun get(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "syllabus.db"
                ).build().also { instance = it }
            }
    }
}

class Repository(private val db: AppDatabase) {
    private val dao = db.dao()

    fun observe(): Flow<List<Completion>> = dao.observeAll()

    /** Completed -> incomplete (row deleted). Incomplete -> completed today. Saved immediately. */
    suspend fun toggle(chapterId: String, todayEpochDay: Long) {
        db.withTransaction {
            if (dao.find(chapterId) != null) {
                dao.delete(chapterId)
            } else {
                dao.insert(Completion(chapterId, todayEpochDay))
            }
        }
    }
}
