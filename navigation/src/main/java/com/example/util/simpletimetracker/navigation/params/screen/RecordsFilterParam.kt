package com.example.util.simpletimetracker.navigation.params.screen

import android.os.Parcelable
import com.example.util.simpletimetracker.domain.daysOfWeek.model.DayOfWeek
import kotlinx.parcelize.Parcelize

sealed interface RecordsFilterParam : Parcelable {

    @Parcelize
    object Untracked : RecordsFilterParam

    @Parcelize
    object Multitask : RecordsFilterParam

    @Parcelize
    data class Activity(val typeIds: List<Long>) : RecordsFilterParam

    @Parcelize
    data class Category(val items: List<CategoryItem>) : RecordsFilterParam

    @Parcelize
    data class Comment(val items: List<CommentItem>) : RecordsFilterParam

    @Parcelize
    data class Date(val range: RangeLengthParams, val position: Int) : RecordsFilterParam

    @Parcelize
    data class SelectedTags(val items: List<TagItem>) : RecordsFilterParam

    @Parcelize
    data class FilteredTags(val items: List<TagItem>) : RecordsFilterParam

    @Parcelize
    data class ManuallyFiltered(val recordIds: List<Long>) : RecordsFilterParam

    @Parcelize
    data class DaysOfWeek(val items: List<DayOfWeek>) : RecordsFilterParam

    @Parcelize
    data class TimeOfDay(val range: RangeParams) : RecordsFilterParam

    @Parcelize
    data class Duration(val range: RangeParams) : RecordsFilterParam

    @Parcelize
    data class Duplications(val items: List<DuplicationsItem>) : RecordsFilterParam

    sealed interface CommentItem : Parcelable {
        @Parcelize
        object NoComment : CommentItem

        @Parcelize
        object AnyComment : CommentItem

        @Parcelize
        data class Comment(val text: String) : CommentItem
    }

    sealed interface CategoryItem : Parcelable {
        @Parcelize
        data class Categorized(val categoryId: Long) : CategoryItem

        @Parcelize
        object Uncategorized : CategoryItem
    }

    sealed interface TagItem : Parcelable {
        @Parcelize
        data class Tagged(val tagId: Long) : TagItem

        @Parcelize
        object Untagged : TagItem
    }

    sealed interface DuplicationsItem : Parcelable {
        @Parcelize
        object SameActivity : DuplicationsItem

        @Parcelize
        object SameTimes : DuplicationsItem
    }
}