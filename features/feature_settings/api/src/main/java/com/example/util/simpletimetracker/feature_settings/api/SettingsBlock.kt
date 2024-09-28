package com.example.util.simpletimetracker.feature_settings.api

enum class SettingsBlock {
    MainTop,
    AllowMultitasking,
    DarkMode,
    Language,
    Categories,
    Archive,
    MainBottom,

    RatingTop,
    RateUs,
    SupportDevelopment,
    Feedback,
    Version,
    DebugMenu,
    RatingBottom,

    NotificationsTop,
    NotificationsCollapse,
    NotificationsShow,
    NotificationsShowControls,
    NotificationsInactivity,
    NotificationsInactivityRecurrent,
    NotificationsInactivityDoNotDisturbStart,
    NotificationsInactivityDoNotDisturbEnd,
    NotificationsActivity,
    NotificationsActivityRecurrent,
    NotificationsActivityDoNotDisturbStart,
    NotificationsActivityDoNotDisturbEnd,
    NotificationsSystemSettings,
    NotificationsBottom,

    DisplayTop,
    DisplayCollapse,
    DisplayUntrackedHint,
    DisplayUntrackedInRecords,
    DisplayUntrackedInStatistics,
    DisplayUntrackedIgnoreShort,
    DisplayUntrackedRangeCheckbox,
    DisplayUntrackedRangeStart,
    DisplayUntrackedRangeEnd,
    DisplayCalendarView,
    DisplayCalendarButtonOnRecordsTab,
    DisplayReverseOrder,
    DisplayDaysInCalendar,
    DisplayShowActivityFilters,
    DisplayAllowMultipleActivityFilters,
    DisplayEnablePomodoroMode,
    DisplayEnableRepeatButton,
    DisplayRepeatButtonMode,
    DisplayPomodoroModeActivities,
    DisplayGoalsOnSeparateTabs,
    DisplayNavBarAtTheBottom,
    DisplayWidgetBackground,
    DisplayMilitaryFormat,
    DisplayMonthDayFormat,
    DisplayProportionalFormat,
    DisplayShowSeconds,
    DisplaySortActivities,
    DisplaySortCategories,
    DisplaySortTags,
    DisplayCardSize,
    DisplayBottom,

    AdditionalTop,
    AdditionalCollapse,
    AdditionalIgnoreShort,
    AdditionalShowTagSelection,
    AdditionalTagSelectionExcludeActivities,
    AdditionalCloseAfterOneTag,
    AdditionalKeepStatisticsRange,
    AdditionalKeepScreenOn,
    AdditionalFirstDayOfWeek,
    AdditionalShiftStartOfDay,
    AdditionalShiftStartOfDayButton,
    AdditionalShiftStartOfDayHint,
    AdditionalAutomatedTracking,
    AdditionalSendEvents,
    AdditionalDataEdit,
    AdditionalComplexRules,
    AdditionalBottom,

    BackupTop,
    BackupCollapse,
    BackupSave,
    BackupAutomatic,
    BackupAutomaticHint,
    BackupRestore,
    BackupCustomized,
    BackupBottom,

    ExportTop,
    ExportCollapse,
    ExportSpreadsheet,
    ExportSpreadsheetAutomatic,
    ExportSpreadsheetAutomaticHint,
    ExportSpreadsheetImport,
    ExportSpreadsheetImportHint,
    ExportIcs,
    ExportBottom,

    TranslatorsTop,
    TranslatorsTitle,
    TranslatorsBottom,

    ContributorsTop,
    ContributorsTitle,
    ContributorsBottom,

    PomodoroFocusTime,
    PomodoroBreakTime,
    PomodoroLongBreakTime,
    PomodoroPeriodsUntilLongBreak,
}