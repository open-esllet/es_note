package com.es.note.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.es.note.room.dao.FolderDao
import com.es.note.room.dao.NoteDao
import com.es.note.room.entity.Folder
import com.es.note.room.entity.Note

@Database(entities = [Note::class, Folder::class], version = 1)
abstract class NoteDb : RoomDatabase() {

    abstract fun folderDao(): FolderDao
    abstract fun noteDao(): NoteDao

    companion object {
        @Volatile
        private var instance: NoteDb? = null

        fun getDatabase(context: Context): NoteDb {
            return instance ?: synchronized(this) {
                Room.databaseBuilder(context, NoteDb::class.java, "note")
                    .build()
                    .also { instance = it }
            }
        }
    }
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE note ADD COLUMN age INTEGER NOT NULL DEFAULT 0")
    }
}