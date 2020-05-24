package com.azyoot.relearn.domain.usecase.relearn

import com.azyoot.relearn.data.repository.RelearnEventRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GetOrderingNumberForNextReLearnSourceUseCase @Inject constructor(
    private val relearnEventRepository: RelearnEventRepository,
    private val getRandomOrderingNumberFromSourceRangeUseCase: GetRandomOrderingNumberFromSourceRangeUseCase
) {

    suspend fun getOrderingNumberForNextReLearnSource(): Int? {
        val range = withContext(Dispatchers.IO) {
            relearnEventRepository.getSourceRange()
        }
        range ?: return null

        return getRandomOrderingNumberFromSourceRangeUseCase.getRandomOrderingNumberFromSourceRangeUseCase(
            range
        )
    }
}