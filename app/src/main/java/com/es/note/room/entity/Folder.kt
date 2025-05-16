package com.es.note.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity("folder")
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val parentId: Int,
    val name: String,
) {
    var userId: Int = 0
    var createdTime: Long
    var updatedTime: Long
    var accessTime: Long

    init {
        val current = System.currentTimeMillis()
        createdTime = current
        updatedTime = current
        accessTime = current
    }
}