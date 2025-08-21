/*
 * BSD 3-Clause License
 *
 * Copyright (c) 2025, pat733
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

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