package com.arman.dev.converterpro.feature.files.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arman.dev.converterpro.core.player.AudioPlayer
import com.arman.dev.converterpro.feature.files.domain.model.ConvertedFile
import com.arman.dev.converterpro.feature.files.domain.repository.FilesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FilesViewModel @Inject constructor(
    private val filesRepository: FilesRepository,
    private val audioPlayer: AudioPlayer
) : ViewModel() {

    private val _uiState = MutableStateFlow(FilesUiState())
    val uiState: StateFlow<FilesUiState> = _uiState.asStateFlow()

    init {
        observePlayback()
    }

    fun loadFiles() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            filesRepository.loadConvertedFiles()
                .onSuccess { files ->
                    _uiState.update {
                        it.copy(isLoading = false, files = files, error = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            error = error.message ?: "Unable to load converted files."
                        )
                    }
                }
        }
    }

    /**
     * Loads every converted file into the queue starting at [file] so the player screen can skip
     * through the whole list. A file that is already loaded resumes instead of restarting.
     */
    fun onPlayClick(file: ConvertedFile) {
        val playback = audioPlayer.state.value

        if (playback.currentTrack?.id == file.id) {
            if (!playback.isPlaying) audioPlayer.togglePlayPause()
            return
        }

        val files = _uiState.value.files
        val startIndex = files.indexOfFirst { it.id == file.id }
        if (startIndex == -1) return

        audioPlayer.setQueue(
            tracks = files.map(ConvertedFile::toPlaybackTrack),
            startIndex = startIndex
        )
    }

    fun onDeleteClick(file: ConvertedFile) {
        viewModelScope.launch {
            filesRepository.deleteConvertedFile(file.uri)
                .onSuccess {
                    if (audioPlayer.state.value.currentTrack?.id == file.id) {
                        audioPlayer.stop()
                    }
                    _uiState.update { state ->
                        state.copy(
                            files = state.files.filterNot { it.id == file.id },
                            message = "${file.name} deleted"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(error = error.message ?: "Unable to delete that file.")
                    }
                }
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null, error = null) }
    }

    private fun observePlayback() {
        viewModelScope.launch {
            audioPlayer.state.collect { playback ->
                val playingId = playback.currentTrack?.id?.takeIf { playback.isPlaying }
                _uiState.update { it.copy(playingFileId = playingId) }
            }
        }
    }
}
