package com.azyoot.relearn.data

import com.azyoot.relearn.data.mapper.TranslationEventMapper
import com.azyoot.relearn.domain.entity.TranslationEvent
import javax.inject.Inject

class TranslationEventRepository @Inject constructor(
    private val database: AppDatabase,
    private val mapper: TranslationEventMapper
) {

    suspend fun saveTranslationEvent(translationEvent: TranslationEvent) {
        database.translationEventDao().addTranslationEvent(mapper.toDataEntity(translationEvent))
    }

    suspend fun getLastTranslationEventForResultText(toText: String) =
        database.translationEventDao().getLastTranslationEventByResultText(toText)
            ?.let { mapper.toDomainEntity(it) }
}