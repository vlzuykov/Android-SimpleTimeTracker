package com.example.util.simpletimetracker.domain.mapper

import com.example.util.simpletimetracker.domain.interactor.CalculateAdjacentActivitiesInteractor
import com.example.util.simpletimetracker.domain.interactor.CalculateAdjacentActivitiesInteractor.CalculationResult
import com.example.util.simpletimetracker.domain.model.Record
import com.example.util.simpletimetracker.domain.model.RecordBase
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class CalculateAdjacentActivitiesInteractorTest(
    private val input: Pair<Long, List<RecordBase>>,
    private val output: List<CalculationResult>,
) {

    private val subject = CalculateAdjacentActivitiesInteractor()

    @Suppress("UNCHECKED_CAST")
    @Test
    fun map() {
        val expected = output
        val actual = subject.calculateNextActivities(
            typeId = input.first,
            records = input.second,
        )

        assertEquals(
            "Test failed for params $input",
            expected,
            actual,
        )
    }

    companion object {
        private val record: Record = Record(
            id = 0L,
            typeId = 0L,
            timeStarted = 0L,
            timeEnded = 0L,
            comment = "",
            tagIds = emptyList(),
        )

        @JvmStatic
        @Parameterized.Parameters
        fun data() = listOf(
            // Empty.
            arrayOf(
                0L to emptyList<RecordBase>(),
                emptyList<CalculationResult>(),
            ),
            arrayOf(
                0L to listOf(record),
                emptyList<CalculationResult>(),
            ),
            arrayOf(
                0L to listOf(
                    record.copy(typeId = 1),
                    record.copy(typeId = 2),
                ),
                emptyList<CalculationResult>(),
            ),
            // Multitasked.
            arrayOf(
                0L to listOf(
                    record.copy(typeId = 0, timeStarted = 1, timeEnded = 10),
                    record.copy(typeId = 1, timeStarted = 2, timeEnded = 3),
                    record.copy(typeId = 2, timeStarted = 4, timeEnded = 15),
                ),
                emptyList<CalculationResult>(),
            ),
            // Only before.
            arrayOf(
                0L to listOf(
                    record.copy(typeId = 1, timeStarted = 1, timeEnded = 2),
                    record.copy(typeId = 2, timeStarted = 2, timeEnded = 3),
                    record.copy(typeId = 0, timeStarted = 3, timeEnded = 4),
                ),
                emptyList<CalculationResult>(),
            ),
            // One after.
            arrayOf(
                0L to listOf(
                    record.copy(typeId = 0, timeStarted = 0, timeEnded = 1),
                    record.copy(typeId = 1, timeStarted = 2, timeEnded = 3),
                ),
                listOf(
                    CalculationResult(1, 1),
                ),
            ),
            // Several.
            arrayOf(
                0L to listOf(
                    record.copy(typeId = 1, timeStarted = 0, timeEnded = 1),
                    record.copy(typeId = 0, timeStarted = 1, timeEnded = 2),
                    record.copy(typeId = 1, timeStarted = 2, timeEnded = 3),
                    record.copy(typeId = 2, timeStarted = 3, timeEnded = 4),
                    record.copy(typeId = 0, timeStarted = 4, timeEnded = 5),
                    record.copy(typeId = 2, timeStarted = 5, timeEnded = 6),
                    record.copy(typeId = 0, timeStarted = 6, timeEnded = 7),
                    record.copy(typeId = 1, timeStarted = 10, timeEnded = 11),
                ),
                listOf(
                    CalculationResult(1, 2),
                    CalculationResult(2, 1),
                ),
            ),
        )
    }
}