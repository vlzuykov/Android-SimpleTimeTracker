package com.example.util.simpletimetracker.data_local.recordType

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recordTypes")
data class RecordTypeDBO(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "icon")
    val icon: String,

    @ColumnInfo(name = "color")
    val color: Int,

    // If not empty - take color from here, custom colorInt stored as text.
    @ColumnInfo(name = "color_int")
    val colorInt: String,

    @ColumnInfo(name = "hidden")
    val hidden: Boolean,

    @Deprecated("Remove on next table altering")
    @ColumnInfo(name = "instant")
    val instant: Boolean = false,

    // Seconds.
    @ColumnInfo(name = "instantDuration")
    val defaultDuration: Long,

    @ColumnInfo(name = "note")
    val note: String,
)