package com.example.util.simpletimetracker.feature_statistics.mapper

import com.example.util.simpletimetracker.core.repo.ResourceRepo
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.empty.EmptyViewData
import com.example.util.simpletimetracker.feature_base_adapter.hint.HintViewData
import com.example.util.simpletimetracker.feature_base_adapter.hintBig.HintBigViewData
import com.example.util.simpletimetracker.feature_statistics.R
import com.example.util.simpletimetracker.feature_statistics.viewData.StatisticsInfoViewData
import javax.inject.Inject

class StatisticsViewDataMapper @Inject constructor(
    private val resourceRepo: ResourceRepo,
) {

    fun mapStatisticsTotalTracked(totalTracked: String): ViewHolderType {
        return StatisticsInfoViewData(
            name = resourceRepo.getString(R.string.statistics_total_tracked),
            text = totalTracked,
        )
    }

    fun mapToEmpty(): ViewHolderType {
        return EmptyViewData(
            message = R.string.no_data.let(resourceRepo::getString),
        )
    }

    fun mapToNoStatistics(): ViewHolderType {
        return HintBigViewData(
            text = resourceRepo.getString(R.string.no_statistics_exist),
            infoIconVisible = true,
            closeIconVisible = false,
        )
    }

    fun mapToHint(): ViewHolderType {
        return HintViewData(
            text = R.string.statistics_hint.let(resourceRepo::getString),
        )
    }

    fun mapToGoalHint(): ViewHolderType {
        return HintViewData(
            text = R.string.change_record_type_goal_time_hint.let(resourceRepo::getString),
        )
    }
}