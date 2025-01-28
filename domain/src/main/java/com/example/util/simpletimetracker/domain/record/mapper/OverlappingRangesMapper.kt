package com.example.util.simpletimetracker.domain.record.mapper

import com.example.util.simpletimetracker.domain.record.model.Range
import javax.inject.Inject

class OverlappingRangesMapper @Inject constructor() {

    interface Id

    fun map(segments: List<Pair<Id, Range>>): List<Pair<List<Id>, Range>> {
        if (segments.isEmpty()) return emptyList()

        val n = segments.size

        // Create a list to store starting and ending points
        // Segment start marked with false.
        // range id, range value, is closing.
        val points: MutableList<Triple<Id, Long, Boolean>> = mutableListOf()
        var secondIsHigher: Boolean
        for (i in (0 until n)) {
            // Reverse segments if needed
            secondIsHigher = segments[i].second.timeEnded > segments[i].second.timeStarted
            points.add(Triple(segments[i].first, segments[i].second.timeStarted, !secondIsHigher))
            points.add(Triple(segments[i].first, segments[i].second.timeEnded, secondIsHigher))
        }

        // Sorting all points by point value
        points.sortWith(compareBy({ it.second }, { it.third }))

        // Initialize result
        val result = mutableListOf<Pair<List<Id>, Range>>()

        // To keep track of counts of current open segments
        // (Starting point is processed, but ending point is not)
        val counter = mutableListOf<Id>()

        // Traverse through all points
        for (i in (0 until points.size)) {
            // If there are more than one open point and is closing, then we add the
            // difference between previous and current point.
            if (counter.size >= 2) {
                Range(points[i - 1].second, points[i].second)
                    .takeUnless { it.timeStarted == it.timeEnded }
                    ?.let { result.add(counter.toList() to it) }
            }

            // If this is an ending point, reduce count of open points.
            if (points[i].third) counter.remove(points[i].first) else counter.add(points[i].first)
        }

        return result
    }
}