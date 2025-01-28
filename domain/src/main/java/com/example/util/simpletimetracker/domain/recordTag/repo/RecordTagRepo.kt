package com.example.util.simpletimetracker.domain.recordTag.repo

import com.example.util.simpletimetracker.domain.recordTag.model.RecordTag

interface RecordTagRepo {

    suspend fun isEmpty(): Boolean

    suspend fun getAll(): List<RecordTag>

    suspend fun get(id: Long): RecordTag?

    suspend fun get(name: String): List<RecordTag>

    suspend fun getByType(typeId: Long): List<RecordTag>

    suspend fun add(tag: RecordTag): Long

    suspend fun archive(id: Long)

    suspend fun restore(id: Long)

    suspend fun remove(id: Long)

    suspend fun clear()
}