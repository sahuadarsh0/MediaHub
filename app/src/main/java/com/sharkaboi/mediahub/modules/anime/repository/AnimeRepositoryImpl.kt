package com.sharkaboi.mediahub.modules.anime.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.sharkaboi.mediahub.data.api.ApiConstants
import com.sharkaboi.mediahub.data.api.enums.AnimeStatus
import com.sharkaboi.mediahub.data.api.enums.UserAnimeSortType
import com.sharkaboi.mediahub.data.api.models.useranime.UserAnimeListResponse
import com.sharkaboi.mediahub.data.api.retrofit.UserAnimeService
import com.sharkaboi.mediahub.data.datastore.DataStoreRepository
import com.sharkaboi.mediahub.data.paging.UserAnimeListDataSource
import com.sharkaboi.mediahub.data.sharedpref.SharedPreferencesKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull

class AnimeRepositoryImpl(
    private val userAnimeService: UserAnimeService,
    private val dataStoreRepository: DataStoreRepository,
    private val sharedPreferences: SharedPreferences
) : AnimeRepository {

    override suspend fun getAnimeListFlow(
        animeStatus: AnimeStatus,
        animeSortType: UserAnimeSortType
    ): Flow<PagingData<UserAnimeListResponse.Data>> {
        val showNsfw = sharedPreferences.getBoolean(SharedPreferencesKeys.NSFW_OPTION, false)
        val accessToken: String? = dataStoreRepository.accessTokenFlow.firstOrNull()
        Log.d(TAG, "accessToken: $accessToken")
        return Pager(
            config = PagingConfig(
                pageSize = ApiConstants.API_PAGE_LIMIT,
                enablePlaceholders = false
            ),
            pagingSourceFactory = {
                UserAnimeListDataSource(
                    userAnimeService = userAnimeService,
                    accessToken = accessToken,
                    animeStatus = animeStatus,
                    animeSortType = animeSortType,
                    showNsfw = showNsfw
                )
            }
        ).flow
    }

    companion object {
        private const val TAG = "AnimeRepository"
    }
}