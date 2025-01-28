package com.example.util.simpletimetracker.domain.record.repo

import com.example.util.simpletimetracker.domain.record.model.Range
import com.example.util.simpletimetracker.domain.record.model.Record

interface RecordRepo {

    suspend fun isEmpty(): Boolean

    suspend fun getAll(): List<Record>

    suspend fun getByType(typeIds: List<Long>): List<Record>

    suspend fun getByTypeWithAnyComment(typeIds: List<Long>): List<Record>

    suspend fun searchComment(text: String): List<Record>

    suspend fun searchByTypeWithComment(typeIds: List<Long>, text: String): List<Record>

    suspend fun searchAnyComments(): List<Record>

    suspend fun get(id: Long): Record?

    suspend fun getFromRange(range: Range): List<Record>

    suspend fun getFromRangeByType(typeIds: List<Long>, range: Range): List<Record>

    suspend fun getPrev(timeStarted: Long): Record?

    suspend fun getNext(timeEnded: Long): Record?

    suspend fun getByTimeStarted(timeStarted: Long): List<Record>

    suspend fun getByTimeEnded(timeEnded: Long): List<Record>

    suspend fun getPrevTimeStarted(fromTimestamp: Long): Long?

    suspend fun getNextTimeStarted(fromTimestamp: Long): Long?

    suspend fun getPrevTimeEnded(fromTimestamp: Long): Long?

    suspend fun getNextTimeEnded(fromTimestamp: Long): Long?

    suspend fun add(record: Record): Long

    suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
    )

    suspend fun updateTimeEnded(recordId: Long, timeEnded: Long)

    suspend fun remove(id: Long)

    suspend fun removeByType(typeId: Long)

    suspend fun clear()
}