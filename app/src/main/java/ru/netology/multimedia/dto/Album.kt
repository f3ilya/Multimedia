package ru.netology.multimedia.dto

import ru.netology.multimedia.utils.UiText

data class Album(
    val id: Long = 1,
    val title: String = "MySong",
    val subtitle: String = "www.111.ru",
    val artist: String = "Me",
    val published: String = "2026",
    val genre: String = "rock",
    val tracks: List<Track>,
)

data class Track(
    val id: Long = 1,
    val file: String = "0.mp3",
    val isLiked: Boolean = false,
    val isPlaying: Boolean = false,
)

data class AlbumState(
    val album: Album? = null,
    val tracks: List<Track> = emptyList(),
    val currentTrack: Track? = null,
    val isAlbumPlaying: Boolean = false,
    val isLoading: Boolean = false,
    val error: UiText? = null,
)
