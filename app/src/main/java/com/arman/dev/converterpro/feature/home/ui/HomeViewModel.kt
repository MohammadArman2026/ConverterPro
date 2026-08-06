package com.arman.dev.converterpro.feature.home.ui

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arman.dev.converterpro.feature.home.domain.repository.HomeRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val homeRepository: HomeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun setLoading(isLoading: Boolean) {
        _uiState.update { it.copy(isLoading = isLoading) }
    }

    fun setError(error: String?) {
        _uiState.update { it.copy(error = error) }
    }

    fun processUris(uris: List<Uri>) {
        viewModelScope.launch {
            setLoading(true)
            if (uris.isEmpty()) {
                setLoading(false)
                return@launch
            }
            val response = homeRepository.processUris(
                newUris = uris,
                existingUris = getExistingUri()
            )
            response
                .onSuccess {it->
                    _uiState.update { uiState ->
                        uiState.copy(
                            mediaList = uiState.mediaList + it,
                            isLoading = false ,
                            error = null
                        )
                    }
                }.onFailure {error->
                    _uiState.update {uiState ->
                        uiState.copy(
                            isLoading = false ,
                            error = error.message
                        )
                    }
                }
        }
    }

    private fun getExistingUri(): List<Uri> = _uiState.value.mediaList.map { it.uri }

    fun removeUri(uri: Uri){
        _uiState.update { state ->
            state.copy(
             mediaList = state.mediaList.filterNot { it.uri == uri}
            )
        }
    }

    /**
     * Drops the picked files once they have been handed to the converter, so returning here starts
     * from an empty list instead of the previous selection.
     */
    fun clearSelection() {
        _uiState.update { it.copy(mediaList = emptyList(), error = null) }
    }
}
