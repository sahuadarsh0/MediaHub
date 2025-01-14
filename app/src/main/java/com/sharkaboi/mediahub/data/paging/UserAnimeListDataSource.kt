package com.sharkaboi.mediahub.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.haroldadmin.cnradapter.NetworkResponse
import com.sharkaboi.mediahub.common.extensions.emptyString
import com.sharkaboi.mediahub.data.api.constants.ApiConstants
import com.sharkaboi.mediahub.data.api.enums.AnimeStatus
import com.sharkaboi.mediahub.data.api.enums.UserAnimeSortType
import com.sharkaboi.mediahub.data.api.models.ApiError
import com.sharkaboi.mediahub.data.api.models.useranime.UserAnimeListResponse
import com.sharkaboi.mediahub.data.api.retrofit.UserAnimeService
import com.sharkaboi.mediahub.data.wrappers.MHError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

class UserAnimeListDataSource(
    private val userAnimeService: UserAnimeService,
    private val accessToken: String?,
    private val animeStatus: AnimeStatus,
    private val animeSortType: UserAnimeSortType = UserAnimeSortType.list_updated_at,
    private val showNsfw: Boolean = false
) : PagingSource<Int, UserAnimeListResponse.Data>() {

    /**
     *   prevKey == null -> first page
     *   nextKey == null -> last page
     *   both prevKey and nextKey null -> only one page
     */
    override fun getRefreshKey(state: PagingState<Int, UserAnimeListResponse.Data>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(ApiConstants.API_PAGE_LIMIT) ?: anchorPage?.nextKey?.minus(
                ApiConstants.API_PAGE_LIMIT
            )
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, UserAnimeListResponse.Data> {
        Timber.d("params : ${params.key}")
        try {
            val offset = params.key ?: ApiConstants.API_START_OFFSET
            val limit = ApiConstants.API_PAGE_LIMIT
            if (accessToken == null) {
                return LoadResult.Error(
                    MHError.LoginExpiredError.getThrowable()
                )
            } else {
                val response = withContext(Dispatchers.IO) {
                    userAnimeService.getAnimeListOfUserAsync(
                        authHeader = ApiConstants.BEARER_SEPARATOR + accessToken,
                        status = getStatus(),
                        offset = offset,
                        limit = limit,
                        sort = animeSortType.name,
                        nsfw = if (showNsfw) ApiConstants.NSFW_ALSO else ApiConstants.SFW_ONLY
                    ).await()
                }
                when (response) {
                    is NetworkResponse.Success -> {
                        val nextOffset = if (response.body.data.isEmpty()) null else offset + limit
                        return LoadResult.Page(
                            data = response.body.data,
                            prevKey = if (offset == ApiConstants.API_START_OFFSET) null else offset - limit,
                            nextKey = nextOffset
                        )
                    }
                    is NetworkResponse.UnknownError -> {
                        return LoadResult.Error(
                            response.error
                        )
                    }
                    is NetworkResponse.ServerError -> {
                        return LoadResult.Error(
                            response.body?.getThrowable() ?: ApiError.DefaultError.getThrowable()
                        )
                    }
                    is NetworkResponse.NetworkError -> {
                        return LoadResult.Error(
                            response.error
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Timber.d(e.message ?: String.emptyString)
            return LoadResult.Error(e)
        }
    }

    private fun getStatus(): String? = if (animeStatus == AnimeStatus.all) {
        null
    } else {
        animeStatus.name
    }
}
