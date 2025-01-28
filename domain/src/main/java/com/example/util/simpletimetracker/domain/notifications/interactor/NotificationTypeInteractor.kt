package com.example.util.simpletimetracker.domain.notifications.interactor

interface NotificationTypeInteractor {

    suspend fun checkAndShow(
        typeId: Long,
        typesShift: Int = 0,
        tagsShift: Int = 0,
        selectedTypeId: Long = 0,
    )

    suspend fun checkAndHide(typeId: Long)

    suspend fun updateNotifications()
}