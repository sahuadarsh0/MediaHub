package com.sharkaboi.mediahub.modules.manga_details.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sharkaboi.mediahub.data.api.enums.MangaStatus
import com.sharkaboi.mediahub.data.api.enums.mangaStatusFromString
import com.sharkaboi.mediahub.data.wrappers.MHError
import com.sharkaboi.mediahub.modules.manga_details.repository.MangaDetailsRepository
import com.sharkaboi.mediahub.modules.manga_details.util.MangaDetailsUpdateClass
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MangaDetailsViewModel
@Inject constructor(
    private val mangaDetailsRepository: MangaDetailsRepository
) : ViewModel() {

    private val _uiState = MutableLiveData<MangaDetailsState>().getDefault()
    val uiState: LiveData<MangaDetailsState> = _uiState
    private var _mangaDetailsUpdate: MutableLiveData<MangaDetailsUpdateClass> =
        MutableLiveData<MangaDetailsUpdateClass>()
    val mangaDetailsUpdate: LiveData<MangaDetailsUpdateClass> = _mangaDetailsUpdate

    fun getMangaDetails(mangaId: Int) {
        viewModelScope.launch {
            _uiState.setLoading()
            val result = mangaDetailsRepository.getMangaById(mangaId)
            if (result.isSuccess) {
                result.data?.let {
                    _mangaDetailsUpdate.value = MangaDetailsUpdateClass(
                        mangaStatus = it.myListStatus?.status?.mangaStatusFromString(),
                        mangaId = it.id,
                        score = it.myListStatus?.score,
                        numReadChapters = it.myListStatus?.numChaptersRead,
                        numReadVolumes = it.myListStatus?.numVolumesRead,
                        totalVolumes = it.numVolumes,
                        totalChapters = it.numChapters
                    )
                    _uiState.setFetchSuccess(result.data)
                }
            } else {
                _uiState.setFailure(result.error.errorMessage)
            }
        }
    }

    fun setStatus(mangaStatus: MangaStatus) {
        _mangaDetailsUpdate.apply {
            value = value?.copy(mangaStatus = mangaStatus)
            if (mangaStatus == MangaStatus.completed) {
                value = value?.copy(
                    numReadVolumes = value?.totalVolumes,
                    numReadChapters = value?.totalChapters
                )
            }
        }
    }

    fun setReadChapterCount(numReadChapter: Int) {
        _mangaDetailsUpdate.apply {
            if (this.value?.mangaStatus == null ||
                (
                    this.value?.mangaStatus != MangaStatus.reading &&
                        this.value?.mangaStatus != MangaStatus.completed
                    )
            ) {
                value = this.value?.copy(mangaStatus = MangaStatus.reading)
            }
            value = value?.copy(numReadChapters = numReadChapter)
        }
    }

    fun setReadVolumeCount(numReadVolume: Int) {
        _mangaDetailsUpdate.apply {
            if (this.value?.mangaStatus == null ||
                (
                    this.value?.mangaStatus != MangaStatus.reading &&
                        this.value?.mangaStatus != MangaStatus.completed
                    )
            ) {
                value = this.value?.copy(mangaStatus = MangaStatus.reading)
            }
            value = value?.copy(numReadVolumes = numReadVolume)
        }
    }

    fun setScore(score: Int) {
        _mangaDetailsUpdate.apply {
            value = value?.copy(score = score)
        }
    }

    fun submitStatusUpdate(mangaId: Int) {
        viewModelScope.launch {
            _uiState.setLoading()
            _mangaDetailsUpdate.value?.let {
                val result = mangaDetailsRepository.updateMangaStatus(it)
                if (result.isSuccess) {
                    getMangaDetails(mangaId)
                } else {
                    _uiState.setFailure(result.error.errorMessage)
                }
            } ?: run {
                _uiState.setFailure(MHError.InvalidStateError.errorMessage)
            }
        }
    }

    fun removeFromList(mangaId: Int) {
        viewModelScope.launch {
            _uiState.setLoading()
            val result = mangaDetailsRepository.removeMangaFromList(mangaId = mangaId)
            if (result.isSuccess) {
                getMangaDetails(mangaId)
            } else {
                _uiState.setFailure(result.error.errorMessage)
            }
        }
    }
}
