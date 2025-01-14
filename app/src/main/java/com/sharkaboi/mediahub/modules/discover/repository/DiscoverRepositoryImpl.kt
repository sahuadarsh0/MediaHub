package com.sharkaboi.mediahub.modules.discover.repository

import android.content.SharedPreferences
import com.haroldadmin.cnradapter.NetworkResponse
import com.sharkaboi.mediahub.common.extensions.emptyString
import com.sharkaboi.mediahub.data.api.constants.ApiConstants
import com.sharkaboi.mediahub.data.api.enums.getAnimeSeason
import com.sharkaboi.mediahub.data.api.models.anime.AnimeRankingResponse
import com.sharkaboi.mediahub.data.api.models.anime.AnimeSeasonalResponse
import com.sharkaboi.mediahub.data.api.models.anime.AnimeSuggestionsResponse
import com.sharkaboi.mediahub.data.api.retrofit.AnimeService
import com.sharkaboi.mediahub.data.datastore.DataStoreRepository
import com.sharkaboi.mediahub.data.sharedpref.SharedPreferencesKeys
import com.sharkaboi.mediahub.data.wrappers.MHError
import com.sharkaboi.mediahub.data.wrappers.MHTaskState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import timber.log.Timber
import java.time.LocalDate

class DiscoverRepositoryImpl(
    private val animeService: AnimeService,
    private val dataStoreRepository: DataStoreRepository,
    private val sharedPreferences: SharedPreferences
) : DiscoverRepository {

    override suspend fun getAnimeRecommendations(): MHTaskState<AnimeSuggestionsResponse> =
        withContext(Dispatchers.IO) {
            try {
                val showNsfw =
                    sharedPreferences.getBoolean(SharedPreferencesKeys.NSFW_OPTION, false)
                val accessToken: String? = dataStoreRepository.accessTokenFlow.firstOrNull()
                if (accessToken == null) {
                    return@withContext MHTaskState(
                        isSuccess = false,
                        data = null,
                        error = MHError.LoginExpiredError
                    )
                } else {
                    val result = animeService.getAnimeSuggestionsAsync(
                        authHeader = ApiConstants.BEARER_SEPARATOR + accessToken,
                        limit = ApiConstants.API_PAGE_LIMIT,
                        offset = ApiConstants.API_START_OFFSET,
                        nsfw = if (showNsfw) ApiConstants.NSFW_ALSO else ApiConstants.SFW_ONLY
                    ).await()
                    when (result) {
                        is NetworkResponse.Success -> {
                            Timber.d(result.body.toString())
                            return@withContext MHTaskState(
                                isSuccess = true,
                                data = result.body,
                                error = MHError.EmptyError
                            )
                        }
                        is NetworkResponse.NetworkError -> {
                            Timber.d(result.error.message ?: String.emptyString)
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.error.message?.let { MHError(it) }
                                    ?: MHError.NetworkError
                            )
                        }
                        is NetworkResponse.ServerError -> {
                            Timber.d(result.body.toString())
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.body?.message?.let { MHError(it) }
                                    ?: MHError.apiErrorWithCode(result.code)
                            )
                        }
                        is NetworkResponse.UnknownError -> {
                            Timber.d(result.error.message ?: String.emptyString)
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.error.message?.let { MHError(it) }
                                    ?: MHError.ParsingError
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d(e.message ?: String.emptyString)
                return@withContext MHTaskState(
                    isSuccess = false,
                    data = null,
                    error = e.message?.let { MHError(it) } ?: MHError.UnknownError
                )
            }
        }

    override suspend fun getAnimeSeasonals(): MHTaskState<AnimeSeasonalResponse> =
        withContext(Dispatchers.IO) {
            try {
                val showNsfw =
                    sharedPreferences.getBoolean(SharedPreferencesKeys.NSFW_OPTION, false)
                val accessToken: String? = dataStoreRepository.accessTokenFlow.firstOrNull()
                if (accessToken == null) {
                    return@withContext MHTaskState(
                        isSuccess = false,
                        data = null,
                        error = MHError.LoginExpiredError
                    )
                } else {
                    val today = LocalDate.now()
                    val result = animeService.getAnimeBySeasonAsync(
                        authHeader = ApiConstants.BEARER_SEPARATOR + accessToken,
                        year = today.year,
                        season = today.getAnimeSeason().name,
                        limit = ApiConstants.API_PAGE_LIMIT,
                        offset = ApiConstants.API_START_OFFSET,
                        nsfw = if (showNsfw) ApiConstants.NSFW_ALSO else ApiConstants.SFW_ONLY
                    ).await()
                    when (result) {
                        is NetworkResponse.Success -> {
                            Timber.d(result.body.toString())
                            return@withContext MHTaskState(
                                isSuccess = true,
                                data = result.body,
                                error = MHError.EmptyError
                            )
                        }
                        is NetworkResponse.NetworkError -> {
                            Timber.d(result.error.message ?: String.emptyString)
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.error.message?.let { MHError(it) }
                                    ?: MHError.NetworkError
                            )
                        }
                        is NetworkResponse.ServerError -> {
                            Timber.d(result.body.toString())
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.body?.message?.let { MHError(it) }
                                    ?: MHError.apiErrorWithCode(result.code)
                            )
                        }
                        is NetworkResponse.UnknownError -> {
                            Timber.d(result.error.message ?: String.emptyString)
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.error.message?.let { MHError(it) }
                                    ?: MHError.ParsingError
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d(e.message ?: String.emptyString)
                return@withContext MHTaskState(
                    isSuccess = false,
                    data = null,
                    error = e.message?.let { MHError(it) } ?: MHError.UnknownError
                )
            }
        }

    override suspend fun getAnimeRankings(): MHTaskState<AnimeRankingResponse> =
        withContext(Dispatchers.IO) {
            try {
                val showNsfw =
                    sharedPreferences.getBoolean(SharedPreferencesKeys.NSFW_OPTION, false)
                val accessToken: String? = dataStoreRepository.accessTokenFlow.firstOrNull()
                if (accessToken == null) {
                    return@withContext MHTaskState(
                        isSuccess = false,
                        data = null,
                        error = MHError.LoginExpiredError
                    )
                } else {
                    val result = animeService.getAnimeRankingAsync(
                        authHeader = ApiConstants.BEARER_SEPARATOR + accessToken,
                        limit = ApiConstants.API_PAGE_LIMIT,
                        offset = ApiConstants.API_START_OFFSET,
                        nsfw = if (showNsfw) ApiConstants.NSFW_ALSO else ApiConstants.SFW_ONLY
                    ).await()
                    when (result) {
                        is NetworkResponse.Success -> {
                            Timber.d(result.body.toString())
                            return@withContext MHTaskState(
                                isSuccess = true,
                                data = result.body,
                                error = MHError.EmptyError
                            )
                        }
                        is NetworkResponse.NetworkError -> {
                            Timber.d(result.error.message ?: String.emptyString)
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.error.message?.let { MHError(it) }
                                    ?: MHError.NetworkError
                            )
                        }
                        is NetworkResponse.ServerError -> {
                            Timber.d(result.body.toString())
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.body?.message?.let { MHError(it) }
                                    ?: MHError.apiErrorWithCode(result.code)
                            )
                        }
                        is NetworkResponse.UnknownError -> {
                            Timber.d(result.error.message ?: String.emptyString)
                            return@withContext MHTaskState(
                                isSuccess = false,
                                data = null,
                                error = result.error.message?.let { MHError(it) }
                                    ?: MHError.ParsingError
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Timber.d(e.message ?: String.emptyString)
                return@withContext MHTaskState(
                    isSuccess = false,
                    data = null,
                    error = e.message?.let { MHError(it) } ?: MHError.UnknownError
                )
            }
        }
}
