package com.es.note.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.es.note.room.entity.Note
import kotlinx.coroutines.flow.Flow

@Dao
interface NoteDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: Note): Long

    @Update
    suspend fun update(item: Note)

    @Delete
    suspend fun delete(item: Note)

    @Query("SELECT * from note WHERE id = :id")
    fun getItem(id: Long): Note?

    @Query("SELECT * from note WHERE id = :id")
    fun getItemStream(id: Long): Flow<Note>

    @Query("SELECT * from note ORDER BY id ASC")
    fun getAllItemsStream(): Flow<List<Note>>
}