package com.example.util.simpletimetracker.core.mapper

import com.example.util.simpletimetracker.core.R
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.currentTimestampProvider
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.hourInMs
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.localeProvider
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.minuteInMs
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.resourceRepo
import com.example.util.simpletimetracker.core.mapper.TimeMapperTest.Subject.secondInMs
import com.example.util.simpletimetracker.domain.base.CurrentTimestampProvider
import com.example.util.simpletimetracker.core.provider.LocaleProvider
import com.example.util.simpletimetracker.core.repo.ResourceRepo
import org.junit.AfterClass
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.BeforeClass
import org.junit.Test
import org.junit.experimental.runners.Enclosed
import org.junit.runner.RunWith
import org.junit.runners.Parameterized
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import java.util.Locale
import java.util.concurrent.TimeUnit

@RunWith(Enclosed::class)
class TimeMapperTest {

    private object Subject {
        val resourceRepo: ResourceRepo = Mockito.mock(ResourceRepo::class.java)
        val currentTimestampProvider: CurrentTimestampProvider = Mockito.mock(CurrentTimestampProvider::class.java)
        val localeProvider: LocaleProvider = Mockito.mock(LocaleProvider::class.java)

        val secondInMs = TimeUnit.SECONDS.toMillis(1)
        val minuteInMs = TimeUnit.MINUTES.toMillis(1)
        val hourInMs = TimeUnit.HOURS.toMillis(1)
    }

    @RunWith(Parameterized::class)
    class FormatIntervalTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            `when`(resourceRepo.getString(R.string.time_hour)).thenReturn("h")
            `when`(resourceRepo.getString(R.string.time_minute)).thenReturn("m")
            `when`(resourceRepo.getString(R.string.time_second)).thenReturn("s")
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(input, useProportionalMinutes = false, forceSeconds = false),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0m"),
                arrayOf(100, "0m"),
                arrayOf(secondInMs, "0m"),
                arrayOf(14 * secondInMs, "0m"),

                arrayOf(minuteInMs, "1m"),
                arrayOf(minuteInMs + 100, "1m"),
                arrayOf(minuteInMs + secondInMs, "1m"),
                arrayOf(2 * minuteInMs, "2m"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "12m"),
                arrayOf(59 * minuteInMs, "59m"),

                arrayOf(hourInMs, "1h 0m"),
                arrayOf(hourInMs + minuteInMs, "1h 1m"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "1h 1m"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "12h 34m"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class FormatIntervalProportionalTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            `when`(resourceRepo.getString(R.string.time_hour)).thenReturn("h")
            `when`(resourceRepo.getString(R.string.time_minute)).thenReturn("m")
            `when`(resourceRepo.getString(R.string.time_second)).thenReturn("s")
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(input, useProportionalMinutes = true, forceSeconds = false),
            )
        }

        companion object {
            private val localeDefault: Locale = Locale.getDefault()

            @JvmStatic
            @BeforeClass
            fun beforeClass() {
                Locale.setDefault(Locale.US)
            }

            @JvmStatic
            @AfterClass
            fun afterClass() {
                Locale.setDefault(localeDefault)
            }

            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0.00h"),
                arrayOf(100, "0.00h"),
                arrayOf(secondInMs, "0.00h"),
                arrayOf(14 * secondInMs, "0.00h"),

                arrayOf(minuteInMs, "0.02h"),
                arrayOf(minuteInMs + 100, "0.02h"),
                arrayOf(minuteInMs + secondInMs, "0.02h"),
                arrayOf(2 * minuteInMs, "0.03h"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "0.20h"),
                arrayOf(59 * minuteInMs, "0.98h"),

                arrayOf(hourInMs, "1.00h"),
                arrayOf(hourInMs + minuteInMs, "1.02h"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "1.02h"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "12.57h"),
            )
        }
    }

    @RunWith(Parameterized::class)
    class FormatIntervalWithForcedSecondsTest(
        private val input: Long,
        private val output: String,
    ) {

        @Before
        fun before() {
            `when`(resourceRepo.getString(R.string.time_hour)).thenReturn("h")
            `when`(resourceRepo.getString(R.string.time_minute)).thenReturn("m")
            `when`(resourceRepo.getString(R.string.time_second)).thenReturn("s")
        }

        @Test
        fun formatInterval() {
            `when`(localeProvider.get()).thenReturn(Locale.getDefault())

            val subject = TimeMapper(
                localeProvider = localeProvider,
                resourceRepo = resourceRepo,
                currentTimestampProvider = currentTimestampProvider,
            )

            assertEquals(
                "Test failed for params $input",
                output,
                subject.formatInterval(input, useProportionalMinutes = false, forceSeconds = true),
            )
        }

        companion object {
            @JvmStatic
            @Parameterized.Parameters
            fun data() = listOf(
                arrayOf(0, "0s"),
                arrayOf(100, "0s"),
                arrayOf(secondInMs, "1s"),
                arrayOf(14 * secondInMs, "14s"),

                arrayOf(minuteInMs, "1m 0s"),
                arrayOf(minuteInMs + 100, "1m 0s"),
                arrayOf(minuteInMs + secondInMs, "1m 1s"),
                arrayOf(2 * minuteInMs, "2m 0s"),
                arrayOf(12 * minuteInMs + 34 * secondInMs, "12m 34s"),
                arrayOf(59 * minuteInMs, "59m 0s"),

                arrayOf(hourInMs, "1h 0m 0s"),
                arrayOf(hourInMs + minuteInMs, "1h 1m 0s"),
                arrayOf(hourInMs + minuteInMs + secondInMs, "1h 1m 1s"),
                arrayOf(12 * hourInMs + 34 * minuteInMs + 56 * secondInMs, "12h 34m 56s"),
            )
        }
    }
}