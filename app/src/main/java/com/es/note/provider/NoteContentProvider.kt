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

package com.es.note.provider

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import com.es.note.room.NoteDb
import com.es.note.utils.Config
import com.es.note.utils.LogUtil

class NoteContentProvider : ContentProvider() {

    companion object {
        const val TAG = "NoteContentProvider"
        const val AUTHORITY = "com.es.note.provider"
        const val TABLE_NOTE = "note"
        const val TABLE_LOCK = "lock"
        const val TABLE_FOLDER = "folder"
        const val ITEM_NOTE = 1
        const val ITEM_NOTE_ID = 2
        const val ITEM_LOCK = 3
        const val ITEM_FOLDER = 4
        const val ITEM_FOLDER_ID = 5
    }

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    private lateinit var openHelper: SupportSQLiteOpenHelper

    init {
        uriMatcher.addURI(AUTHORITY, TABLE_NOTE, ITEM_NOTE)
        uriMatcher.addURI(AUTHORITY, "$TABLE_NOTE/#", ITEM_NOTE_ID)
        uriMatcher.addURI(AUTHORITY, TABLE_LOCK, ITEM_LOCK)
        uriMatcher.addURI(AUTHORITY, TABLE_FOLDER, ITEM_FOLDER)
        uriMatcher.addURI(AUTHORITY, "$TABLE_FOLDER/#", ITEM_FOLDER_ID)
    }

    override fun onCreate(): Boolean {
        LogUtil.d(TAG, "onCreate()")
        openHelper = NoteDb.getDatabase(context!!).openHelper
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        LogUtil.d(TAG, "query() $uri, $projection, $selection, $selectionArgs, $sortOrder")
        when (uriMatcher.match(uri)) {
            ITEM_NOTE -> {
                LogUtil.d(TAG, "query note items...")
                return openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder.builder(TABLE_NOTE)
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }

            ITEM_NOTE_ID -> {
                val id = ContentUris.parseId(uri)
                LogUtil.d(TAG, "query note items id=$id")
                return openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder.builder(TABLE_NOTE)
                        .selection("id=?", arrayOf<Any>(id))
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }

            ITEM_LOCK -> {
                LogUtil.d(TAG, "query lock items...")
                return openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder.builder(TABLE_LOCK)
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }

            ITEM_FOLDER -> {
                LogUtil.d(TAG, "query folder items...")
                return openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder.builder(TABLE_FOLDER)
                        .selection(selection, selectionArgs)
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }

            ITEM_FOLDER_ID -> {
                val id = ContentUris.parseId(uri)
                LogUtil.d(TAG, "query folder items id=$id")
                return openHelper.readableDatabase.query(
                    SupportSQLiteQueryBuilder.builder(TABLE_FOLDER)
                        .selection("id=?", arrayOf<Any>(id))
                        .columns(projection)
                        .orderBy(sortOrder)
                        .create()
                )
            }

            else -> return null
        }
    }

    override fun getType(uri: Uri): String? {
        return null
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        return when (uriMatcher.match(uri)) {
            ITEM_NOTE -> {
                val rowId = openHelper.writableDatabase.insert(
                    TABLE_NOTE,
                    SQLiteDatabase.CONFLICT_ROLLBACK,
                    values!!
                )
                ContentUris.withAppendedId(uri, rowId)
            }

            ITEM_FOLDER -> {
                val rowId = openHelper.writableDatabase.insert(
                    TABLE_FOLDER,
                    SQLiteDatabase.CONFLICT_ROLLBACK,
                    values!!
                )
                ContentUris.withAppendedId(uri, rowId)
            }

            else -> null
        }
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        LogUtil.d(TAG, "delete() url = $uri")
        return when (uriMatcher.match(uri)) {
            ITEM_NOTE_ID -> {
                val id = ContentUris.parseId(uri)
                LogUtil.d(TAG, "delete() $uri, $selection, $selectionArgs")
                val rows = openHelper.writableDatabase.delete(TABLE_NOTE, "id=?", arrayOf(id))
                if (rows == 1) {
                    Config.getNoteFile(id, 1).parentFile?.deleteRecursively()
                }
                rows
            }

            ITEM_FOLDER_ID -> {
                val id = ContentUris.parseId(uri)
                LogUtil.d(TAG, "delete() $uri, $selection, $selectionArgs")
                openHelper.writableDatabase.delete(TABLE_FOLDER, "id=?", arrayOf(id))
            }

            else -> 0
        }
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        LogUtil.d(TAG, "update() url = $uri")
        return when (uriMatcher.match(uri)) {
            ITEM_NOTE_ID -> {
                val id = ContentUris.parseId(uri)
                openHelper.writableDatabase.update(
                    TABLE_NOTE, SQLiteDatabase.CONFLICT_ROLLBACK,
                    values!!, "id=?", arrayOf(id)
                )
            }

            ITEM_FOLDER_ID -> {
                val id = ContentUris.parseId(uri)
                openHelper.writableDatabase.update(
                    TABLE_FOLDER, SQLiteDatabase.CONFLICT_ROLLBACK,
                    values!!, "id=?", arrayOf(id)
                )
            }

            else -> 0
        }
    }
}