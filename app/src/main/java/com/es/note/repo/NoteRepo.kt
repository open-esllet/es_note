package com.es.note.repo

import com.es.note.room.entity.Folder
import com.es.note.room.entity.Note
import kotlinx.coroutines.flow.Flow

interface NoteRepo {
    fun getAllFoldersStream(): Flow<List<Folder>>
    fun getFolderStream(id: Long): Flow<Folder?>
    suspend fun insertFolder(item: Folder)
    suspend fun deleteFolder(item: Folder)
    suspend fun updateFolder(item: Folder)

    fun getNoteStream(id: Long): Flow<Note?>
    suspend fun getNote(id:Long): Note?
    suspend fun insertNote(item: Note): Long
    suspend fun deleteNote(item: Note)
    suspend fun updateNote(item: Note)
}