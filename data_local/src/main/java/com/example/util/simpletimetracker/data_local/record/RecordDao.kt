package com.example.util.simpletimetracker.data_local.record

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface RecordDao {

    @Transaction
    @Query("select exists(select 1 from records)")
    suspend fun isEmpty(): Long

    @Transaction
    @Query("SELECT * FROM records")
    suspend fun getAll(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds)")
    suspend fun getByType(typesIds: List<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND comment != \"\"")
    suspend fun getByTypeWithAnyComment(typesIds: List<Long>): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE instr(lower(comment), lower(:text)) > 0")
    suspend fun searchComment(text: String): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND instr(lower(comment), lower(:text)) > 0")
    suspend fun searchByTypeWithComment(typesIds: List<Long>, text: String): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE comment != \"\"")
    suspend fun searchAnyComments(): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun get(id: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started < :end AND time_ended > :start")
    suspend fun getFromRange(start: Long, end: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE type_id IN (:typesIds) AND time_started < :end AND time_ended > :start")
    suspend fun getFromRangeByType(typesIds: List<Long>, start: Long, end: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE time_ended <= :timeStarted ORDER BY time_ended DESC LIMIT 1")
    suspend fun getPrev(timeStarted: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started >= :timeEnded ORDER BY time_started ASC LIMIT 1")
    suspend fun getNext(timeEnded: Long): RecordWithRecordTagsDBO?

    @Transaction
    @Query("SELECT time_started FROM records WHERE time_started < :fromTimestamp ORDER BY time_started DESC LIMIT 1")
    suspend fun getPrevTimeStarted(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_started FROM records WHERE time_started > :fromTimestamp ORDER BY time_started ASC LIMIT 1")
    suspend fun getNextTimeStarted(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_ended FROM records WHERE time_ended < :fromTimestamp ORDER BY time_ended DESC LIMIT 1")
    suspend fun getPrevTimeEnded(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT time_ended FROM records WHERE time_ended > :fromTimestamp ORDER BY time_ended ASC LIMIT 1")
    suspend fun getNextTimeEnded(fromTimestamp: Long): Long?

    @Transaction
    @Query("SELECT * FROM records WHERE time_started = :timeStarted")
    suspend fun getByTimeStarted(timeStarted: Long): List<RecordWithRecordTagsDBO>

    @Transaction
    @Query("SELECT * FROM records WHERE time_ended = :timeEnded")
    suspend fun getByTimeEnded(timeEnded: Long): List<RecordWithRecordTagsDBO>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: RecordDBO): Long

    @Query("UPDATE records SET type_id = :typeId, comment = :comment WHERE id = :recordId")
    suspend fun update(
        recordId: Long,
        typeId: Long,
        comment: String,
    )

    @Query("UPDATE records SET time_ended = :timeEnded WHERE id = :recordId")
    suspend fun updateTimeEnded(recordId: Long, timeEnded: Long)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM records WHERE type_id = :typeId")
    suspend fun deleteByType(typeId: Long)

    @Query("DELETE FROM records")
    suspend fun clear()
}