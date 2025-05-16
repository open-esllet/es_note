package com.es.note.repo

import com.es.note.room.dao.FolderDao
import com.es.note.room.dao.NoteDao
import com.es.note.room.entity.Folder
import com.es.note.room.entity.Note
import kotlinx.coroutines.flow.Flow

class OfflineRepo(private val folderDao: FolderDao, private val noteDao: NoteDao) : NoteRepo {
    override fun getAllFoldersStream(): Flow<List<Folder>> {
        return folderDao.getAllItemsStream()
    }

    override fun getFolderStream(id: Long): Flow<Folder?> {
        return folderDao.getItemStream(id)
    }

    override suspend fun insertFolder(item: Folder) {
        return folderDao.insert(item)
    }

    override suspend fun deleteFolder(item: Folder) {
        return folderDao.delete(item)
    }

    override suspend fun updateFolder(item: Folder) {
        return folderDao.update(item)
    }

    override suspend fun getNote(id: Long): Note? {
        return noteDao.getItem(id)
    }

    override fun getNoteStream(id: Long): Flow<Note?> {
        return noteDao.getItemStream(id)
    }

    override suspend fun insertNote(item: Note): Long {
        return noteDao.insert(item)
    }

    override suspend fun deleteNote(item: Note) {
        return noteDao.delete(item)
    }

    override suspend fun updateNote(item: Note) {
        return noteDao.update(item)
    }
}