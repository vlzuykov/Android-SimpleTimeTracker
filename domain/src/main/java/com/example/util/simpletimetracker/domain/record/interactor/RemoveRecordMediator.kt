package com.example.util.simpletimetracker.domain.record.interactor

import com.example.util.simpletimetracker.domain.notifications.interactor.UpdateExternalViewsInteractor
import javax.inject.Inject

class RemoveRecordMediator @Inject constructor(
    private val recordInteractor: RecordInteractor,
    private val externalViewsInteractor: UpdateExternalViewsInteractor,
) {

    suspend fun remove(recordId: Long, typeId: Long) {
        recordInteractor.remove(recordId)
        doAfterRemove(typeId)
    }

    suspend fun doAfterRemove(typeId: Long) {
        externalViewsInteractor.onRecordRemove(typeId)
    }
}