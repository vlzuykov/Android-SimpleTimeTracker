package com.example.util.simpletimetracker.feature_widget.universal

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.view.ContextThemeWrapper
import android.view.LayoutInflater
import android.view.View
import android.widget.RemoteViews
import com.example.util.simpletimetracker.core.extension.allowDiskRead
import com.example.util.simpletimetracker.core.extension.allowVmViolations
import com.example.util.simpletimetracker.core.utils.PendingIntents
import com.example.util.simpletimetracker.domain.prefs.interactor.PrefsInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RecordInteractor
import com.example.util.simpletimetracker.domain.recordType.interactor.RecordTypeInteractor
import com.example.util.simpletimetracker.domain.record.interactor.RunningRecordInteractor
import com.example.util.simpletimetracker.domain.record.model.RunningRecord
import com.example.util.simpletimetracker.feature_views.extension.getBitmapFromView
import com.example.util.simpletimetracker.feature_views.extension.measureExactly
import com.example.util.simpletimetracker.feature_widget.R
import com.example.util.simpletimetracker.feature_widget.universal.activity.view.WidgetUniversalActivity
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalView
import com.example.util.simpletimetracker.feature_widget.universal.customView.WidgetUniversalViewData
import com.example.util.simpletimetracker.feature_widget.universal.mapper.WidgetUniversalViewDataMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WidgetUniversalProvider : AppWidgetProvider() {

    @Inject
    lateinit var runningRecordInteractor: RunningRecordInteractor

    @Inject
    lateinit var recordInteractor: RecordInteractor

    @Inject
    lateinit var recordTypeInteractor: RecordTypeInteractor

    @Inject
    lateinit var widgetUniversalViewDataMapper: WidgetUniversalViewDataMapper

    @Inject
    lateinit var prefsInteractor: PrefsInteractor

    override fun onUpdate(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetIds: IntArray?,
    ) {
        appWidgetIds?.forEach { widgetId ->
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    private fun updateAppWidget(
        context: Context?,
        appWidgetManager: AppWidgetManager?,
        appWidgetId: Int,
    ) {
        if (context == null || appWidgetManager == null) return

        allowDiskRead { MainScope() }.launch {
            val runningRecords: List<RunningRecord> = runningRecordInteractor.getAll()
            val recordTypes = recordTypeInteractor.getAll().associateBy { it.id }
            val isDarkTheme = prefsInteractor.getDarkMode()
            val backgroundTransparency = prefsInteractor.getWidgetBackgroundTransparencyPercent()
            val retroactiveTrackingModeEnabled = prefsInteractor.getRetroactiveTrackingMode()
            val prevRecord = if (retroactiveTrackingModeEnabled) {
                // TODO several previous?
                recordInteractor.getAllPrev(timeStarted = System.currentTimeMillis())
                    .maxByOrNull { it.timeStarted }
            } else {
                null
            }

            val data = when {
                runningRecords.isNotEmpty() -> {
                    widgetUniversalViewDataMapper.mapToWidgetViewData(
                        runningRecords = runningRecords,
                        recordTypes = recordTypes,
                        isDarkTheme = isDarkTheme,
                        backgroundTransparency = backgroundTransparency,
                    )
                }
                prevRecord != null -> {
                    widgetUniversalViewDataMapper.mapToRetroactiveWidgetViewData(
                        prevRecord = prevRecord,
                        recordTypes = recordTypes,
                        isDarkTheme = isDarkTheme,
                        backgroundTransparency = backgroundTransparency,
                    )
                }
                else -> {
                    widgetUniversalViewDataMapper.mapToEmptyWidgetViewData(
                        backgroundTransparency = backgroundTransparency,
                    )
                }
            }

            val view = prepareView(context, data)
            measureView(context, view)
            val bitmap = view.getBitmapFromView()

            val views = RemoteViews(context.packageName, R.layout.widget_layout)
            when {
                data.data.size > 2 -> {
                    views.setViewVisibility(R.id.timerWidget, View.GONE)
                    views.setViewVisibility(R.id.timerWidget2, View.GONE)
                }
                runningRecords.isNotEmpty() -> {
                    setChronometer(
                        timestamp = runningRecords.getOrNull(0)?.timeStarted
                            ?.let { System.currentTimeMillis() - it },
                        chronometerId = R.id.timerWidget,
                        views = views,
                        started = true,
                    )
                    setChronometer(
                        timestamp = runningRecords.getOrNull(1)?.timeStarted
                            ?.let { System.currentTimeMillis() - it },
                        chronometerId = R.id.timerWidget2,
                        views = views,
                        started = true,
                    )
                }
                prevRecord != null -> {
                    setChronometer(
                        timestamp = System.currentTimeMillis() - prevRecord.timeEnded,
                        chronometerId = R.id.timerWidget,
                        views = views,
                        started = true,
                    )
                    setChronometer(
                        timestamp = prevRecord.timeEnded - prevRecord.timeStarted,
                        chronometerId = R.id.timerWidget2,
                        views = views,
                        started = false,
                    )
                }
                else -> {
                    views.setViewVisibility(R.id.timerWidget, View.GONE)
                    views.setViewVisibility(R.id.timerWidget2, View.GONE)
                }
            }
            views.setImageViewBitmap(R.id.ivWidgetBackground, bitmap)
            views.setOnClickPendingIntent(R.id.btnWidget, getPendingIntent(context))

            runCatching {
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun prepareView(
        context: Context,
        data: WidgetUniversalViewData,
    ): View {
        return allowVmViolations {
            WidgetUniversalView(ContextThemeWrapper(context, R.style.AppTheme))
        }.apply {
            setData(data)
        }
    }

    private fun setChronometer(
        timestamp: Long?,
        chronometerId: Int,
        views: RemoteViews,
        started: Boolean,
    ) {
        if (timestamp != null) {
            val base = SystemClock.elapsedRealtime() - timestamp
            views.setChronometer(chronometerId, base, null, started)
            views.setViewVisibility(chronometerId, View.VISIBLE)
        } else {
            views.setViewVisibility(chronometerId, View.GONE)
        }
    }

    private fun measureView(context: Context, view: View) {
        var width = context.resources.getDimensionPixelSize(R.dimen.widget_width)
        var height = context.resources.getDimensionPixelSize(R.dimen.widget_height)
        val inflater = LayoutInflater.from(context)

        val entireView: View = allowVmViolations { inflater.inflate(R.layout.widget_layout, null) }
        entireView.measureExactly(width = width, height = height)

        val imageView = entireView.findViewById<View>(R.id.ivWidgetBackground)
        width = imageView.measuredWidth
        height = imageView.measuredHeight
        view.measureExactly(width = width, height = height)
    }

    private fun getPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, WidgetUniversalActivity::class.java)
        return PendingIntent.getActivity(context, 0, intent, PendingIntents.getFlags())
    }
}