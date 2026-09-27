package ru.netology.multimedia.viewmodel

import android.media.AudioAttributes
import android.media.MediaPlayer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.netology.multimedia.R
import ru.netology.multimedia.api.RetrofitClient
import ru.netology.multimedia.dto.AlbumState
import ru.netology.multimedia.dto.Track
import ru.netology.multimedia.error.EmptyBodyException
import ru.netology.multimedia.error.HttpException
import ru.netology.multimedia.utils.UiText

class MainViewModel : ViewModel() {
    private val _state = MutableStateFlow(AlbumState())
    val state: StateFlow<AlbumState> = _state.asStateFlow()

    private var mediaPlayer: MediaPlayer? = null
    private val baseAudioUrl =
        "https://raw.githubusercontent.com/netology-code/andad-homeworks/master/09_multimedia/data/"

    init {
        loadAlbum()
    }

    fun loadAlbum() {
        _state.value = AlbumState(isLoading = true)

        viewModelScope.launch {
            runCatching {
                val response = RetrofitClient.apiService.getAlbum()

                if (!response.isSuccessful) throw HttpException(response.code())

                response.body() ?: throw EmptyBodyException()
            }.onSuccess { album ->
                _state.value = AlbumState(album = album, tracks = album.tracks, isLoading = false)
            }.onFailure { exception ->
                if (exception is CancellationException) throw exception

                val uiError = when (exception) {
                    is HttpException -> when (exception.code) {
                        404 -> UiText.ResourceString(R.string.album_not_found)
                        500 -> UiText.ResourceString(R.string.server_error)
                        else -> UiText.ResourceString(R.string.error, exception.code)
                    }

                    is EmptyBodyException -> UiText.ResourceString(R.string.body_is_null)
                    else -> UiText.ResourceString(
                        R.string.network_error,
                        exception.localizedMessage ?: ""
                    )
                }

                _state.value = AlbumState(error = uiError)
            }
        }
    }

    fun playTrack(track: Track) {
        val currentState = _state.value
        if (currentState.tracks.isEmpty()) return

        val isCurrent = currentState.currentTrack?.id == track.id

        if (isCurrent) {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.pause()
                updatePlaybackState(track.id, false)
            } else {
                mediaPlayer?.start()
                updatePlaybackState(track.id, true)
            }
        } else {
            mediaPlayer?.release()
            startMediaPlayer(track)
            updatePlaybackState(track.id, true)
        }
    }

    fun playAlbum() {
        val currentState = _state.value
        if (currentState.tracks.isEmpty()) return

        val trackToPlay = currentState.currentTrack ?: currentState.tracks.first()
        playTrack(trackToPlay)
    }

    fun like(track: Track) {
        val currentState = _state.value
        val updatedTracks = currentState.tracks.map {
            if (it.id == track.id) it.copy(isLiked = !it.isLiked) else it
        }

        val updatedCurrentTrack = if (currentState.currentTrack?.id == track.id) {
            currentState.currentTrack.copy(isLiked = !track.isLiked)
        } else {
            currentState.currentTrack
        }

        _state.value = currentState.copy(tracks = updatedTracks, currentTrack = updatedCurrentTrack)
    }

    private fun startMediaPlayer(track: Track) {
        mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(baseAudioUrl + track.file)
            prepareAsync()
            setOnPreparedListener { it.start() }
            setOnCompletionListener { playNextTrack() }
            setOnErrorListener { _, _, _ ->
                _state.value =
                    _state.value.copy(error = UiText.ResourceString(R.string.playback_error))
                false
            }
        }
    }

    private fun playNextTrack() {
        val currentState = _state.value
        if (currentState.tracks.isEmpty()) return

        val currentIndex =
            currentState.tracks.indexOfFirst { it.id == currentState.currentTrack?.id }
        val nextIndex =
            if (currentIndex == -1 || currentIndex == currentState.tracks.lastIndex) 0 else currentIndex + 1
        val nextTrack = currentState.tracks[nextIndex]

        mediaPlayer?.release()
        startMediaPlayer(nextTrack)
        updatePlaybackState(nextTrack.id, true)
    }

    private fun updatePlaybackState(playingTrackId: Long, isPlaying: Boolean) {
        val currentState = _state.value
        val updatedTracks = currentState.tracks.map { current ->
            if (current.id == playingTrackId) {
                current.copy(isPlaying = isPlaying)
            } else {
                current.copy(isPlaying = false)
            }
        }
        val currentTrack = updatedTracks.find { it.id == playingTrackId }

        _state.value = currentState.copy(
            tracks = updatedTracks,
            currentTrack = currentTrack,
            isAlbumPlaying = isPlaying
        )
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
        mediaPlayer = null
    }
}