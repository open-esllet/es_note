package com.es.note.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("note")
data class Note(
    val folderId: Long,
    val name: String,
) {
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null
    var userId: Int = 0
    var saving: Int = 0
    var pageNo: Int = 0
    var imgPaths: String? = null
    var backgroundPaths: String? = null
    var createdTime: Long
    var updatedTime: Long
    var accessTime: Long

    init {
        val current = System.currentTimeMillis()
        createdTime = current
        updatedTime = current
        accessTime = current
    }

    override fun toString(): String {
        return "Note(id=$id, folderId=$folderId, saving=$saving, pageNo=$pageNo, imgPaths=$imgPaths, backgroundPaths=$backgroundPaths)"
    }
}