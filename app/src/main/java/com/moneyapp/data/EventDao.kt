import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface EventDao {
    @Insert
    suspend fun insert(event: Event): Long

    @Query("SELECT * FROM events WHERE id = :id")
    suspend fun getById(id: String): Event?

    @Query("SELECT * FROM events ORDER BY createdAt DESC")
    suspend fun getAll(): List<Event>

    @Update
    suspend fun update(event: Event)

    @Delete
    suspend fun delete(event: Event)
}
