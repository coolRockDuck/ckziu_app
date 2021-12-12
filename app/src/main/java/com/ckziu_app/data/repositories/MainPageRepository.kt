package com.ckziu_app.data.repositories

import com.ckziu_app.data.network.MainPageInfoGetter
import com.ckziu_app.model.*
import kotlinx.coroutines.flow.flow

/** Repository managing informations regarding main page
 * @see com.ckziu_app.model.MainPageInfo*/
class MainPageRepository(private val mainPageInfoGetter: MainPageInfoGetter) {

    /** Returns flow of informations about main page. */
    suspend fun flowOfMainPageInfo() =
        flow<Result<MainPageInfo>> {
            emit(InProgress())
            when (val result = mainPageInfoGetter.getMainPageInfo()) {
                null -> {
                    emit(Failure("MainPageInfoGetter failed to load mainPageInfo"))
                }

                else -> {
                    emit(Success(result))
                }
            }
        }
    }

