package com.example.util.simpletimetracker.feature_statistics.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.util.simpletimetracker.core.base.SingleLiveEvent
import com.example.util.simpletimetracker.core.extension.set
import com.example.util.simpletimetracker.core.interactor.SharingInteractor
import com.example.util.simpletimetracker.core.interactor.StatisticsDetailNavigationInteractor
import com.example.util.simpletimetracker.core.model.NavigationTab
import com.example.util.simpletimetracker.domain.extension.orZero
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.feature_base_adapter.ViewHolderType
import com.example.util.simpletimetracker.feature_base_adapter.loader.LoaderViewData
import com.example.util.simpletimetracker.feature_base_adapter.statistics.StatisticsViewData
import com.example.util.simpletimetracker.feature_base_adapter.statisticsGoal.StatisticsGoalViewData
import com.example.util.simpletimetracker.feature_statistics.extra.StatisticsExtra
import com.example.util.simpletimetracker.feature_statistics.interactor.StatisticsDetailTotalNavigator
import com.example.util.simpletimetracker.feature_statistics.interactor.StatisticsViewDataInteractor
import com.example.util.simpletimetracker.navigation.Router
import com.example.util.simpletimetracker.navigation.params.screen.ChartFilterDialogParams
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val router: Router,
    private val statisticsViewDataInteractor: StatisticsViewDataInteractor,
    private val sharingInteractor: SharingInteractor,
    private val prefsInteractor: PrefsInteractor,
    private val statisticsDetailNavigationInteractor: StatisticsDetailNavigationInteractor,
    private val statisticsDetailTotalNavigator: StatisticsDetailTotalNavigator,
) : ViewModel() {

    var extra: StatisticsExtra? = null

    val statistics: LiveData<List<ViewHolderType>> by lazy {
        MutableLiveData(listOf(LoaderViewData() as ViewHolderType))
    }
    val sharingData: SingleLiveEvent<List<ViewHolderType>> = SingleLiveEvent()
    val resetScreen: SingleLiveEvent<Unit> = SingleLiveEvent()
    val animateChartParticles: LiveData<Boolean> = MutableLiveData()

    private var isVisible: Boolean = false
    private var isTabScrolling: Boolean = false
    private var isChartAttached: Boolean = false
    private var isChartFilterOpened: Boolean = false
    private var timerJob: Job? = null
    private val shift: Int get() = extra?.shift.orZero()

    fun onVisible() {
        isVisible = true
        if (shift == 0) {
            startUpdate()
        } else {
            updateStatistics()
        }
        updateAnimateChartParticles()
    }

    fun onHidden() {
        isVisible = false
        stopUpdate()
        updateAnimateChartParticles()
    }

    fun onChartAttached(isAttached: Boolean) {
        isChartAttached = isAttached
        updateAnimateChartParticles()
    }

    fun isScrolling(isScrolling: Boolean) {
        isTabScrolling = isScrolling
        updateAnimateChartParticles()
    }

    fun onRangeUpdated() {
        if (isVisible) updateStatistics()
    }

    fun onFilterClick() {
        router.navigate(ChartFilterDialogParams(ChartFilterDialogParams.Type.Statistics))
        isChartFilterOpened = true
        updateAnimateChartParticles()
    }

    fun onShareClick() = viewModelScope.launch {
        val data = loadStatisticsViewData(forSharing = true)
        sharingData.set(data)
    }

    fun onItemClick(
        item: StatisticsViewData,
        sharedElements: Map<Any, String>,
    ) = viewModelScope.launch {
        statisticsDetailNavigationInteractor.navigate(
            transitionName = item.transitionName,
            filterType = prefsInteractor.getChartFilterType(),
            shift = getShift(),
            sharedElements = sharedElements,
            itemId = item.id,
            itemName = item.name,
            itemIcon = item.icon,
            itemColor = item.color,
        )
    }

    fun onGoalClick(item: StatisticsGoalViewData) = viewModelScope.launch {
        statisticsDetailNavigationInteractor.navigateByGoal(
            goalId = item.id,
            shift = getShift(),
        )
    }

    fun onTotalClicked() = viewModelScope.launch {
        statisticsDetailTotalNavigator.execute(
            shift = getShift(),
        )
    }

    fun onFilterApplied() {
        updateStatistics()
        isChartFilterOpened = false
        updateAnimateChartParticles()
    }

    fun onShareView(view: Any) = viewModelScope.launch {
        sharingInteractor.execute(view = view, filename = SHARING_NAME)
    }

    fun onTabReselected(tab: NavigationTab?) {
        if (isVisible && tab is NavigationTab.Statistics) {
            resetScreen.set(Unit)
        }
    }

    private suspend fun getShift(): Int {
        return if (prefsInteractor.getKeepStatisticsRange()) shift else 0
    }

    private fun updateAnimateChartParticles() {
        val shouldAnimate = isVisible && isChartAttached && !isTabScrolling && !isChartFilterOpened
        animateChartParticles.set(shouldAnimate)
    }

    private fun updateStatistics() = viewModelScope.launch {
        val data = loadStatisticsViewData()
        statistics.set(data)
    }

    private suspend fun loadStatisticsViewData(forSharing: Boolean = false): List<ViewHolderType> {
        return statisticsViewDataInteractor.getViewData(
            rangeLength = prefsInteractor.getStatisticsRange(),
            shift = shift,
            forSharing = forSharing,
        )
    }

    private fun startUpdate() {
        timerJob = viewModelScope.launch {
            timerJob?.cancelAndJoin()
            while (isActive) {
                updateStatistics()
                delay(TIMER_UPDATE)
            }
        }
    }

    private fun stopUpdate() {
        viewModelScope.launch {
            timerJob?.cancelAndJoin()
        }
    }

    companion object {
        private const val TIMER_UPDATE = 1000L
        private const val SHARING_NAME = "stt_statistics"
    }
}
