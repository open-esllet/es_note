package com.es.note.room.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.es.note.room.entity.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FolderDao {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(item: Folder)

    @Update
    suspend fun update(item: Folder)

    @Delete
    suspend fun delete(item: Folder)

    @Query("SELECT * from folder WHERE id = :id")
    fun getItemStream(id: Long): Flow<Folder>

    @Query("SELECT * from folder ORDER BY id ASC")
    fun getAllItemsStream(): Flow<List<Folder>>
}